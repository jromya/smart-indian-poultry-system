package com.poultry.smartfarm.inventory.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.document.StockMovement;
import com.poultry.smartfarm.inventory.document.Supplier;
import com.poultry.smartfarm.inventory.dto.InventoryItemRequest;
import com.poultry.smartfarm.inventory.dto.StockAdjustmentRequest;
import com.poultry.smartfarm.inventory.dto.StockIssueRequest;
import com.poultry.smartfarm.inventory.dto.StockTransferRequest;
import com.poultry.smartfarm.inventory.repository.InventoryItemRepository;
import com.poultry.smartfarm.inventory.repository.StockMovementRepository;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryRepository;
    private final StockMovementRepository movementRepository;
    private final SupplierRepository supplierRepository;
    private final FarmRepository farmRepository;
    private final BatchService batchService;
    private final NotificationAlertService alertService;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public InventoryService(InventoryItemRepository inventoryRepository,
                            StockMovementRepository movementRepository,
                            SupplierRepository supplierRepository,
                            FarmRepository farmRepository,
                            BatchService batchService,
                            NotificationAlertService alertService,
                            FarmAccessGuard farmAccessGuard,
                            AuditLogService auditLogService) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.supplierRepository = supplierRepository;
        this.farmRepository = farmRepository;
        this.batchService = batchService;
        this.alertService = alertService;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public List<InventoryItem> listInventoryItems(String farmId, String category) {
        User currentUser = farmAccessGuard.getCurrentUser();
        List<InventoryItem> items;

        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            items = inventoryRepository.findByFarmIdOrderByCategoryAscItemNameAsc(farmId);
        } else if (currentUser.getRole() == Role.SUPER_ADMIN) {
            items = inventoryRepository.findAll();
        } else if (currentUser.getRole().isFarmRole() && currentUser.getAssignedFarmIds() != null && !currentUser.getAssignedFarmIds().isEmpty()) {
            items = inventoryRepository.findByFarmIdInOrderByCategoryAscItemNameAsc(currentUser.getAssignedFarmIds());
        } else {
            items = Collections.emptyList();
        }

        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            items = items.stream()
                    .filter(i -> category.equalsIgnoreCase(i.getCategory()))
                    .collect(Collectors.toList());
        }
        return items;
    }

    public InventoryItem getItemWithAccess(String itemId) {
        InventoryItem item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + itemId));
        farmAccessGuard.requireFarmAccess(item.getFarmId());
        return item;
    }

    public InventoryItem createInventoryItem(InventoryItemRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        farmAccessGuard.requireFarmWriteAccess(req.getFarmId());
        Farm farm = farmRepository.findById(req.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + req.getFarmId()));

        String code = req.getItemCode().trim().toUpperCase();
        if (inventoryRepository.existsByFarmIdAndItemCodeIgnoreCase(farm.getId(), code)) {
            throw new BadRequestException("An inventory item with code '" + code + "' already exists on " + farm.getFarmName());
        }

        InventoryItem item = new InventoryItem();
        item.setFarmId(farm.getId());
        item.setFarmName(farm.getFarmName());
        item.setItemCode(code);
        item.setItemName(req.getItemName().trim());
        item.setCategory(req.getCategory().trim().toUpperCase());
        item.setSubCategory(req.getSubCategory() != null ? req.getSubCategory().trim().toUpperCase() : "GENERAL");
        item.setUnit(req.getUnit().trim().toUpperCase());
        item.setCurrentStock(round2(req.getOpeningStock()));
        item.setReorderLevel(round2(req.getReorderLevel()));
        item.setUnitCostInr(round2(req.getUnitCostInr()));
        item.setBatchLotNumber(req.getBatchLotNumber());
        item.setExpiryDate(req.getExpiryDate());
        item.setStorageLocation(req.getStorageLocation());

        if (req.getPreferredSupplierId() != null && !req.getPreferredSupplierId().isBlank()) {
            supplierRepository.findById(req.getPreferredSupplierId()).ifPresent(s -> {
                item.setPreferredSupplierId(s.getId());
                item.setPreferredSupplierName(s.getSupplierName());
            });
        }
        item.recalculateValuation();
        InventoryItem saved = inventoryRepository.save(item);

        if (saved.getCurrentStock() > 0) {
            recordMovement(saved, "OPENING_STOCK", saved.getCurrentStock(), 0.0, saved.getCurrentStock(),
                    saved.getUnitCostInr(), "INITIAL_SETUP", saved.getId(), null, null, null, null,
                    "Opening stock configured", actor.getUsername());
        }

        alertService.refreshFarmAlertsInternal(farm.getId());
        auditLogService.record(actor, farm.getId(), "INVENTORY_ITEM_CREATED", "INVENTORY_ITEM", saved.getId(),
                "Created inventory item " + saved.getItemCode() + " (" + saved.getItemName() + ") with stock "
                        + saved.getCurrentStock() + " " + saved.getUnit());
        return saved;
    }

    public InventoryItem updateInventoryItem(String id, InventoryItemRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        InventoryItem item = getItemWithAccess(id);
        farmAccessGuard.requireFarmWriteAccess(item.getFarmId());

        item.setItemName(req.getItemName().trim());
        item.setCategory(req.getCategory().trim().toUpperCase());
        if (req.getSubCategory() != null) item.setSubCategory(req.getSubCategory().trim().toUpperCase());
        item.setUnit(req.getUnit().trim().toUpperCase());
        item.setReorderLevel(round2(req.getReorderLevel()));
        item.setUnitCostInr(round2(req.getUnitCostInr()));
        item.setBatchLotNumber(req.getBatchLotNumber());
        item.setExpiryDate(req.getExpiryDate());
        item.setStorageLocation(req.getStorageLocation());
        if (req.getPreferredSupplierId() != null && !req.getPreferredSupplierId().isBlank()) {
            supplierRepository.findById(req.getPreferredSupplierId()).ifPresent(s -> {
                item.setPreferredSupplierId(s.getId());
                item.setPreferredSupplierName(s.getSupplierName());
            });
        }
        item.recalculateValuation();
        InventoryItem saved = inventoryRepository.save(item);
        alertService.refreshFarmAlertsInternal(saved.getFarmId());

        auditLogService.record(actor, saved.getFarmId(), "INVENTORY_ITEM_UPDATED", "INVENTORY_ITEM", saved.getId(),
                "Updated inventory item metadata for " + saved.getItemName());
        return saved;
    }

    /**
     * Reusable atomic stock deduction helper invoked when issuing feed, medicine, vaccine, equipment, or supplies.
     */
    public InventoryItem deductItemForOperation(String itemId, double quantity, String referenceType,
                                                String referenceId, Batch batch, String remarks, User actor) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity to deduct must be greater than zero.");
        }
        InventoryItem item = getItemWithAccess(itemId);
        if (batch != null && !item.getFarmId().equals(batch.getFarmId())) {
            throw new BadRequestException("Farm mismatch: Inventory item '" + item.getItemName()
                    + "' belongs to a different farm than batch " + batch.getBatchCode() + ".");
        }

        double before = item.getCurrentStock();
        if (before < quantity) {
            throw new BadRequestException("Insufficient inventory stock for '" + item.getItemName()
                    + "': Available = " + before + " " + item.getUnit()
                    + ", Requested = " + quantity + " " + item.getUnit() + ".");
        }

        double after = round2(before - quantity);
        item.setCurrentStock(after);
        item.recalculateValuation();
        InventoryItem saved = inventoryRepository.save(item);

        recordMovement(saved, "BATCH_ISSUE", quantity, before, after, saved.getUnitCostInr(),
                referenceType, referenceId,
                batch != null ? batch.getId() : null,
                batch != null ? batch.getBatchCode() : null,
                null, null, remarks, actor != null ? actor.getUsername() : "SYSTEM");

        alertService.refreshFarmAlertsInternal(saved.getFarmId());
        return saved;
    }

    /**
     * Reusable stock addition helper invoked when a Supplier Purchase Invoice is recorded.
     */
    public InventoryItem addStockFromPurchase(String itemId, double quantityPurchased, double purchaseRatePerUnit,
                                              String batchLotNumber, LocalDate expiryDate, Supplier supplier,
                                              String purchaseId, String invoiceNo, User actor) {
        InventoryItem item = getItemWithAccess(itemId);
        double before = item.getCurrentStock();
        double oldRate = item.getUnitCostInr();
        double after = round2(before + quantityPurchased);

        // Weighted average unit cost valuation
        double weightedCost = after > 0
                ? round2(((before * oldRate) + (quantityPurchased * purchaseRatePerUnit)) / after)
                : round2(purchaseRatePerUnit);

        item.setCurrentStock(after);
        item.setUnitCostInr(weightedCost);
        if (batchLotNumber != null && !batchLotNumber.isBlank()) {
            item.setBatchLotNumber(batchLotNumber.trim());
        }
        if (expiryDate != null) {
            item.setExpiryDate(expiryDate);
        }
        if (supplier != null) {
            item.setPreferredSupplierId(supplier.getId());
            item.setPreferredSupplierName(supplier.getSupplierName());
        }
        item.recalculateValuation();
        InventoryItem saved = inventoryRepository.save(item);

        recordMovement(saved, "PURCHASE_IN", quantityPurchased, before, after, purchaseRatePerUnit,
                "PURCHASE", purchaseId, null, null, null, null,
                "Purchase Invoice #" + invoiceNo + (supplier != null ? " from " + supplier.getSupplierName() : ""),
                actor != null ? actor.getUsername() : "SYSTEM");

        alertService.refreshFarmAlertsInternal(saved.getFarmId());
        return saved;
    }

    public InventoryItem issueStockDirect(StockIssueRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = (req.getBatchId() != null && !req.getBatchId().isBlank())
                ? batchService.getBatchDocumentWithAccess(req.getBatchId()) : null;
        if (batch != null) {
            batchService.ensureBatchEditable(batch);
        }

        InventoryItem updated = deductItemForOperation(req.getInventoryItemId(), req.getQuantity(),
                "MANUAL_STOCK_ISSUE", null, batch,
                req.getRemarks() != null ? req.getRemarks() : "Issued poultry supply", actor);

        auditLogService.record(actor, updated.getFarmId(), "STOCK_ISSUED", "INVENTORY_ITEM", updated.getId(),
                "Issued " + req.getQuantity() + " " + updated.getUnit() + " of " + updated.getItemName()
                        + (batch != null ? " to batch " + batch.getBatchCode() : "")
                        + ". Remaining: " + updated.getCurrentStock());
        return updated;
    }

    public InventoryItem adjustStock(StockAdjustmentRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        InventoryItem item = getItemWithAccess(req.getInventoryItemId());
        farmAccessGuard.requireFarmWriteAccess(item.getFarmId());

        String type = req.getAdjustmentType().trim().toUpperCase();
        double qty = round2(req.getQuantity());
        double before = item.getCurrentStock();
        double after;
        String movementType;

        if ("ADD".equals(type) || "ADJUSTMENT_ADD".equals(type)) {
            after = round2(before + qty);
            movementType = "ADJUSTMENT_ADD";
        } else if ("DEDUCT".equals(type) || "ADJUSTMENT_DEDUCT".equals(type)) {
            if (before < qty) {
                throw new BadRequestException("Cannot deduct " + qty + " " + item.getUnit()
                        + "; current stock is only " + before + " " + item.getUnit() + ".");
            }
            after = round2(before - qty);
            movementType = "ADJUSTMENT_DEDUCT";
        } else {
            throw new BadRequestException("Adjustment type must be ADD or DEDUCT.");
        }

        item.setCurrentStock(after);
        item.recalculateValuation();
        InventoryItem saved = inventoryRepository.save(item);

        recordMovement(saved, movementType, qty, before, after, saved.getUnitCostInr(),
                "STOCK_ADJUSTMENT", saved.getId(), null, null, null, null,
                req.getReason().trim(), actor.getUsername());

        alertService.refreshFarmAlertsInternal(saved.getFarmId());
        auditLogService.record(actor, saved.getFarmId(), "STOCK_ADJUSTED", "INVENTORY_ITEM", saved.getId(),
                movementType + " " + qty + " " + saved.getUnit() + " on " + saved.getItemName()
                        + ". Reason: " + req.getReason().trim());
        return saved;
    }

    public Map<String, InventoryItem> transferStockBetweenFarms(StockTransferRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        InventoryItem source = getItemWithAccess(req.getSourceInventoryItemId());
        farmAccessGuard.requireFarmWriteAccess(source.getFarmId());
        farmAccessGuard.requireFarmWriteAccess(req.getDestinationFarmId());

        if (source.getFarmId().equals(req.getDestinationFarmId())) {
            throw new BadRequestException("Destination farm must be different from source farm for a stock transfer.");
        }
        Farm destFarm = farmRepository.findById(req.getDestinationFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination farm not found: " + req.getDestinationFarmId()));

        double qty = round2(req.getQuantity());
        double srcBefore = source.getCurrentStock();
        if (srcBefore < qty) {
            throw new BadRequestException("Insufficient stock to transfer: Available = "
                    + srcBefore + " " + source.getUnit());
        }

        // Deduct from source farm
        double srcAfter = round2(srcBefore - qty);
        source.setCurrentStock(srcAfter);
        source.recalculateValuation();
        InventoryItem savedSource = inventoryRepository.save(source);

        // Find or create matching itemCode on destination farm
        InventoryItem dest = inventoryRepository.findByFarmIdAndItemCodeIgnoreCase(destFarm.getId(), source.getItemCode())
                .orElseGet(() -> {
                    InventoryItem created = new InventoryItem();
                    created.setFarmId(destFarm.getId());
                    created.setFarmName(destFarm.getFarmName());
                    created.setItemCode(source.getItemCode());
                    created.setItemName(source.getItemName());
                    created.setCategory(source.getCategory());
                    created.setSubCategory(source.getSubCategory());
                    created.setUnit(source.getUnit());
                    created.setCurrentStock(0.0);
                    created.setReorderLevel(source.getReorderLevel());
                    created.setUnitCostInr(source.getUnitCostInr());
                    created.setBatchLotNumber(source.getBatchLotNumber());
                    created.setExpiryDate(source.getExpiryDate());
                    created.setPreferredSupplierId(source.getPreferredSupplierId());
                    created.setPreferredSupplierName(source.getPreferredSupplierName());
                    created.setStorageLocation(req.getDestinationStorageLocation() != null
                            ? req.getDestinationStorageLocation() : "Main Store");
                    return created;
                });

        double destBefore = dest.getCurrentStock();
        double destAfter = round2(destBefore + qty);
        double destWeightedCost = destAfter > 0
                ? round2(((destBefore * dest.getUnitCostInr()) + (qty * source.getUnitCostInr())) / destAfter)
                : source.getUnitCostInr();
        dest.setCurrentStock(destAfter);
        dest.setUnitCostInr(destWeightedCost);
        if (req.getDestinationStorageLocation() != null && !req.getDestinationStorageLocation().isBlank()) {
            dest.setStorageLocation(req.getDestinationStorageLocation());
        }
        dest.recalculateValuation();
        InventoryItem savedDest = inventoryRepository.save(dest);

        String note = req.getRemarks() != null && !req.getRemarks().isBlank()
                ? req.getRemarks() : "Inter-farm stock transfer";

        recordMovement(savedSource, "TRANSFER_OUT", qty, srcBefore, srcAfter, savedSource.getUnitCostInr(),
                "FARM_TRANSFER", savedDest.getId(), null, null, destFarm.getId(), destFarm.getFarmName(),
                note + " -> To " + destFarm.getFarmName(), actor.getUsername());

        recordMovement(savedDest, "TRANSFER_IN", qty, destBefore, destAfter, savedSource.getUnitCostInr(),
                "FARM_TRANSFER", savedSource.getId(), null, null, savedSource.getFarmId(), savedSource.getFarmName(),
                note + " <- From " + savedSource.getFarmName(), actor.getUsername());

        alertService.refreshFarmAlertsInternal(savedSource.getFarmId());
        alertService.refreshFarmAlertsInternal(destFarm.getId());

        auditLogService.record(actor, savedSource.getFarmId(), "STOCK_TRANSFERRED", "INVENTORY_ITEM", savedSource.getId(),
                "Transferred " + qty + " " + savedSource.getUnit() + " of " + savedSource.getItemName()
                        + " from " + savedSource.getFarmName() + " to " + destFarm.getFarmName());

        Map<String, InventoryItem> out = new LinkedHashMap<>();
        out.put("sourceItem", savedSource);
        out.put("destinationItem", savedDest);
        return out;
    }

    public List<StockMovement> getStockMovements(String farmId, String inventoryItemId) {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (inventoryItemId != null && !inventoryItemId.isBlank()) {
            InventoryItem item = getItemWithAccess(inventoryItemId);
            return movementRepository.findByInventoryItemIdOrderByMovementAtDesc(item.getId());
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return movementRepository.findTop100ByFarmIdOrderByMovementAtDesc(farmId);
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return movementRepository.findAll().stream()
                    .sorted((a, b) -> b.getMovementAt().compareTo(a.getMovementAt()))
                    .limit(100)
                    .collect(Collectors.toList());
        }
        if (currentUser.getRole().isFarmRole() && currentUser.getAssignedFarmIds() != null && !currentUser.getAssignedFarmIds().isEmpty()) {
            return movementRepository.findTop100ByFarmIdInOrderByMovementAtDesc(currentUser.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public Map<String, Object> getInventoryValuationSummary(String farmId) {
        List<InventoryItem> items = listInventoryItems(farmId, null);
        LocalDate today = LocalDate.now();

        double totalValuation = 0.0;
        long lowStockCount = 0;
        long expiringOrExpiredCount = 0;
        Map<String, Double> categoryValuation = new LinkedHashMap<>();
        categoryValuation.put("FEED", 0.0);
        categoryValuation.put("MEDICINE", 0.0);
        categoryValuation.put("VACCINE", 0.0);
        categoryValuation.put("EQUIPMENT", 0.0);
        categoryValuation.put("PACKAGING", 0.0);
        categoryValuation.put("OTHER", 0.0);

        for (InventoryItem i : items) {
            if (!i.isActive()) continue;
            double val = i.getTotalValuationInr();
            totalValuation += val;
            String cat = i.getCategory() != null ? i.getCategory().toUpperCase() : "OTHER";
            categoryValuation.put(cat, round2(categoryValuation.getOrDefault(cat, 0.0) + val));

            if (i.getCurrentStock() <= i.getReorderLevel()) {
                lowStockCount++;
            }
            if (i.getExpiryDate() != null && i.getCurrentStock() > 0 && !i.getExpiryDate().isAfter(today.plusDays(30))) {
                expiringOrExpiredCount++;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalItemsCount", items.size());
        summary.put("totalValuationInr", round2(totalValuation));
        summary.put("lowStockItemsCount", lowStockCount);
        summary.put("expiringSoonOrExpiredCount", expiringOrExpiredCount);
        summary.put("categoryValuationInr", categoryValuation);
        return summary;
    }

    public StockMovement recordMovement(InventoryItem item, String movementType, double quantity,
                                        double before, double after, double unitCost,
                                        String refType, String refId, String batchId, String batchCode,
                                        String counterpartyFarmId, String counterpartyFarmName,
                                        String remarks, String username) {
        StockMovement m = new StockMovement();
        m.setFarmId(item.getFarmId());
        m.setFarmName(item.getFarmName());
        m.setInventoryItemId(item.getId());
        m.setItemCode(item.getItemCode());
        m.setItemName(item.getItemName());
        m.setCategory(item.getCategory());
        m.setMovementType(movementType);
        m.setQuantity(round2(quantity));
        m.setUnit(item.getUnit());
        m.setStockBefore(round2(before));
        m.setStockAfter(round2(after));
        m.setUnitCostInr(round2(unitCost));
        m.setTotalMovementValueInr(round2(quantity * unitCost));
        m.setReferenceType(refType);
        m.setReferenceId(refId);
        m.setBatchId(batchId);
        m.setBatchCode(batchCode);
        m.setCounterpartyFarmId(counterpartyFarmId);
        m.setCounterpartyFarmName(counterpartyFarmName);
        m.setRemarks(remarks);
        m.setPerformedBy(username);
        m.setMovementAt(LocalDateTime.now());
        return movementRepository.save(m);
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
