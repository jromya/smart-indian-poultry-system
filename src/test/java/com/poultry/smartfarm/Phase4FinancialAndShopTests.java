package com.poultry.smartfarm;

import com.poultry.smartfarm.audit.repository.AuditLogRepository;
import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.commerce.document.ChickenShop;
import com.poultry.smartfarm.commerce.document.Customer;
import com.poultry.smartfarm.commerce.document.Expense;
import com.poultry.smartfarm.commerce.document.PaymentRecord;
import com.poultry.smartfarm.commerce.document.ProcessingRecord;
import com.poultry.smartfarm.commerce.document.RetailerStore;
import com.poultry.smartfarm.commerce.document.Sale;
import com.poultry.smartfarm.commerce.document.WholesalerDeal;
import com.poultry.smartfarm.commerce.dto.ChickenShopProcessRequest;
import com.poultry.smartfarm.commerce.dto.PaymentRequest;
import com.poultry.smartfarm.commerce.dto.WholesalerDealRequest;
import com.poultry.smartfarm.commerce.repository.ChickenShopRepository;
import com.poultry.smartfarm.commerce.repository.CustomerRepository;
import com.poultry.smartfarm.commerce.repository.ExpenseRepository;
import com.poultry.smartfarm.commerce.repository.PaymentRecordRepository;
import com.poultry.smartfarm.commerce.repository.ProcessingRecordRepository;
import com.poultry.smartfarm.commerce.repository.RetailerStoreRepository;
import com.poultry.smartfarm.commerce.repository.SaleRepository;
import com.poultry.smartfarm.commerce.repository.WholesalerDealRepository;
import com.poultry.smartfarm.commerce.service.CommerceAndShopService;
import com.poultry.smartfarm.commerce.service.LedgerAndFinancialService;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.farm.repository.PoultryHouseRepository;
import com.poultry.smartfarm.inventory.document.FeedRecord;
import com.poultry.smartfarm.inventory.document.Purchase;
import com.poultry.smartfarm.inventory.document.Supplier;
import com.poultry.smartfarm.inventory.repository.FeedRecordRepository;
import com.poultry.smartfarm.inventory.repository.InventoryItemRepository;
import com.poultry.smartfarm.inventory.repository.MedicineRecordRepository;
import com.poultry.smartfarm.inventory.repository.PurchaseRepository;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.inventory.repository.VaccinationRecordRepository;
import com.poultry.smartfarm.inventory.service.InventoryService;
import com.poultry.smartfarm.inventory.service.SupplierAndPurchaseService;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Phase4FinancialAndShopTests {

    private User currentUser;
    private Map<String, Customer> customerStore;
    private Map<String, WholesalerDeal> wholesaleStore;
    private Map<String, RetailerStore> retailerStoreMap;
    private Map<String, ChickenShop> chickenShopMap;
    private Map<String, ProcessingRecord> processingStore;
    private Map<String, Sale> saleStore;
    private Map<String, Expense> expenseStore;
    private Map<String, PaymentRecord> paymentStore;
    private Map<String, Batch> batchStore;
    private Map<String, DailyRecord> dailyStore;
    private Map<String, FeedRecord> feedStore;
    private Map<String, Purchase> purchaseStore;
    private Map<String, Supplier> supplierStore;
    private Map<String, Farm> farmStore;

    private CommerceAndShopService commerceService;
    private LedgerAndFinancialService financialService;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId("u-admin");
        currentUser.setUsername("farmadmin1");
        currentUser.setFullName("Ramesh Deshmukh");
        currentUser.setRole(Role.FARM_ADMIN);
        currentUser.setAssignedFarmIds(List.of("farm-1"));

        customerStore = new LinkedHashMap<>();
        wholesaleStore = new LinkedHashMap<>();
        retailerStoreMap = new LinkedHashMap<>();
        chickenShopMap = new LinkedHashMap<>();
        processingStore = new LinkedHashMap<>();
        saleStore = new LinkedHashMap<>();
        expenseStore = new LinkedHashMap<>();
        paymentStore = new LinkedHashMap<>();
        batchStore = new LinkedHashMap<>();
        dailyStore = new LinkedHashMap<>();
        feedStore = new LinkedHashMap<>();
        purchaseStore = new LinkedHashMap<>();
        supplierStore = new LinkedHashMap<>();
        farmStore = new LinkedHashMap<>();

        Farm farm = new Farm();
        farm.setId("farm-1");
        farm.setFarmName("Sahyadri Poultry Farm");
        farmStore.put("farm-1", farm);

        FarmAccessGuard guard = new FarmAccessGuard(null) {
            @Override
            public User getCurrentUser() {
                return currentUser;
            }
        };
        AuditLogService audit = new AuditLogService(InMemoryRepoProxy.createEmpty(AuditLogRepository.class));

        CustomerRepository customerRepo = InMemoryRepoProxy.create(CustomerRepository.class, customerStore);
        WholesalerDealRepository wholesaleRepo = InMemoryRepoProxy.create(WholesalerDealRepository.class, wholesaleStore);
        RetailerStoreRepository retailerRepo = InMemoryRepoProxy.create(RetailerStoreRepository.class, retailerStoreMap);
        ChickenShopRepository shopRepo = InMemoryRepoProxy.create(ChickenShopRepository.class, chickenShopMap);
        ProcessingRecordRepository procRepo = InMemoryRepoProxy.create(ProcessingRecordRepository.class, processingStore);
        SaleRepository saleRepo = InMemoryRepoProxy.create(SaleRepository.class, saleStore);
        ExpenseRepository expRepo = InMemoryRepoProxy.create(ExpenseRepository.class, expenseStore);
        PaymentRecordRepository payRepo = InMemoryRepoProxy.create(PaymentRecordRepository.class, paymentStore);
        BatchRepository batchRepo = InMemoryRepoProxy.create(BatchRepository.class, batchStore);
        DailyRecordRepository dailyRepo = InMemoryRepoProxy.create(DailyRecordRepository.class, dailyStore);
        FeedRecordRepository feedRepo = InMemoryRepoProxy.create(FeedRecordRepository.class, feedStore);
        PurchaseRepository purchaseRepo = InMemoryRepoProxy.create(PurchaseRepository.class, purchaseStore);
        SupplierRepository supplierRepo = InMemoryRepoProxy.create(SupplierRepository.class, supplierStore);
        FarmRepository farmRepo = InMemoryRepoProxy.create(FarmRepository.class, farmStore);

        BatchService batchService = new BatchService(
                batchRepo, farmRepo,
                InMemoryRepoProxy.createEmpty(PoultryHouseRepository.class),
                InMemoryRepoProxy.createEmpty(BreedRepository.class),
                guard, audit) {
            @Override
            public void syncHouseOccupancy(String houseId) {
                // no-op in memory
            }
        };

        InventoryService dummyInv = new InventoryService(
                InMemoryRepoProxy.createEmpty(InventoryItemRepository.class),
                null, supplierRepo, farmRepo, batchService, null, guard, audit);

        SupplierAndPurchaseService supplierPurchaseService = new SupplierAndPurchaseService(
                supplierRepo, purchaseRepo, farmRepo, dummyInv, guard, audit);

        commerceService = new CommerceAndShopService(
                customerRepo, wholesaleRepo, retailerRepo, shopRepo, procRepo,
                saleRepo, expRepo, payRepo, batchService, batchRepo, farmRepo, guard, audit);

        financialService = new LedgerAndFinancialService(
                saleRepo, expRepo, payRepo, customerRepo, supplierRepo, purchaseRepo,
                feedRepo,
                InMemoryRepoProxy.createEmpty(MedicineRecordRepository.class),
                InMemoryRepoProxy.createEmpty(VaccinationRecordRepository.class),
                dailyRepo, batchRepo, farmRepo, retailerRepo, shopRepo,
                batchService, commerceService, supplierPurchaseService, guard, audit);
    }

    @Test
    void chickenCuttingShopUsesActualDressedWeightAndExactSection10Formulas() {
        currentUser.setId("u-shop");
        currentUser.setUsername("shopowner1");
        currentUser.setRole(Role.CHICKEN_SHOP_OWNER);

        ChickenShop shop = new ChickenShop();
        shop.setId("shop-1");
        shop.setOwnerUserId("u-shop");
        shop.setShopName("Bismillah Fresh Chicken Shop");
        shop.setLiveBirdsStockCount(10); // 10 birds already in cage
        shop.setDressedMeatStockKg(5.0);  // 5 kg already in chiller
        chickenShopMap.put("shop-1", shop);

        ChickenShopProcessRequest req = new ChickenShopProcessRequest();
        req.setShopId("shop-1");
        req.setRecordDate(LocalDate.now());
        req.setBirdType("Commercial Broiler");
        req.setLiveBirdsPurchased(50);          // Total available live birds = 10 + 50 = 60
        req.setBirdsProcessedCount(45);         // Remaining live birds in cage = 15
        req.setLiveWeightKg(90.0);              // 90 kg live weight
        req.setLivePurchaseRatePerKg(120.0);    // Live purchase cost = 90 * 120 = ₹10,800
        req.setActualDressedMeatWeightKg(61.2); // Actual dressed meat = 61.2 kg (observed yield = 68.0%)
        req.setMeatSoldImmediatelyKg(55.0);     // 55 kg sold @ ₹230 = ₹12,650; +6.2 kg added to chiller -> 11.2 kg
        req.setSellingPricePerKg(230.0);
        req.setByProductSalesInr(450.0);        // Total revenue = 12,650 + 450 = ₹13,100
        req.setProcessingExpensesInr(500.0);
        req.setOtherAllocatedExpensesInr(200.0);// Net profit = 13,100 - 10,800 - 500 - 200 = ₹1,600

        ProcessingRecord pr = commerceService.recordShopProcessingBatch(req);

        assertEquals(10800.0, pr.getLivePurchaseCostInr(), 0.01);
        assertEquals(13100.0, pr.getTotalRevenueInr(), 0.01);
        assertEquals(11500.0, pr.getTotalCostInr(), 0.01);
        assertEquals(1600.0, pr.getNetProfitLossInr(), 0.01);
        assertEquals(68.0, pr.getObservedDressingPct(), 0.01);

        // Verify strictly separate Live Bird vs Dressed Meat stock!
        assertEquals(15, shop.getLiveBirdsStockCount());
        assertEquals(11.2, shop.getDressedMeatStockKg(), 0.01);

        // Reject impossible dressed weight > live weight
        req.setActualDressedMeatWeightKg(95.0);
        assertThrows(BadRequestException.class, () -> commerceService.recordShopProcessingBatch(req));
    }

    @Test
    void wholesaleDispatchUpdatesBatchLiveBirdsAndTracksCustomerReceivables() {
        Batch b = new Batch();
        b.setId("b-100");
        b.setBatchCode("BATCH-MH-B100");
        b.setFarmId("farm-1");
        b.setFarmName("Sahyadri Poultry Farm");
        b.setStatus("ACTIVE");
        b.setStartingBirdQuantity(2000);
        b.setTotalMortality(40);
        b.setTotalCulls(10);
        b.setTotalBirdsSold(0); // Current live birds = 1950
        batchStore.put("b-100", b);

        Customer buyer = new Customer();
        buyer.setId("cust-ws-1");
        buyer.setFarmId("farm-1");
        buyer.setCustomerCode("CUST-WS-01");
        buyer.setCustomerName("Deccan Mandi Traders");
        buyer.setBuyerCategory("WHOLESALER");
        buyer.setPhone("9822011223");
        customerStore.put("cust-ws-1", buyer);

        WholesalerDealRequest req = new WholesalerDealRequest();
        req.setBatchId("b-100");
        req.setDealNumber("WD-2026-01");
        req.setDealDate(LocalDate.now());
        req.setCustomerId("cust-ws-1");
        req.setLiveBirdQuantity(500);
        req.setTotalLiveWeightKg(1100.0); // Avg 2.20 kg
        req.setPurchasePricePerKg(85.0);
        req.setTransportCostInr(1500.0);
        req.setSellingPricePerKg(120.0);  // Total sale = 1100 * 120 = ₹132,000
        req.setCashReceivedInr(100000.0); // Outstanding = ₹32,000

        WholesalerDeal saved = commerceService.recordWholesaleDeal(req);

        assertEquals(2.20, saved.getAverageWeightKg(), 0.01);
        assertEquals(132000.0, saved.getTotalSaleAmountInr(), 0.01);
        assertEquals(32000.0, saved.getOutstandingPaymentInr(), 0.01);
        assertEquals("PARTIAL", saved.getPaymentStatus());
        assertEquals(500, b.getTotalBirdsSold());
        assertEquals(32000.0, buyer.getOutstandingBalance(), 0.01);

        // Now settle ₹20,000 via Customer Receipt
        PaymentRequest payReq = new PaymentRequest();
        payReq.setPaymentDate(LocalDate.now());
        payReq.setPaymentType("CUSTOMER_RECEIPT");
        payReq.setCustomerId("cust-ws-1");
        payReq.setRelatedInvoiceId(saved.getLinkedSaleId());
        payReq.setAmountInr(20000.0);
        payReq.setPaymentMethod("NEFT_RTGS");

        financialService.recordPaymentSettlement(payReq);
        assertEquals(12000.0, buyer.getOutstandingBalance(), 0.01);
    }

    @Test
    void financialReportPreventsDoubleCountingStockPurchasesAndOverlappingFeedRecords() {
        LocalDate today = LocalDate.now();

        // 1. Batch with 1,000 chicks @ ₹40 = ₹40,000 chick purchase cost
        Batch b = new Batch();
        b.setId("b-fin-1");
        b.setBatchCode("BATCH-FIN-01");
        b.setFarmId("farm-1");
        b.setPurchaseDate(today.minusDays(10));
        b.setStartingBirdQuantity(1000);
        b.setPurchasePricePerChick(40.0);
        b.setEstimatedFeedRatePerKg(42.0);
        batchStore.put("b-fin-1", b);

        // 2. Warehouse Feed Stock Purchase of ₹88,000 (Asset procurement — MUST NOT be double-counted into P&L!)
        Purchase warehousePurchase = new Purchase();
        warehousePurchase.setId("pur-1");
        warehousePurchase.setFarmId("farm-1");
        warehousePurchase.setPurchaseDate(today.minusDays(9));
        warehousePurchase.setTotalInvoiceAmount(88000.0);
        purchaseStore.put("pur-1", warehousePurchase);

        // 3. Issued 100 kg feed via FeedRecord on `today` @ ₹44/kg = ₹4,400
        FeedRecord fr = new FeedRecord();
        fr.setId("fr-1");
        fr.setFarmId("farm-1");
        fr.setBatchId("b-fin-1");
        fr.setRecordDate(today);
        fr.setQuantityIssuedKg(100.0);
        fr.setTotalFeedCostInr(4400.0);
        feedStore.put("fr-1", fr);

        // 4. DailyRecord on the SAME batch & date (`today`) also mentions 100 kg feed + ₹500 labour
        //    -> Feed MUST NOT be counted twice for `today`, only ₹500 labour should be added!
        DailyRecord dr = new DailyRecord();
        dr.setId("dr-1");
        dr.setFarmId("farm-1");
        dr.setBatchId("b-fin-1");
        dr.setRecordDate(today);
        dr.setFeedConsumptionKg(100.0);
        dr.setLabourCost(500.0);
        dr.setElectricityCost(200.0);
        dailyStore.put("dr-1", dr);

        // 5. Sale Invoice of ₹65,000 (₹50,000 cash received, ₹15,000 credit)
        Sale sale = new Sale();
        sale.setId("sale-1");
        sale.setFarmId("farm-1");
        sale.setBatchId("b-fin-1");
        sale.setSaleDate(today);
        sale.setSaleCategory("LIVE_CHICKEN_SALE");
        sale.setTotalSaleAmountInr(65000.0);
        sale.setCashReceivedInr(50000.0);
        sale.setOutstandingBalanceInr(15000.0);
        saleStore.put("sale-1", sale);
        b.setTotalBirdSalesRevenue(65000.0); // Synchronized batch counter — MUST NOT be double-counted either!

        Map<String, Object> report = financialService.calculateFinancialReport("farm-1", null, null, null);

        assertEquals(65000.0, (Double) report.get("totalRevenueInr"), 0.01, "Sale must not be double-counted with batch counter");
        assertEquals(40000.0, (Double) report.get("chickPurchaseExpensesInr"), 0.01);
        assertEquals(4400.0, (Double) report.get("feedExpensesInr"), 0.01, "Warehouse ₹88k purchase & duplicate daily feed must not double-count");
        assertEquals(500.0, (Double) report.get("labourExpensesInr"), 0.01);
        assertEquals(200.0, (Double) report.get("electricityExpensesInr"), 0.01);
        assertEquals(45100.0, (Double) report.get("totalExpensesInr"), 0.01);
        assertEquals(19900.0, (Double) report.get("netProfitLossInr"), 0.01);
        assertEquals(50000.0, (Double) report.get("cashReceivedInr"), 0.01);
        assertEquals(88000.0, (Double) report.get("inventoryStockPurchasesExcludedFromDoubleCountInr"), 0.01);
    }
}
