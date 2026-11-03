package com.poultry.smartfarm.commerce.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.commerce.document.ChickenShop;
import com.poultry.smartfarm.commerce.document.Customer;
import com.poultry.smartfarm.commerce.document.Expense;
import com.poultry.smartfarm.commerce.document.PaymentRecord;
import com.poultry.smartfarm.commerce.document.ProcessingRecord;
import com.poultry.smartfarm.commerce.document.RetailerStore;
import com.poultry.smartfarm.commerce.document.Sale;
import com.poultry.smartfarm.commerce.document.WholesalerDeal;
import com.poultry.smartfarm.commerce.dto.ChickenShopProcessRequest;
import com.poultry.smartfarm.commerce.dto.CustomerRequest;
import com.poultry.smartfarm.commerce.dto.RetailStockPurchaseRequest;
import com.poultry.smartfarm.commerce.dto.WholesalerDealRequest;
import com.poultry.smartfarm.commerce.repository.ChickenShopRepository;
import com.poultry.smartfarm.commerce.repository.CustomerRepository;
import com.poultry.smartfarm.commerce.repository.ExpenseRepository;
import com.poultry.smartfarm.commerce.repository.PaymentRecordRepository;
import com.poultry.smartfarm.commerce.repository.ProcessingRecordRepository;
import com.poultry.smartfarm.commerce.repository.RetailerStoreRepository;
import com.poultry.smartfarm.commerce.repository.SaleRepository;
import com.poultry.smartfarm.commerce.repository.WholesalerDealRepository;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommerceAndShopService {

    private final CustomerRepository customerRepository;
    private final WholesalerDealRepository wholesalerRepository;
    private final RetailerStoreRepository retailerRepository;
    private final ChickenShopRepository chickenShopRepository;
    private final ProcessingRecordRepository processingRepository;
    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRecordRepository paymentRepository;
    private final BatchService batchService;
    private final BatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public CommerceAndShopService(CustomerRepository customerRepository,
                                  WholesalerDealRepository wholesalerRepository,
                                  RetailerStoreRepository retailerRepository,
                                  ChickenShopRepository chickenShopRepository,
                                  ProcessingRecordRepository processingRepository,
                                  SaleRepository saleRepository,
                                  ExpenseRepository expenseRepository,
                                  PaymentRecordRepository paymentRepository,
                                  BatchService batchService,
                                  BatchRepository batchRepository,
                                  FarmRepository farmRepository,
                                  FarmAccessGuard farmAccessGuard,
                                  AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.wholesalerRepository = wholesalerRepository;
        this.retailerRepository = retailerRepository;
        this.chickenShopRepository = chickenShopRepository;
        this.processingRepository = processingRepository;
        this.saleRepository = saleRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.batchService = batchService;
        this.batchRepository = batchRepository;
        this.farmRepository = farmRepository;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    // =========================================================================
    // 1. CUSTOMER MASTER (ALL 5 BUYER CATEGORIES)
    // =========================================================================
    public List<Customer> listCustomers(String farmId) {
        User user = farmAccessGuard.getCurrentUser();
        if (user.getRole() == Role.RETAILER || user.getRole() == Role.CHICKEN_SHOP_OWNER) {
            return customerRepository.findByOwnerUserIdOrderByCustomerNameAsc(user.getId());
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return customerRepository.findByFarmIdOrderByCustomerNameAsc(farmId);
        }
        if (user.getRole() == Role.SUPER_ADMIN) {
            return customerRepository.findAll();
        }
        if (user.getRole().isFarmRole() && user.getAssignedFarmIds() != null && !user.getAssignedFarmIds().isEmpty()) {
            return customerRepository.findByFarmIdInOrderByCustomerNameAsc(user.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public Customer createCustomer(CustomerRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        String code = req.getCustomerCode().trim().toUpperCase();
        if (customerRepository.existsByCustomerCodeIgnoreCase(code)) {
            throw new BadRequestException("Customer code '" + code + "' already exists.");
        }

        Customer c = new Customer();
        c.setCustomerCode(code);
        c.setCustomerName(req.getCustomerName().trim());
        c.setBuyerCategory(req.getBuyerCategory().trim().toUpperCase());
        c.setPhone(req.getPhone().trim());
        c.setEmail(req.getEmail());
        c.setGstNumber(req.getGstNumber());
        c.setAddress(req.getAddress());
        c.setCity(req.getCity());
        c.setState(req.getState());
        c.setNotes(req.getNotes());

        if (actor.getRole() == Role.RETAILER) {
            c.setBusinessContext("RETAIL");
            c.setOwnerUserId(actor.getId());
        } else if (actor.getRole() == Role.CHICKEN_SHOP_OWNER) {
            c.setBusinessContext("CHICKEN_SHOP");
            c.setOwnerUserId(actor.getId());
        } else {
            if (req.getFarmId() == null || req.getFarmId().isBlank()) {
                throw new BadRequestException("Farm ID is required when adding a farm customer/buyer.");
            }
            farmAccessGuard.requireFarmWriteAccess(req.getFarmId());
            Farm farm = farmRepository.findById(req.getFarmId())
                    .orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + req.getFarmId()));
            c.setBusinessContext("FARM");
            c.setFarmId(farm.getId());
            c.setFarmName(farm.getFarmName());
            c.setOwnerUserId(actor.getId());
        }

        Customer saved = customerRepository.save(c);
        auditLogService.record(actor, saved.getFarmId(), "CUSTOMER_CREATED", "CUSTOMER", saved.getId(),
                "Added buyer " + saved.getCustomerName() + " (" + saved.getBuyerCategory() + ")");
        return saved;
    }

    public void recalculateCustomerBalances(Customer customer) {
        if (customer == null || customer.getId() == null) return;
        List<Sale> sales = saleRepository.findByCustomerIdOrderBySaleDateDesc(customer.getId());
        double totalBilled = round2(sales.stream().mapToDouble(Sale::getTotalSaleAmountInr).sum());
        double receivedOnInvoices = sales.stream().mapToDouble(Sale::getCashReceivedInr).sum();

        // Include any standalone customer receipts not linked to a specific invoice (linked ones already increment sale.cashReceivedInr)
        List<PaymentRecord> standaloneReceipts = paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customer.getId())
                .stream()
                .filter(p -> "CUSTOMER_RECEIPT".equalsIgnoreCase(p.getPaymentType()))
                .filter(p -> p.getRelatedInvoiceId() == null || p.getRelatedInvoiceId().isBlank())
                .toList();
        double unallocatedReceipts = standaloneReceipts.stream().mapToDouble(PaymentRecord::getAmountInr).sum();

        double totalReceived = round2(receivedOnInvoices + unallocatedReceipts);
        double outstanding = round2(Math.max(0.0, totalBilled - totalReceived));

        customer.setTotalBilledAmount(totalBilled);
        customer.setTotalReceivedAmount(totalReceived);
        customer.setOutstandingBalance(outstanding);
        customerRepository.save(customer);
    }

    // =========================================================================
    // 2. WHOLESALE LIVE BIRD DISPATCH MODULE
    // =========================================================================
    public List<WholesalerDeal> listWholesaleDeals(String farmId) {
        User user = farmAccessGuard.getCurrentUser();
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return wholesalerRepository.findByFarmIdOrderByDealDateDescCreatedAtDesc(farmId);
        }
        if (user.getRole() == Role.SUPER_ADMIN) {
            return wholesalerRepository.findAll();
        }
        if (user.getRole().isFarmRole() && user.getAssignedFarmIds() != null && !user.getAssignedFarmIds().isEmpty()) {
            return wholesalerRepository.findByFarmIdInOrderByDealDateDescCreatedAtDesc(user.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public WholesalerDeal recordWholesaleDeal(WholesalerDealRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(req.getBatchId());
        farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());
        batchService.ensureBatchEditable(batch);

        String dealNo = req.getDealNumber().trim().toUpperCase();
        if (wholesalerRepository.existsByDealNumberIgnoreCase(dealNo)) {
            throw new BadRequestException("Wholesale deal number '" + dealNo + "' already exists.");
        }

        BatchResponse view = BatchResponse.fromDocument(batch);
        if (req.getLiveBirdQuantity() > view.getCurrentLiveBirds()) {
            throw new BadRequestException("Insufficient live birds in batch " + batch.getBatchCode()
                    + ": Available = " + view.getCurrentLiveBirds() + ", Requested = " + req.getLiveBirdQuantity());
        }

        Customer buyer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Wholesale customer not found: " + req.getCustomerId()));

        double avgWtKg = round2(req.getTotalLiveWeightKg() / req.getLiveBirdQuantity());
        double basePurchaseAmt = round2((req.getTotalLiveWeightKg() * req.getPurchasePricePerKg()) + req.getTransportCostInr());
        double totalSaleAmt = round2(req.getTotalLiveWeightKg() * req.getSellingPricePerKg());
        double received = round2(req.getCashReceivedInr());
        if (received > totalSaleAmt) {
            throw new BadRequestException("Cash received (" + received + ") cannot exceed wholesale invoice amount (" + totalSaleAmt + ").");
        }
        double outstanding = round2(totalSaleAmt - received);
        String payStatus = outstanding <= 0.0 ? "PAID" : (received > 0.0 ? "PARTIAL" : "CREDIT_PENDING");

        // 1. Update Batch live bird & sales counters
        batch.setTotalBirdsSold(batch.getTotalBirdsSold() + req.getLiveBirdQuantity());
        batch.setTotalSoldLiveWeightKg(round2(batch.getTotalSoldLiveWeightKg() + req.getTotalLiveWeightKg()));
        batch.setTotalBirdSalesRevenue(round2(batch.getTotalBirdSalesRevenue() + totalSaleAmt));
        batch.setLiveSellingPricePerKg(req.getSellingPricePerKg());
        batchRepository.save(batch);
        batchService.syncHouseOccupancy(batch.getPoultryHouseId());

        // 2. Create synchronized Sale Invoice in `sales` collection
        Sale sale = new Sale();
        sale.setInvoiceNumber("WS-" + dealNo);
        sale.setSaleDate(req.getDealDate());
        sale.setBusinessContext("FARM");
        sale.setFarmId(batch.getFarmId());
        sale.setFarmName(batch.getFarmName());
        sale.setBatchId(batch.getId());
        sale.setBatchCode(batch.getBatchCode());
        sale.setOwnerUserId(actor.getId());
        sale.setSaleCategory("LIVE_CHICKEN_SALE");
        sale.setProductDescription("Wholesale Live Birds (" + req.getLiveBirdQuantity() + " birds, avg " + avgWtKg + " kg)");
        sale.setBuyerCategory(buyer.getBuyerCategory());
        sale.setCustomerId(buyer.getId());
        sale.setCustomerName(buyer.getCustomerName());
        sale.setCustomerPhone(buyer.getPhone());
        sale.setQuantity((double) req.getLiveBirdQuantity());
        sale.setUnit("BIRDS");
        sale.setLiveWeightKg(round2(req.getTotalLiveWeightKg()));
        sale.setUnitSellingPriceInr(round2(req.getSellingPricePerKg()));
        sale.setTotalSaleAmountInr(totalSaleAmt);
        sale.setCashReceivedInr(received);
        sale.setOutstandingBalanceInr(outstanding);
        sale.setPaymentStatus(payStatus);
        sale.setPaymentMode(outstanding > 0 ? "CREDIT" : "NEFT_RTGS");
        sale.setRemarks(req.getRemarks());
        sale.setRecordedBy(actor.getUsername());
        Sale savedSale = saleRepository.save(sale);

        // 3. Log Farm Transport Expense if farm paid transport > 0
        if (req.getTransportCostInr() > 0) {
            Expense trExp = new Expense();
            trExp.setExpenseDate(req.getDealDate());
            trExp.setBusinessContext("FARM");
            trExp.setFarmId(batch.getFarmId());
            trExp.setFarmName(batch.getFarmName());
            trExp.setBatchId(batch.getId());
            trExp.setBatchCode(batch.getBatchCode());
            trExp.setOwnerUserId(actor.getId());
            trExp.setExpenseCategory("TRANSPORTATION");
            trExp.setAmountInr(round2(req.getTransportCostInr()));
            trExp.setPaidAmountInr(round2(req.getTransportCostInr()));
            trExp.setPayeeName(req.getVehicleNumber() != null ? "Vehicle " + req.getVehicleNumber() : "Wholesale Transport");
            trExp.setDescription("Transport for Wholesale Deal #" + dealNo);
            trExp.setRecordedBy(actor.getUsername());
            expenseRepository.save(trExp);
        }

        // 4. Save WholesalerDeal record
        WholesalerDeal deal = new WholesalerDeal();
        deal.setFarmId(batch.getFarmId());
        deal.setFarmName(batch.getFarmName());
        deal.setBatchId(batch.getId());
        deal.setBatchCode(batch.getBatchCode());
        deal.setDealNumber(dealNo);
        deal.setDealDate(req.getDealDate());
        deal.setCustomerId(buyer.getId());
        deal.setCustomerName(buyer.getCustomerName());
        deal.setCustomerPhone(buyer.getPhone());
        deal.setBuyerCategory(buyer.getBuyerCategory());
        deal.setLiveBirdQuantity(req.getLiveBirdQuantity());
        deal.setAverageWeightKg(avgWtKg);
        deal.setTotalLiveWeightKg(round2(req.getTotalLiveWeightKg()));
        deal.setPurchasePricePerKg(round2(req.getPurchasePricePerKg()));
        deal.setTransportCostInr(round2(req.getTransportCostInr()));
        deal.setTotalPurchaseAmountInr(basePurchaseAmt);
        deal.setSellingPricePerKg(round2(req.getSellingPricePerKg()));
        deal.setTotalSaleAmountInr(totalSaleAmt);
        deal.setCashReceivedInr(received);
        deal.setOutstandingPaymentInr(outstanding);
        deal.setPaymentStatus(payStatus);
        deal.setVehicleNumber(req.getVehicleNumber());
        deal.setLinkedSaleId(savedSale.getId());
        deal.setRemarks(req.getRemarks());
        deal.setRecordedBy(actor.getUsername());
        WholesalerDeal saved = wholesalerRepository.save(deal);

        recalculateCustomerBalances(buyer);

        auditLogService.record(actor, batch.getFarmId(), "WHOLESALE_DISPATCH", "WHOLESALER_DEAL", saved.getId(),
                "Dispatched " + req.getLiveBirdQuantity() + " birds (" + req.getTotalLiveWeightKg() + " kg) to "
                        + buyer.getCustomerName() + " for ₹" + totalSaleAmt);
        return saved;
    }

    // =========================================================================
    // 3. RETAILER BUSINESS MODULE (CHICKENS & EGGS STOCK + SALES)
    // =========================================================================
    public RetailerStore getOrCreateCurrentRetailStore() {
        User user = farmAccessGuard.getCurrentUser();
        if (user.getRole() == Role.SUPER_ADMIN) {
            return retailerRepository.findAll().stream().findFirst().orElseGet(() -> createDefaultRetailStore(user));
        }
        if (user.getRole() != Role.RETAILER && user.getRole() != Role.FARM_ADMIN) {
            throw new AccessDeniedException("Retail store access requires Retailer, Farm Admin, or Super Admin role.");
        }
        return retailerRepository.findByOwnerUserId(user.getId()).orElseGet(() -> createDefaultRetailStore(user));
    }

    private RetailerStore createDefaultRetailStore(User user) {
        RetailerStore s = new RetailerStore();
        s.setOwnerUserId(user.getId());
        s.setOwnerUsername(user.getUsername());
        s.setStoreCode("RET-" + user.getUsername().toUpperCase());
        s.setStoreName(user.getBusinessName() != null && !user.getBusinessName().isBlank()
                ? user.getBusinessName() : user.getFullName() + " Poultry Retail");
        s.setPhone(user.getPhone());
        s.setCity(user.getDistrict());
        s.setState(user.getState());
        return retailerRepository.save(s);
    }

    public RetailerStore recordRetailStockPurchase(RetailStockPurchaseRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        RetailerStore store = (req.getStoreId() != null && !req.getStoreId().isBlank())
                ? retailerRepository.findById(req.getStoreId()).orElseGet(this::getOrCreateCurrentRetailStore)
                : getOrCreateCurrentRetailStore();

        String pType = req.getProductType().trim().toUpperCase();
        int qty = req.getQuantity();
        double rate = req.getUnitPurchaseCostInr();
        double purchaseCost = round2((qty * rate) + req.getTransportExpenseInr());

        if ("LIVE_BIRDS".equals(pType) || "CHICKEN".equals(pType)) {
            int before = store.getLiveBirdStockCount();
            int after = before + qty;
            double avg = after > 0 ? round2(((before * store.getLiveBirdAvgPurchaseCost()) + (qty * rate)) / after) : rate;
            store.setLiveBirdStockCount(after);
            store.setLiveBirdAvgPurchaseCost(avg);
        } else if ("EGGS".equals(pType)) {
            int before = store.getEggStockPieces();
            int after = before + qty;
            double avg = after > 0 ? round2(((before * store.getEggAvgPurchaseCost()) + (qty * rate)) / after) : rate;
            store.setEggStockPieces(after);
            store.setEggAvgPurchaseCost(avg);
        } else {
            throw new BadRequestException("Retail productType must be LIVE_BIRDS or EGGS.");
        }

        store.setTotalStockPurchaseCostInr(round2(store.getTotalStockPurchaseCostInr() + purchaseCost));
        RetailerStore saved = retailerRepository.save(store);

        auditLogService.record(actor, null, "RETAIL_STOCK_PURCHASED", "RETAILER_STORE", saved.getId(),
                "Retailer purchased " + qty + " " + pType + " for ₹" + purchaseCost);
        return saved;
    }

    public Map<String, Object> getRetailDashboardData() {
        RetailerStore store = getOrCreateCurrentRetailStore();
        List<Sale> sales = saleRepository.findByOwnerUserIdOrderBySaleDateDescCreatedAtDesc(store.getOwnerUserId());
        List<Expense> expenses = expenseRepository.findByOwnerUserIdOrderByExpenseDateDescCreatedAtDesc(store.getOwnerUserId());

        double totalSalesRev = round2(sales.stream().mapToDouble(Sale::getTotalSaleAmountInr).sum());
        double opExpenses = round2(expenses.stream().mapToDouble(Expense::getAmountInr).sum());
        double netProfit = round2(totalSalesRev - store.getTotalStockPurchaseCostInr() - opExpenses);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("store", store);
        out.put("totalSalesRevenueInr", totalSalesRev);
        out.put("totalStockPurchaseCostInr", store.getTotalStockPurchaseCostInr());
        out.put("totalOperatingExpensesInr", opExpenses);
        out.put("netProfitLossInr", netProfit);
        out.put("recentSales", sales);
        return out;
    }

    // =========================================================================
    // 4. CHICKEN CUTTING SHOP MODULE (ACTUAL DRESSED WEIGHT — NEVER FIXED YIELD!)
    // =========================================================================
    public ChickenShop getOrCreateCurrentChickenShop() {
        User user = farmAccessGuard.getCurrentUser();
        if (user.getRole() == Role.SUPER_ADMIN) {
            return chickenShopRepository.findAll().stream().findFirst().orElseGet(() -> createDefaultChickenShop(user));
        }
        if (user.getRole() != Role.CHICKEN_SHOP_OWNER && user.getRole() != Role.FARM_ADMIN) {
            throw new AccessDeniedException("Chicken shop access requires Chicken Shop Owner, Farm Admin, or Super Admin role.");
        }
        return chickenShopRepository.findByOwnerUserId(user.getId()).orElseGet(() -> createDefaultChickenShop(user));
    }

    private ChickenShop createDefaultChickenShop(User user) {
        ChickenShop shop = new ChickenShop();
        shop.setOwnerUserId(user.getId());
        shop.setOwnerUsername(user.getUsername());
        shop.setShopCode("SHOP-" + user.getUsername().toUpperCase());
        shop.setShopName(user.getBusinessName() != null && !user.getBusinessName().isBlank()
                ? user.getBusinessName() : user.getFullName() + " Chicken Cutting Shop");
        shop.setPhone(user.getPhone());
        shop.setCity(user.getDistrict());
        shop.setState(user.getState());
        return chickenShopRepository.save(shop);
    }

    /**
     * Records live chicken purchase & cutting shop processing using ACTUAL dressed weight (never fixed yield!).
     * Exact Formulas:
     *   Live purchase cost = Live weight × Purchase rate
     *   Total revenue = Meat sales + By-product sales
     *   Net profit = Total revenue - Purchase cost - Processing expenses - Other allocated expenses
     */
    public ProcessingRecord recordShopProcessingBatch(ChickenShopProcessRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        ChickenShop shop = (req.getShopId() != null && !req.getShopId().isBlank())
                ? chickenShopRepository.findById(req.getShopId()).orElseGet(this::getOrCreateCurrentChickenShop)
                : getOrCreateCurrentChickenShop();

        if (req.getActualDressedMeatWeightKg() > req.getLiveWeightKg()) {
            throw new BadRequestException("Invalid weight: Actual dressed meat weight (" + req.getActualDressedMeatWeightKg()
                    + " kg) cannot exceed live bird weight (" + req.getLiveWeightKg() + " kg).");
        }
        if (req.getMeatSoldImmediatelyKg() > req.getActualDressedMeatWeightKg() + shop.getDressedMeatStockKg()) {
            throw new BadRequestException("Meat sold (" + req.getMeatSoldImmediatelyKg()
                    + " kg) exceeds available dressed meat (" + (req.getActualDressedMeatWeightKg() + shop.getDressedMeatStockKg()) + " kg).");
        }

        int availableLiveBirdsBefore = shop.getLiveBirdsStockCount() + req.getLiveBirdsPurchased();
        if (req.getBirdsProcessedCount() > availableLiveBirdsBefore) {
            throw new BadRequestException("Birds processed (" + req.getBirdsProcessedCount()
                    + ") cannot exceed live birds available (" + availableLiveBirdsBefore + ").");
        }

        // Exact Business Formulas from Section 10:
        double livePurchaseCost = round2(req.getLiveWeightKg() * req.getLivePurchaseRatePerKg());
        double meatSalesRev = round2(req.getMeatSoldImmediatelyKg() * req.getSellingPricePerKg());
        double byProdRev = round2(req.getByProductSalesInr());
        double totalRevenue = round2(meatSalesRev + byProdRev);
        double procExp = round2(req.getProcessingExpensesInr());
        double otherExp = round2(req.getOtherAllocatedExpensesInr());
        double totalCost = round2(livePurchaseCost + procExp + otherExp);
        double netProfit = round2(totalRevenue - livePurchaseCost - procExp - otherExp);

        double observedYieldPct = round2((req.getActualDressedMeatWeightKg() / req.getLiveWeightKg()) * 100.0);
        double netMeatAddedToStock = round2(req.getActualDressedMeatWeightKg() - req.getMeatSoldImmediatelyKg());
        double autoWaste = req.getWasteAndFeathersKg() > 0
                ? round2(req.getWasteAndFeathersKg())
                : round2(Math.max(0.0, req.getLiveWeightKg() - req.getActualDressedMeatWeightKg()));

        ProcessingRecord pr = new ProcessingRecord();
        pr.setShopId(shop.getId());
        pr.setShopName(shop.getShopName());
        pr.setOwnerUserId(shop.getOwnerUserId());
        pr.setRecordDate(req.getRecordDate());
        pr.setSupplierOrFarmName(req.getSupplierOrFarmName());
        pr.setBirdType(req.getBirdType());
        pr.setLiveBirdsPurchased(req.getLiveBirdsPurchased());
        pr.setBirdsProcessedCount(req.getBirdsProcessedCount());
        pr.setLiveWeightKg(round2(req.getLiveWeightKg()));
        pr.setLivePurchaseRatePerKg(round2(req.getLivePurchaseRatePerKg()));
        pr.setLivePurchaseCostInr(livePurchaseCost);
        pr.setProcessingExpensesInr(procExp);
        pr.setOtherAllocatedExpensesInr(otherExp);
        pr.setActualDressedMeatWeightKg(round2(req.getActualDressedMeatWeightKg()));
        pr.setObservedDressingPct(observedYieldPct);
        pr.setMeatSoldImmediatelyKg(round2(req.getMeatSoldImmediatelyKg()));
        pr.setRemainingMeatAddedToStockKg(netMeatAddedToStock);
        pr.setSellingPricePerKg(round2(req.getSellingPricePerKg()));
        pr.setMeatSalesRevenueInr(meatSalesRev);
        pr.setWasteAndFeathersKg(autoWaste);
        pr.setByProductSalesInr(byProdRev);
        pr.setTotalRevenueInr(totalRevenue);
        pr.setTotalCostInr(totalCost);
        pr.setNetProfitLossInr(netProfit);
        pr.setRemarks(req.getRemarks());
        pr.setRecordedBy(actor.getUsername());
        ProcessingRecord saved = processingRepository.save(pr);

        // Update separate Live Bird stock & Processed Dressed Meat stock on ChickenShop!
        shop.setLiveBirdsStockCount(availableLiveBirdsBefore - req.getBirdsProcessedCount());
        shop.setDressedMeatStockKg(round2(Math.max(0.0, shop.getDressedMeatStockKg() + netMeatAddedToStock)));
        shop.setTotalDressedMeatProducedKg(round2(shop.getTotalDressedMeatProducedKg() + req.getActualDressedMeatWeightKg()));
        shop.setTotalDressedMeatSoldKg(round2(shop.getTotalDressedMeatSoldKg() + req.getMeatSoldImmediatelyKg()));
        shop.setDefaultMeatSellingPricePerKg(round2(req.getSellingPricePerKg()));

        shop.setTotalLivePurchaseCostInr(round2(shop.getTotalLivePurchaseCostInr() + livePurchaseCost));
        shop.setTotalProcessingExpensesInr(round2(shop.getTotalProcessingExpensesInr() + procExp));
        shop.setTotalOtherAllocatedExpensesInr(round2(shop.getTotalOtherAllocatedExpensesInr() + otherExp));
        shop.setTotalMeatSalesRevenueInr(round2(shop.getTotalMeatSalesRevenueInr() + meatSalesRev));
        shop.setTotalByProductSalesRevenueInr(round2(shop.getTotalByProductSalesRevenueInr() + byProdRev));
        chickenShopRepository.save(shop);

        // Record counter meat sale & by-product sale in unified `sales` ledger for analytics
        if (meatSalesRev > 0) {
            Sale ms = new Sale();
            ms.setInvoiceNumber("CS-M-" + System.currentTimeMillis() % 1000000);
            ms.setSaleDate(req.getRecordDate());
            ms.setBusinessContext("CHICKEN_SHOP");
            ms.setOwnerUserId(shop.getOwnerUserId());
            ms.setChickenShopId(shop.getId());
            ms.setSaleCategory("DRESSED_MEAT_SALE");
            ms.setProductDescription("Fresh Dressed Chicken (" + req.getBirdType() + ")");
            ms.setCustomerName("Counter Customers");
            ms.setQuantity(round2(req.getMeatSoldImmediatelyKg()));
            ms.setUnit("KG");
            ms.setUnitSellingPriceInr(round2(req.getSellingPricePerKg()));
            ms.setTotalSaleAmountInr(meatSalesRev);
            ms.setCashReceivedInr(meatSalesRev);
            ms.setOutstandingBalanceInr(0.0);
            ms.setPaymentStatus("PAID");
            ms.setRecordedBy(actor.getUsername());
            saleRepository.save(ms);
        }

        if (byProdRev > 0) {
            Sale bps = new Sale();
            bps.setInvoiceNumber("CS-BP-" + (System.currentTimeMillis() + 1) % 1000000);
            bps.setSaleDate(req.getRecordDate());
            bps.setBusinessContext("CHICKEN_SHOP");
            bps.setOwnerUserId(shop.getOwnerUserId());
            bps.setChickenShopId(shop.getId());
            bps.setSaleCategory("MANURE_BYPRODUCT_SALE");
            bps.setProductDescription("Shop By-Products (Liver, Gizzard, Paws/Offal)");
            bps.setCustomerName("Hotel / Pet-Food Buyers");
            bps.setQuantity(1.0);
            bps.setUnit("LOT");
            bps.setUnitSellingPriceInr(byProdRev);
            bps.setTotalSaleAmountInr(byProdRev);
            bps.setCashReceivedInr(byProdRev);
            bps.setOutstandingBalanceInr(0.0);
            bps.setPaymentStatus("PAID");
            bps.setRecordedBy(actor.getUsername());
            saleRepository.save(bps);
        }

        auditLogService.record(actor, null, "CHICKEN_SHOP_PROCESSING", "PROCESSING_RECORD", saved.getId(),
                "Processed " + req.getBirdsProcessedCount() + " birds (" + req.getLiveWeightKg() + " kg live -> "
                        + req.getActualDressedMeatWeightKg() + " kg actual dressed meat, yield " + observedYieldPct + "%)");
        return saved;
    }

    public Map<String, Object> getChickenShopDashboardData() {
        ChickenShop shop = getOrCreateCurrentChickenShop();
        List<ProcessingRecord> procHistory = processingRepository.findByShopIdOrderByRecordDateDescCreatedAtDesc(shop.getId());
        List<Expense> extraShopExpenses = expenseRepository.findByOwnerUserIdOrderByExpenseDateDescCreatedAtDesc(shop.getOwnerUserId());

        double extraAllocated = round2(extraShopExpenses.stream().mapToDouble(Expense::getAmountInr).sum());
        double totalRevenue = round2(shop.getTotalMeatSalesRevenueInr() + shop.getTotalByProductSalesRevenueInr());
        double totalCost = round2(shop.getTotalLivePurchaseCostInr()
                + shop.getTotalProcessingExpensesInr()
                + shop.getTotalOtherAllocatedExpensesInr()
                + extraAllocated);
        double netProfit = round2(totalRevenue - totalCost);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("shop", shop);
        out.put("liveBirdsStockCount", shop.getLiveBirdsStockCount());
        out.put("dressedMeatStockKg", shop.getDressedMeatStockKg());
        out.put("totalMeatSalesRevenueInr", shop.getTotalMeatSalesRevenueInr());
        out.put("totalByProductSalesRevenueInr", shop.getTotalByProductSalesRevenueInr());
        out.put("totalRevenueInr", totalRevenue);
        out.put("totalLivePurchaseCostInr", shop.getTotalLivePurchaseCostInr());
        out.put("totalProcessingExpensesInr", shop.getTotalProcessingExpensesInr());
        out.put("totalOtherAllocatedExpensesInr", round2(shop.getTotalOtherAllocatedExpensesInr() + extraAllocated));
        out.put("totalCostInr", totalCost);
        out.put("netProfitLossInr", netProfit);
        out.put("processingRecords", procHistory);
        return out;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
