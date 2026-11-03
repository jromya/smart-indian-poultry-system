package com.poultry.smartfarm.commerce.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.commerce.document.ChickenShop;
import com.poultry.smartfarm.commerce.document.Customer;
import com.poultry.smartfarm.commerce.document.Expense;
import com.poultry.smartfarm.commerce.document.PaymentRecord;
import com.poultry.smartfarm.commerce.document.RetailerStore;
import com.poultry.smartfarm.commerce.document.Sale;
import com.poultry.smartfarm.commerce.dto.ExpenseRequest;
import com.poultry.smartfarm.commerce.dto.PaymentRequest;
import com.poultry.smartfarm.commerce.dto.SaleRequest;
import com.poultry.smartfarm.commerce.repository.ChickenShopRepository;
import com.poultry.smartfarm.commerce.repository.CustomerRepository;
import com.poultry.smartfarm.commerce.repository.ExpenseRepository;
import com.poultry.smartfarm.commerce.repository.PaymentRecordRepository;
import com.poultry.smartfarm.commerce.repository.RetailerStoreRepository;
import com.poultry.smartfarm.commerce.repository.SaleRepository;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.inventory.document.FeedRecord;
import com.poultry.smartfarm.inventory.document.MedicineRecord;
import com.poultry.smartfarm.inventory.document.Purchase;
import com.poultry.smartfarm.inventory.document.Supplier;
import com.poultry.smartfarm.inventory.document.VaccinationRecord;
import com.poultry.smartfarm.inventory.repository.FeedRecordRepository;
import com.poultry.smartfarm.inventory.repository.MedicineRecordRepository;
import com.poultry.smartfarm.inventory.repository.PurchaseRepository;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.inventory.repository.VaccinationRecordRepository;
import com.poultry.smartfarm.inventory.service.SupplierAndPurchaseService;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LedgerAndFinancialService {

    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRecordRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;
    private final FeedRecordRepository feedRepository;
    private final MedicineRecordRepository medicineRepository;
    private final VaccinationRecordRepository vaccinationRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final BatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final RetailerStoreRepository retailerRepository;
    private final ChickenShopRepository chickenShopRepository;
    private final BatchService batchService;
    private final CommerceAndShopService commerceService;
    private final SupplierAndPurchaseService supplierPurchaseService;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public LedgerAndFinancialService(SaleRepository saleRepository,
                                     ExpenseRepository expenseRepository,
                                     PaymentRecordRepository paymentRepository,
                                     CustomerRepository customerRepository,
                                     SupplierRepository supplierRepository,
                                     PurchaseRepository purchaseRepository,
                                     FeedRecordRepository feedRepository,
                                     MedicineRecordRepository medicineRepository,
                                     VaccinationRecordRepository vaccinationRepository,
                                     DailyRecordRepository dailyRecordRepository,
                                     BatchRepository batchRepository,
                                     FarmRepository farmRepository,
                                     RetailerStoreRepository retailerRepository,
                                     ChickenShopRepository chickenShopRepository,
                                     BatchService batchService,
                                     CommerceAndShopService commerceService,
                                     SupplierAndPurchaseService supplierPurchaseService,
                                     FarmAccessGuard farmAccessGuard,
                                     AuditLogService auditLogService) {
        this.saleRepository = saleRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
        this.feedRepository = feedRepository;
        this.medicineRepository = medicineRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.batchRepository = batchRepository;
        this.farmRepository = farmRepository;
        this.retailerRepository = retailerRepository;
        this.chickenShopRepository = chickenShopRepository;
        this.batchService = batchService;
        this.commerceService = commerceService;
        this.supplierPurchaseService = supplierPurchaseService;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    // =========================================================================
    // 1. UNIFIED SALES INVOICES (FARM, RETAIL & CHICKEN CUTTING SHOP)
    // =========================================================================
    public List<Sale> listSales(String farmId) {
        User user = farmAccessGuard.getCurrentUser();
        if (user.getRole() == Role.RETAILER || user.getRole() == Role.CHICKEN_SHOP_OWNER) {
            return saleRepository.findByOwnerUserIdOrderBySaleDateDescCreatedAtDesc(user.getId());
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return saleRepository.findByFarmIdOrderBySaleDateDescCreatedAtDesc(farmId);
        }
        if (user.getRole() == Role.SUPER_ADMIN) {
            return saleRepository.findAll();
        }
        if (user.getRole().isFarmRole() && user.getAssignedFarmIds() != null && !user.getAssignedFarmIds().isEmpty()) {
            return saleRepository.findByFarmIdInOrderBySaleDateDescCreatedAtDesc(user.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public Sale recordSaleInvoice(SaleRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        String invNo = req.getInvoiceNumber().trim().toUpperCase();
        if (saleRepository.existsByInvoiceNumberIgnoreCase(invNo)) {
            throw new BadRequestException("Sales invoice number '" + invNo + "' already exists.");
        }

        String cat = req.getSaleCategory().trim().toUpperCase();
        double totalAmt = round2(req.getQuantity() * req.getUnitSellingPriceInr());
        double cashRec = req.getCashReceivedInr() != null ? round2(req.getCashReceivedInr()) : totalAmt;
        if (cashRec > totalAmt) {
            throw new BadRequestException("Cash received (" + cashRec + ") cannot exceed invoice total (" + totalAmt + ").");
        }
        double outstanding = round2(totalAmt - cashRec);
        String payStatus = outstanding <= 0.0 ? "PAID" : (cashRec > 0.0 ? "PARTIAL" : "CREDIT_PENDING");

        Sale s = new Sale();
        s.setInvoiceNumber(invNo);
        s.setSaleDate(req.getSaleDate());
        s.setOwnerUserId(actor.getId());
        s.setSaleCategory(cat);
        s.setProductDescription(req.getProductDescription());
        s.setQuantity(round2(req.getQuantity()));
        s.setUnit(req.getUnit().trim().toUpperCase());
        s.setLiveWeightKg(req.getLiveWeightKg());
        s.setUnitSellingPriceInr(round2(req.getUnitSellingPriceInr()));
        s.setTotalSaleAmountInr(totalAmt);
        s.setCashReceivedInr(cashRec);
        s.setOutstandingBalanceInr(outstanding);
        s.setPaymentStatus(payStatus);
        s.setPaymentMode(req.getPaymentMode() != null ? req.getPaymentMode().toUpperCase() : "CASH");
        s.setRemarks(req.getRemarks());
        s.setRecordedBy(actor.getUsername());

        Customer customer = null;
        if (req.getCustomerId() != null && !req.getCustomerId().isBlank()) {
            customer = customerRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + req.getCustomerId()));
            s.setCustomerId(customer.getId());
            s.setCustomerName(customer.getCustomerName());
            s.setCustomerPhone(customer.getPhone());
            s.setBuyerCategory(customer.getBuyerCategory());
        } else {
            s.setCustomerName(req.getWalkInCustomerName() != null && !req.getWalkInCustomerName().isBlank()
                    ? req.getWalkInCustomerName().trim() : "Walk-in Customer");
            s.setCustomerPhone(req.getWalkInCustomerPhone());
            s.setBuyerCategory(req.getBuyerCategory());
        }

        // Handle Stock & Context synchronization per role/business module:
        if (actor.getRole() == Role.RETAILER) {
            s.setBusinessContext("RETAIL");
            RetailerStore store = commerceService.getOrCreateCurrentRetailStore();
            s.setRetailStoreId(store.getId());
            if ("LIVE_CHICKEN_SALE".equals(cat)) {
                int birds = (int) Math.round(req.getQuantity());
                if (store.getLiveBirdStockCount() < birds) {
                    throw new BadRequestException("Insufficient retail bird stock: Available = " + store.getLiveBirdStockCount());
                }
                store.setLiveBirdStockCount(store.getLiveBirdStockCount() - birds);
                store.setTotalChickenSalesRevenueInr(round2(store.getTotalChickenSalesRevenueInr() + totalAmt));
            } else if ("EGG_SALE".equals(cat)) {
                int eggs = "TRAYS".equalsIgnoreCase(s.getUnit())
                        ? (int) Math.round(req.getQuantity() * 30) : (int) Math.round(req.getQuantity());
                if (store.getEggStockPieces() < eggs) {
                    throw new BadRequestException("Insufficient retail egg stock: Available = " + store.getEggStockPieces() + " pieces");
                }
                store.setEggStockPieces(store.getEggStockPieces() - eggs);
                store.setTotalEggSalesRevenueInr(round2(store.getTotalEggSalesRevenueInr() + totalAmt));
            }
            retailerRepository.save(store);
        } else if (actor.getRole() == Role.CHICKEN_SHOP_OWNER) {
            s.setBusinessContext("CHICKEN_SHOP");
            ChickenShop shop = commerceService.getOrCreateCurrentChickenShop();
            s.setChickenShopId(shop.getId());
            if ("DRESSED_MEAT_SALE".equals(cat)) {
                if (shop.getDressedMeatStockKg() < req.getQuantity()) {
                    throw new BadRequestException("Insufficient dressed meat stock in shop: Available = "
                            + shop.getDressedMeatStockKg() + " kg");
                }
                shop.setDressedMeatStockKg(round2(shop.getDressedMeatStockKg() - req.getQuantity()));
                shop.setTotalDressedMeatSoldKg(round2(shop.getTotalDressedMeatSoldKg() + req.getQuantity()));
                shop.setTotalMeatSalesRevenueInr(round2(shop.getTotalMeatSalesRevenueInr() + totalAmt));
            } else {
                shop.setTotalByProductSalesRevenueInr(round2(shop.getTotalByProductSalesRevenueInr() + totalAmt));
            }
            chickenShopRepository.save(shop);
        } else {
            // FARM context sale (Live Birds, Eggs, or Manure/By-Products)
            s.setBusinessContext("FARM");
            if (req.getBatchId() != null && !req.getBatchId().isBlank()) {
                Batch batch = batchService.getBatchDocumentWithAccess(req.getBatchId());
                farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());
                s.setFarmId(batch.getFarmId());
                s.setFarmName(batch.getFarmName());
                s.setBatchId(batch.getId());
                s.setBatchCode(batch.getBatchCode());

                BatchResponse view = BatchResponse.fromDocument(batch);
                if ("LIVE_CHICKEN_SALE".equals(cat)) {
                    int birdsSold = "BIRDS".equalsIgnoreCase(s.getUnit())
                            ? (int) Math.round(req.getQuantity())
                            : (req.getLiveWeightKg() != null && batch.getLatestAverageWeightGrams() > 0
                                    ? Math.max(1, (int) Math.round((req.getLiveWeightKg() * 1000.0) / batch.getLatestAverageWeightGrams()))
                                    : 0);
                    if (birdsSold > view.getCurrentLiveBirds()) {
                        throw new BadRequestException("Insufficient live birds in batch: Available = " + view.getCurrentLiveBirds());
                    }
                    batch.setTotalBirdsSold(batch.getTotalBirdsSold() + birdsSold);
                    if (req.getLiveWeightKg() != null) {
                        batch.setTotalSoldLiveWeightKg(round2(batch.getTotalSoldLiveWeightKg() + req.getLiveWeightKg()));
                    }
                    batch.setTotalBirdSalesRevenue(round2(batch.getTotalBirdSalesRevenue() + totalAmt));
                    batchRepository.save(batch);
                    batchService.syncHouseOccupancy(batch.getPoultryHouseId());
                } else if ("EGG_SALE".equals(cat)) {
                    int eggsSold = "TRAYS".equalsIgnoreCase(s.getUnit())
                            ? (int) Math.round(req.getQuantity() * 30) : (int) Math.round(req.getQuantity());
                    if (eggsSold > view.getCurrentEggStockGood()) {
                        throw new BadRequestException("Insufficient good egg stock in batch: Available = " + view.getCurrentEggStockGood());
                    }
                    batch.setTotalEggsSold(batch.getTotalEggsSold() + eggsSold);
                    batch.setTotalEggSalesRevenue(round2(batch.getTotalEggSalesRevenue() + totalAmt));
                    batchRepository.save(batch);
                }
            } else if (req.getFarmId() != null && !req.getFarmId().isBlank()) {
                farmAccessGuard.requireFarmWriteAccess(req.getFarmId());
                Farm farm = farmRepository.findById(req.getFarmId())
                        .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + req.getFarmId()));
                s.setFarmId(farm.getId());
                s.setFarmName(farm.getFarmName());
            } else {
                throw new BadRequestException("Please select a Farm or Batch for farm sales.");
            }
        }

        Sale saved = saleRepository.save(s);
        if (customer != null) {
            commerceService.recalculateCustomerBalances(customer);
        }

        auditLogService.record(actor, saved.getFarmId(), "SALE_RECORDED", "SALE", saved.getId(),
                "Invoice #" + saved.getInvoiceNumber() + " (" + saved.getSaleCategory() + "): ₹" + totalAmt);
        return saved;
    }

    // =========================================================================
    // 2. OPERATING EXPENSES LEDGER
    // =========================================================================
    public List<Expense> listExpenses(String farmId) {
        User user = farmAccessGuard.getCurrentUser();
        if (user.getRole() == Role.RETAILER || user.getRole() == Role.CHICKEN_SHOP_OWNER) {
            return expenseRepository.findByOwnerUserIdOrderByExpenseDateDescCreatedAtDesc(user.getId());
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return expenseRepository.findByFarmIdOrderByExpenseDateDescCreatedAtDesc(farmId);
        }
        if (user.getRole() == Role.SUPER_ADMIN) {
            return expenseRepository.findAll();
        }
        if (user.getRole().isFarmRole() && user.getAssignedFarmIds() != null && !user.getAssignedFarmIds().isEmpty()) {
            return expenseRepository.findByFarmIdInOrderByExpenseDateDescCreatedAtDesc(user.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public Expense recordExpense(ExpenseRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Expense e = new Expense();
        e.setExpenseDate(req.getExpenseDate());
        e.setOwnerUserId(actor.getId());
        e.setExpenseCategory(req.getExpenseCategory().trim().toUpperCase());
        e.setAmountInr(round2(req.getAmountInr()));
        e.setPaidAmountInr(round2(req.getAmountInr()));
        e.setPaymentMode(req.getPaymentMode() != null ? req.getPaymentMode().toUpperCase() : "CASH");
        e.setPayeeName(req.getPayeeName());
        e.setDescription(req.getDescription());
        e.setRecordedBy(actor.getUsername());

        if (actor.getRole() == Role.RETAILER) {
            e.setBusinessContext("RETAIL");
            RetailerStore store = commerceService.getOrCreateCurrentRetailStore();
            e.setRetailStoreId(store.getId());
            store.setTotalOperatingExpensesInr(round2(store.getTotalOperatingExpensesInr() + e.getAmountInr()));
            retailerRepository.save(store);
        } else if (actor.getRole() == Role.CHICKEN_SHOP_OWNER) {
            e.setBusinessContext("CHICKEN_SHOP");
            ChickenShop shop = commerceService.getOrCreateCurrentChickenShop();
            e.setChickenShopId(shop.getId());
        } else {
            e.setBusinessContext("FARM");
            if (req.getBatchId() != null && !req.getBatchId().isBlank()) {
                Batch b = batchService.getBatchDocumentWithAccess(req.getBatchId());
                farmAccessGuard.requireFarmWriteAccess(b.getFarmId());
                e.setFarmId(b.getFarmId());
                e.setFarmName(b.getFarmName());
                e.setBatchId(b.getId());
                e.setBatchCode(b.getBatchCode());
            } else if (req.getFarmId() != null && !req.getFarmId().isBlank()) {
                farmAccessGuard.requireFarmWriteAccess(req.getFarmId());
                Farm f = farmRepository.findById(req.getFarmId())
                        .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + req.getFarmId()));
                e.setFarmId(f.getId());
                e.setFarmName(f.getFarmName());
            } else {
                throw new BadRequestException("Farm ID or Batch ID is required for farm expenses.");
            }
        }

        Expense saved = expenseRepository.save(e);
        auditLogService.record(actor, saved.getFarmId(), "EXPENSE_RECORDED", "EXPENSE", saved.getId(),
                "Recorded " + saved.getExpenseCategory() + " expense of ₹" + saved.getAmountInr());
        return saved;
    }

    // =========================================================================
    // 3. PAYMENTS SETTLEMENT LEDGER (CUSTOMER RECEIPTS & SUPPLIER PAYMENTS)
    // =========================================================================
    public List<PaymentRecord> listPayments(String farmId) {
        User user = farmAccessGuard.getCurrentUser();
        if (user.getRole() == Role.RETAILER || user.getRole() == Role.CHICKEN_SHOP_OWNER) {
            return paymentRepository.findByOwnerUserIdOrderByPaymentDateDescCreatedAtDesc(user.getId());
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return paymentRepository.findByFarmIdOrderByPaymentDateDescCreatedAtDesc(farmId);
        }
        if (user.getRole() == Role.SUPER_ADMIN) {
            return paymentRepository.findAll();
        }
        if (user.getRole().isFarmRole() && user.getAssignedFarmIds() != null && !user.getAssignedFarmIds().isEmpty()) {
            return paymentRepository.findByFarmIdInOrderByPaymentDateDescCreatedAtDesc(user.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public PaymentRecord recordPaymentSettlement(PaymentRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        String pType = req.getPaymentType().trim().toUpperCase();
        double amt = round2(req.getAmountInr());

        PaymentRecord pr = new PaymentRecord();
        pr.setPaymentDate(req.getPaymentDate());
        pr.setOwnerUserId(actor.getId());
        pr.setPaymentType(pType);
        pr.setAmountInr(amt);
        pr.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod().toUpperCase() : "UPI");
        pr.setTransactionRef(req.getTransactionRef());
        pr.setRemarks(req.getRemarks());
        pr.setRecordedBy(actor.getUsername());

        if (actor.getRole() == Role.RETAILER) pr.setBusinessContext("RETAIL");
        else if (actor.getRole() == Role.CHICKEN_SHOP_OWNER) pr.setBusinessContext("CHICKEN_SHOP");
        else pr.setBusinessContext("FARM");

        if ("CUSTOMER_RECEIPT".equals(pType)) {
            if (req.getCustomerId() == null || req.getCustomerId().isBlank()) {
                throw new BadRequestException("Customer ID is required for CUSTOMER_RECEIPT.");
            }
            Customer cust = customerRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
            pr.setFarmId(cust.getFarmId());
            pr.setFarmName(cust.getFarmName());
            pr.setCustomerId(cust.getId());
            pr.setCustomerName(cust.getCustomerName());

            if (req.getRelatedInvoiceId() != null && !req.getRelatedInvoiceId().isBlank()) {
                saleRepository.findById(req.getRelatedInvoiceId()).ifPresent(sale -> {
                    double newCash = Math.min(sale.getTotalSaleAmountInr(), round2(sale.getCashReceivedInr() + amt));
                    double newOut = round2(Math.max(0.0, sale.getTotalSaleAmountInr() - newCash));
                    sale.setCashReceivedInr(newCash);
                    sale.setOutstandingBalanceInr(newOut);
                    sale.setPaymentStatus(newOut <= 0 ? "PAID" : "PARTIAL");
                    saleRepository.save(sale);
                    pr.setRelatedInvoiceId(sale.getId());
                    pr.setRelatedInvoiceNumber(sale.getInvoiceNumber());
                });
            }
            PaymentRecord saved = paymentRepository.save(pr);
            commerceService.recalculateCustomerBalances(cust);

            auditLogService.record(actor, saved.getFarmId(), "CUSTOMER_PAYMENT_RECEIVED", "PAYMENT", saved.getId(),
                    "Received ₹" + amt + " from customer " + cust.getCustomerName());
            return saved;
        } else if ("SUPPLIER_PAYMENT".equals(pType)) {
            if (req.getSupplierId() == null || req.getSupplierId().isBlank()) {
                throw new BadRequestException("Supplier ID is required for SUPPLIER_PAYMENT.");
            }
            Supplier sup = supplierRepository.findById(req.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found."));
            if (sup.getFarmId() != null) {
                farmAccessGuard.requireFarmWriteAccess(sup.getFarmId());
            }
            pr.setFarmId(sup.getFarmId());
            pr.setFarmName(sup.getFarmName());
            pr.setSupplierId(sup.getId());
            pr.setSupplierName(sup.getSupplierName());

            // Allocate payment across unpaid purchase invoices for this supplier
            double remainingToAllocate = amt;
            List<Purchase> supPurchases = purchaseRepository.findBySupplierIdOrderByPurchaseDateDesc(sup.getId());
            for (Purchase p : supPurchases) {
                if (remainingToAllocate <= 0) break;
                if (p.getBalancePayable() > 0) {
                    double apply = Math.min(p.getBalancePayable(), remainingToAllocate);
                    p.setAmountPaid(round2(p.getAmountPaid() + apply));
                    p.setBalancePayable(round2(Math.max(0.0, p.getTotalInvoiceAmount() - p.getAmountPaid())));
                    p.setPaymentStatus(p.getBalancePayable() <= 0 ? "PAID" : "PARTIAL");
                    purchaseRepository.save(p);
                    remainingToAllocate = round2(remainingToAllocate - apply);
                }
            }
            supplierPurchaseService.recalculateSupplierTotals(sup);
            PaymentRecord saved = paymentRepository.save(pr);

            auditLogService.record(actor, saved.getFarmId(), "SUPPLIER_PAYMENT_SENT", "PAYMENT", saved.getId(),
                    "Paid ₹" + amt + " to supplier " + sup.getSupplierName());
            return saved;
        } else {
            throw new BadRequestException("paymentType must be CUSTOMER_RECEIPT or SUPPLIER_PAYMENT.");
        }
    }

    // =========================================================================
    // 4. NON-DOUBLE-COUNTING FINANCIAL PROFIT/LOSS ENGINE (DAILY / BATCH / FARM / MONTHLY / YEARLY)
    // =========================================================================
    public Map<String, Object> calculateFinancialReport(String farmId, String batchId, LocalDate fromDate, LocalDate toDate) {
        User user = farmAccessGuard.getCurrentUser();

        if (user.getRole() == Role.RETAILER) {
            return commerceService.getRetailDashboardData();
        }
        if (user.getRole() == Role.CHICKEN_SHOP_OWNER) {
            return commerceService.getChickenShopDashboardData();
        }

        List<String> targetFarmIds;
        if (batchId != null && !batchId.isBlank()) {
            Batch b = batchService.getBatchDocumentWithAccess(batchId);
            targetFarmIds = List.of(b.getFarmId());
        } else if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            targetFarmIds = List.of(farmId);
        } else if (user.getRole() == Role.SUPER_ADMIN) {
            targetFarmIds = farmRepository.findAll().stream().map(Farm::getId).toList();
        } else if (user.getRole().isFarmRole() && user.getAssignedFarmIds() != null) {
            targetFarmIds = user.getAssignedFarmIds();
        } else {
            targetFarmIds = Collections.emptyList();
        }

        LocalDate start = fromDate != null ? fromDate : LocalDate.now().minusYears(5);
        LocalDate end = toDate != null ? toDate : LocalDate.now().plusDays(1);

        List<Batch> batches = batchRepository.findByFarmIdInOrderByChickArrivalDateDesc(targetFarmIds).stream()
                .filter(b -> batchId == null || batchId.isBlank() || batchId.equals(b.getId()))
                .toList();
        Map<String, Batch> batchMap = batches.stream().collect(Collectors.toMap(Batch::getId, b -> b));

        // 1. Chick Purchase Expenses (from Batches whose purchaseDate falls in period, or always included for single-batch report)
        double chickExpenses = 0.0;
        for (Batch b : batches) {
            boolean inWindow = (batchId != null && !batchId.isBlank())
                    || (b.getPurchaseDate() != null && !b.getPurchaseDate().isBefore(start) && !b.getPurchaseDate().isAfter(end));
            if (inWindow) {
                chickExpenses += (b.getStartingBirdQuantity() * b.getPurchasePricePerChick());
            }
        }
        chickExpenses = round2(chickExpenses);

        // 2. Feed Expenses Consumed (NEVER double-counts Stock Purchases OR overlapping FeedRecord + DailyRecord!)
        List<FeedRecord> feedRecords = feedRepository.findByFarmIdInOrderByRecordDateDescCreatedAtDesc(targetFarmIds).stream()
                .filter(f -> batchId == null || batchId.isBlank() || batchId.equals(f.getBatchId()))
                .filter(f -> !f.getRecordDate().isBefore(start) && !f.getRecordDate().isAfter(end))
                .toList();
        Set<String> batchDatesCoveredByFeedRecord = new HashSet<>();
        double feedExpenses = 0.0;
        for (FeedRecord fr : feedRecords) {
            feedExpenses += fr.getTotalFeedCostInr();
            batchDatesCoveredByFeedRecord.add(fr.getBatchId() + "|" + fr.getRecordDate());
        }

        List<DailyRecord> dailyRecords = dailyRecordRepository.findByFarmIdInOrderByRecordDateDesc(targetFarmIds).stream()
                .filter(d -> batchId == null || batchId.isBlank() || batchId.equals(d.getBatchId()))
                .filter(d -> !d.getRecordDate().isBefore(start) && !d.getRecordDate().isAfter(end))
                .toList();

        double dailyLabour = 0.0;
        double dailyElectricity = 0.0;
        double dailyOther = 0.0;

        for (DailyRecord dr : dailyRecords) {
            if (!batchDatesCoveredByFeedRecord.contains(dr.getBatchId() + "|" + dr.getRecordDate())) {
                Batch b = batchMap.get(dr.getBatchId());
                double rate = b != null ? b.getEstimatedFeedRatePerKg() : 40.0;
                feedExpenses += (dr.getFeedConsumptionKg() * rate);
            }
            dailyLabour += dr.getLabourCost();
            dailyElectricity += dr.getElectricityCost();
            dailyOther += dr.getOtherExpenses();
        }
        feedExpenses = round2(feedExpenses);

        // 3. Medicine & Vaccination Consumed Expenses (NEVER double-counts unconsumed cabinet purchases!)
        double medExpenses = medicineRepository.findByFarmIdInOrderByAdministrationDateDescCreatedAtDesc(targetFarmIds).stream()
                .filter(m -> batchId == null || batchId.isBlank() || batchId.equals(m.getBatchId()))
                .filter(m -> !m.getAdministrationDate().isBefore(start) && !m.getAdministrationDate().isAfter(end))
                .mapToDouble(MedicineRecord::getTotalMedicineCostInr).sum();

        double vaxExpenses = vaccinationRepository.findByFarmIdInOrderByScheduledDateAsc(targetFarmIds).stream()
                .filter(v -> "COMPLETED".equalsIgnoreCase(v.getStatus()))
                .filter(v -> batchId == null || batchId.isBlank() || batchId.equals(v.getBatchId()))
                .filter(v -> v.getAdministeredDate() != null && !v.getAdministeredDate().isBefore(start) && !v.getAdministeredDate().isAfter(end))
                .mapToDouble(VaccinationRecord::getTotalVaccineCostInr).sum();
        double medicineAndVaccineExpenses = round2(medExpenses + vaxExpenses);

        // 4. Explicit Operating Expenses (Labour, Electricity, Transportation, Equipment, Other — excluding stockPurchaseOnly!)
        List<Expense> explicitExpenses = expenseRepository.findByFarmIdInOrderByExpenseDateDescCreatedAtDesc(targetFarmIds).stream()
                .filter(e -> !e.isStockPurchaseOnly())
                .filter(e -> batchId == null || batchId.isBlank() || batchId.equals(e.getBatchId()))
                .filter(e -> !e.getExpenseDate().isBefore(start) && !e.getExpenseDate().isAfter(end))
                .toList();

        double labourExpenses = dailyLabour;
        double electricityExpenses = dailyElectricity;
        double transportExpenses = 0.0;
        double equipmentExpenses = 0.0;
        double otherExpenses = dailyOther;

        for (Expense e : explicitExpenses) {
            String cat = e.getExpenseCategory() != null ? e.getExpenseCategory().toUpperCase() : "OTHER";
            switch (cat) {
                case "LABOUR" -> labourExpenses += e.getAmountInr();
                case "ELECTRICITY" -> electricityExpenses += e.getAmountInr();
                case "TRANSPORTATION" -> transportExpenses += e.getAmountInr();
                case "EQUIPMENT" -> equipmentExpenses += e.getAmountInr();
                case "FEED" -> feedExpenses = round2(feedExpenses + e.getAmountInr());
                case "MEDICINE", "VACCINE" -> medicineAndVaccineExpenses = round2(medicineAndVaccineExpenses + e.getAmountInr());
                default -> otherExpenses += e.getAmountInr();
            }
        }
        labourExpenses = round2(labourExpenses);
        electricityExpenses = round2(electricityExpenses);
        transportExpenses = round2(transportExpenses);
        equipmentExpenses = round2(equipmentExpenses);
        otherExpenses = round2(otherExpenses);

        double totalExpenses = round2(chickExpenses + feedExpenses + medicineAndVaccineExpenses
                + labourExpenses + electricityExpenses + transportExpenses + equipmentExpenses + otherExpenses);

        // 5. Revenue Breakdown (Chicken Sales, Egg Sales, Meat Sales, Manure & By-Products — Deduplicated with Batch Counters!)
        List<Sale> farmSales = saleRepository.findByFarmIdInOrderBySaleDateDescCreatedAtDesc(targetFarmIds).stream()
                .filter(s -> batchId == null || batchId.isBlank() || batchId.equals(s.getBatchId()))
                .filter(s -> !s.getSaleDate().isBefore(start) && !s.getSaleDate().isAfter(end))
                .toList();

        double chickenSales = 0.0;
        double eggSales = 0.0;
        double meatSales = 0.0;
        double manureByProductSales = 0.0;
        double invoicedCashReceived = 0.0;

        Map<String, Double> invoicedBirdByBatch = new LinkedHashMap<>();
        Map<String, Double> invoicedEggByBatch = new LinkedHashMap<>();

        for (Sale s : farmSales) {
            double amt = s.getTotalSaleAmountInr();
            invoicedCashReceived += s.getCashReceivedInr();
            String sc = s.getSaleCategory() != null ? s.getSaleCategory().toUpperCase() : "";
            if ("LIVE_CHICKEN_SALE".equals(sc)) {
                chickenSales += amt;
                if (s.getBatchId() != null) {
                    invoicedBirdByBatch.put(s.getBatchId(), invoicedBirdByBatch.getOrDefault(s.getBatchId(), 0.0) + amt);
                }
            } else if ("EGG_SALE".equals(sc)) {
                eggSales += amt;
                if (s.getBatchId() != null) {
                    invoicedEggByBatch.put(s.getBatchId(), invoicedEggByBatch.getOrDefault(s.getBatchId(), 0.0) + amt);
                }
            } else if ("DRESSED_MEAT_SALE".equals(sc)) {
                meatSales += amt;
            } else {
                manureByProductSales += amt;
            }
        }

        // Include historical non-invoiced batch bird/egg sales (if full period covers batch) without double-counting invoiced sales!
        boolean isWideOrBatchReport = fromDate == null || !fromDate.isAfter(LocalDate.now().minusDays(30));
        if (isWideOrBatchReport) {
            for (Batch b : batches) {
                double unInvoicedBird = Math.max(0.0, b.getTotalBirdSalesRevenue() - invoicedBirdByBatch.getOrDefault(b.getId(), 0.0));
                double unInvoicedEgg = Math.max(0.0, b.getTotalEggSalesRevenue() - invoicedEggByBatch.getOrDefault(b.getId(), 0.0));
                chickenSales += unInvoicedBird;
                eggSales += unInvoicedEgg;
                invoicedCashReceived += (unInvoicedBird + unInvoicedEgg);
            }
        }

        chickenSales = round2(chickenSales);
        eggSales = round2(eggSales);
        meatSales = round2(meatSales);
        manureByProductSales = round2(manureByProductSales);
        double totalRevenue = round2(chickenSales + eggSales + meatSales + manureByProductSales);
        double netProfitLoss = round2(totalRevenue - totalExpenses);

        // 6. Separate Cash Received, Customer Outstanding Receivables & Supplier Payments/Payables
        List<Customer> customers = customerRepository.findByFarmIdInOrderByCustomerNameAsc(targetFarmIds);
        double customerOutstanding = round2(customers.stream().mapToDouble(Customer::getOutstandingBalance).sum());

        List<Supplier> suppliers = supplierRepository.findByFarmIdInOrderBySupplierNameAsc(targetFarmIds);
        double supplierPaymentsPaid = round2(suppliers.stream().mapToDouble(Supplier::getTotalPaidAmount).sum());
        double supplierOutstandingPayables = round2(suppliers.stream().mapToDouble(Supplier::getOutstandingPayable).sum());

        List<Purchase> stockPurchases = purchaseRepository.findByFarmIdInOrderByPurchaseDateDescCreatedAtDesc(targetFarmIds);
        double inventoryStockProcurementInr = round2(stockPurchases.stream().mapToDouble(Purchase::getTotalInvoiceAmount).sum());

        // 7. Monthly & Yearly Breakdown
        Map<YearMonth, Double> revByMonth = new LinkedHashMap<>();
        Map<YearMonth, Double> expByMonth = new LinkedHashMap<>();
        for (Sale s : farmSales) {
            YearMonth ym = YearMonth.from(s.getSaleDate());
            revByMonth.put(ym, round2(revByMonth.getOrDefault(ym, 0.0) + s.getTotalSaleAmountInr()));
        }
        for (FeedRecord fr : feedRecords) {
            YearMonth ym = YearMonth.from(fr.getRecordDate());
            expByMonth.put(ym, round2(expByMonth.getOrDefault(ym, 0.0) + fr.getTotalFeedCostInr()));
        }
        for (Expense ex : explicitExpenses) {
            YearMonth ym = YearMonth.from(ex.getExpenseDate());
            expByMonth.put(ym, round2(expByMonth.getOrDefault(ym, 0.0) + ex.getAmountInr()));
        }
        Set<YearMonth> allMonths = new HashSet<>(revByMonth.keySet());
        allMonths.addAll(expByMonth.keySet());
        List<Map<String, Object>> monthlyReports = allMonths.stream().sorted().map(ym -> {
            double r = revByMonth.getOrDefault(ym, 0.0);
            double e = expByMonth.getOrDefault(ym, 0.0);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", ym.toString());
            m.put("revenueInr", r);
            m.put("expensesInr", e);
            m.put("profitLossInr", round2(r - e));
            return m;
        }).toList();

        // Also include Super Admin visibility into Retail & Chicken Shop P&L
        double retailProfit = 0.0;
        double chickenShopProfit = 0.0;
        if (user.getRole() == Role.SUPER_ADMIN) {
            for (RetailerStore rs : retailerRepository.findAll()) {
                retailProfit += (rs.getTotalChickenSalesRevenueInr() + rs.getTotalEggSalesRevenueInr()
                        - rs.getTotalStockPurchaseCostInr() - rs.getTotalOperatingExpensesInr());
            }
            for (ChickenShop cs : chickenShopRepository.findAll()) {
                chickenShopProfit += (cs.getTotalMeatSalesRevenueInr() + cs.getTotalByProductSalesRevenueInr()
                        - cs.getTotalLivePurchaseCostInr() - cs.getTotalProcessingExpensesInr() - cs.getTotalOtherAllocatedExpensesInr());
            }
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("periodFrom", start);
        report.put("periodTo", end);
        // Revenue breakdown
        report.put("chickenSalesInr", chickenSales);
        report.put("eggSalesInr", eggSales);
        report.put("meatSalesInr", meatSales);
        report.put("manureAndByProductSalesInr", manureByProductSales);
        report.put("totalRevenueInr", totalRevenue);
        // Expense breakdown (Accrual consumption — never double-counted with stock purchases!)
        report.put("chickPurchaseExpensesInr", chickExpenses);
        report.put("feedExpensesInr", feedExpenses);
        report.put("medicineExpensesInr", medicineAndVaccineExpenses);
        report.put("labourExpensesInr", labourExpenses);
        report.put("electricityExpensesInr", electricityExpenses);
        report.put("transportationExpensesInr", transportExpenses);
        report.put("equipmentExpensesInr", equipmentExpenses);
        report.put("otherExpensesInr", otherExpenses);
        report.put("totalExpensesInr", totalExpenses);
        // Net Profit / Loss
        report.put("netProfitLossInr", netProfitLoss);
        report.put("profitable", netProfitLoss >= 0);
        // Separated Cashflow, Receivables & Payables
        report.put("cashReceivedInr", round2(invoicedCashReceived));
        report.put("customerOutstandingBalancesInr", customerOutstanding);
        report.put("supplierPaymentsPaidInr", supplierPaymentsPaid);
        report.put("supplierOutstandingPayablesInr", supplierOutstandingPayables);
        report.put("inventoryStockPurchasesExcludedFromDoubleCountInr", inventoryStockProcurementInr);
        report.put("retailBusinessProfitLossInr", round2(retailProfit));
        report.put("chickenShopProfitLossInr", round2(chickenShopProfit));
        report.put("monthlyBreakdown", monthlyReports);
        return report;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
