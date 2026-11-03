package com.poultry.smartfarm.inventory.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.document.Purchase;
import com.poultry.smartfarm.inventory.document.Supplier;
import com.poultry.smartfarm.inventory.dto.PurchaseRequest;
import com.poultry.smartfarm.inventory.dto.SupplierRequest;
import com.poultry.smartfarm.inventory.repository.PurchaseRepository;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SupplierAndPurchaseService {

    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;
    private final FarmRepository farmRepository;
    private final InventoryService inventoryService;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public SupplierAndPurchaseService(SupplierRepository supplierRepository,
                                      PurchaseRepository purchaseRepository,
                                      FarmRepository farmRepository,
                                      InventoryService inventoryService,
                                      FarmAccessGuard farmAccessGuard,
                                      AuditLogService auditLogService) {
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
        this.farmRepository = farmRepository;
        this.inventoryService = inventoryService;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public List<Supplier> listSuppliers(String farmId) {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return supplierRepository.findByFarmIdOrderBySupplierNameAsc(farmId);
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return supplierRepository.findAll();
        }
        if (currentUser.getRole().isFarmRole() && currentUser.getAssignedFarmIds() != null && !currentUser.getAssignedFarmIds().isEmpty()) {
            return supplierRepository.findByFarmIdInOrderBySupplierNameAsc(currentUser.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public Supplier createSupplier(SupplierRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        farmAccessGuard.requireFarmWriteAccess(req.getFarmId());
        Farm farm = farmRepository.findById(req.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + req.getFarmId()));

        String code = req.getSupplierCode().trim().toUpperCase();
        if (supplierRepository.existsBySupplierCodeIgnoreCase(code)) {
            throw new BadRequestException("Supplier code '" + code + "' already exists.");
        }

        Supplier s = new Supplier();
        s.setFarmId(farm.getId());
        s.setFarmName(farm.getFarmName());
        s.setSupplierCode(code);
        copySupplierFields(req, s);
        Supplier saved = supplierRepository.save(s);

        auditLogService.record(actor, farm.getId(), "SUPPLIER_CREATED", "SUPPLIER", saved.getId(),
                "Added supplier " + saved.getSupplierName() + " (" + saved.getSupplierCode() + ")");
        return saved;
    }

    public Supplier updateSupplier(String id, SupplierRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
        farmAccessGuard.requireFarmWriteAccess(s.getFarmId());
        copySupplierFields(req, s);
        Supplier saved = supplierRepository.save(s);

        auditLogService.record(actor, s.getFarmId(), "SUPPLIER_UPDATED", "SUPPLIER", saved.getId(),
                "Updated supplier " + saved.getSupplierName());
        return saved;
    }

    private void copySupplierFields(SupplierRequest req, Supplier s) {
        s.setSupplierName(req.getSupplierName().trim());
        s.setContactPerson(req.getContactPerson());
        s.setPhone(req.getPhone().trim());
        s.setEmail(req.getEmail());
        s.setGstNumber(req.getGstNumber());
        s.setAddress(req.getAddress());
        s.setCity(req.getCity());
        s.setState(req.getState());
        if (req.getSuppliedCategories() != null) {
            s.setSuppliedCategories(req.getSuppliedCategories());
        }
        s.setNotes(req.getNotes());
    }

    // -------------------------------------------------------------------------
    // PURCHASE INVOICES (AUTO-INCREMENTS INVENTORY + STOCK MOVEMENT + SUPPLIER LEDGER)
    // -------------------------------------------------------------------------
    public List<Purchase> listPurchases(String farmId, String supplierId) {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (supplierId != null && !supplierId.isBlank()) {
            Supplier sup = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + supplierId));
            farmAccessGuard.requireFarmAccess(sup.getFarmId());
            return purchaseRepository.findBySupplierIdOrderByPurchaseDateDesc(supplierId);
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return purchaseRepository.findByFarmIdOrderByPurchaseDateDescCreatedAtDesc(farmId);
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return purchaseRepository.findAll();
        }
        if (currentUser.getRole().isFarmRole() && currentUser.getAssignedFarmIds() != null && !currentUser.getAssignedFarmIds().isEmpty()) {
            return purchaseRepository.findByFarmIdInOrderByPurchaseDateDescCreatedAtDesc(currentUser.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public Purchase recordPurchaseInvoice(PurchaseRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        farmAccessGuard.requireFarmWriteAccess(req.getFarmId());

        Farm farm = farmRepository.findById(req.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + req.getFarmId()));
        Supplier supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + req.getSupplierId()));
        InventoryItem item = inventoryService.getItemWithAccess(req.getInventoryItemId());

        if (!item.getFarmId().equals(farm.getId())) {
            throw new BadRequestException("Selected inventory item does not belong to farm " + farm.getFarmName());
        }

        double subtotal = req.getQuantityPurchased() * req.getPurchaseRatePerUnit();
        double totalInvoice = round2(subtotal + req.getTransportAndOtherCharges());
        double paid = round2(req.getAmountPaid());
        if (paid > totalInvoice) {
            throw new BadRequestException("Amount paid (" + paid + ") cannot exceed total invoice amount (" + totalInvoice + ").");
        }
        double balance = round2(totalInvoice - paid);
        String payStatus = balance <= 0.0 ? "PAID" : (paid > 0.0 ? "PARTIAL" : "CREDIT_PENDING");

        Purchase p = new Purchase();
        p.setFarmId(farm.getId());
        p.setFarmName(farm.getFarmName());
        p.setInvoiceNumber(req.getInvoiceNumber().trim().toUpperCase());
        p.setPurchaseDate(req.getPurchaseDate());
        p.setSupplierId(supplier.getId());
        p.setSupplierName(supplier.getSupplierName());
        p.setInventoryItemId(item.getId());
        p.setItemCode(item.getItemCode());
        p.setItemName(item.getItemName());
        p.setCategory(item.getCategory());
        p.setSubCategory(item.getSubCategory());
        p.setQuantityPurchased(round2(req.getQuantityPurchased()));
        p.setUnit(item.getUnit());
        p.setPurchaseRatePerUnit(round2(req.getPurchaseRatePerUnit()));
        p.setTransportAndOtherCharges(round2(req.getTransportAndOtherCharges()));
        p.setTotalInvoiceAmount(totalInvoice);
        p.setAmountPaid(paid);
        p.setBalancePayable(balance);
        p.setPaymentStatus(payStatus);
        p.setBatchLotNumber(req.getBatchLotNumber());
        p.setExpiryDate(req.getExpiryDate());
        p.setRemarks(req.getRemarks());
        p.setRecordedBy(actor.getUsername());
        Purchase saved = purchaseRepository.save(p);

        // Automatically increase stock and log PURCHASE_IN movement!
        inventoryService.addStockFromPurchase(item.getId(), saved.getQuantityPurchased(), saved.getPurchaseRatePerUnit(),
                saved.getBatchLotNumber(), saved.getExpiryDate(), supplier, saved.getId(), saved.getInvoiceNumber(), actor);

        // Synchronize Supplier payable balances
        recalculateSupplierTotals(supplier);

        auditLogService.record(actor, farm.getId(), "PURCHASE_RECORDED", "PURCHASE", saved.getId(),
                "Invoice #" + saved.getInvoiceNumber() + ": Purchased " + saved.getQuantityPurchased() + " " + saved.getUnit()
                        + " of " + saved.getItemName() + " (₹" + totalInvoice + ", Balance ₹" + balance + ")");
        return saved;
    }

    public void recalculateSupplierTotals(Supplier supplier) {
        List<Purchase> history = purchaseRepository.findBySupplierIdOrderByPurchaseDateDesc(supplier.getId());
        double totalPur = round2(history.stream().mapToDouble(Purchase::getTotalInvoiceAmount).sum());
        double totalPaid = round2(history.stream().mapToDouble(Purchase::getAmountPaid).sum());
        double balance = round2(Math.max(0.0, totalPur - totalPaid));
        supplier.setTotalPurchasesAmount(totalPur);
        supplier.setTotalPaidAmount(totalPaid);
        supplier.setOutstandingPayable(balance);
        supplierRepository.save(supplier);
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
