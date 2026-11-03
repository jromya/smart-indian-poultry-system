package com.poultry.smartfarm;

import com.poultry.smartfarm.analytics.service.AnalyticsService;
import com.poultry.smartfarm.audit.repository.AuditLogRepository;
import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.document.EggRecord;
import com.poultry.smartfarm.batch.document.WeightRecord;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.repository.EggRecordRepository;
import com.poultry.smartfarm.batch.repository.MortalityRecordRepository;
import com.poultry.smartfarm.batch.repository.WeightRecordRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.batch.service.DailyRecordService;
import com.poultry.smartfarm.batch.service.PoultryAnalyticsCalculationService;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.commerce.document.Sale;
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
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.farm.repository.PoultryHouseRepository;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.repository.AppNotificationRepository;
import com.poultry.smartfarm.inventory.repository.FeedRecordRepository;
import com.poultry.smartfarm.inventory.repository.InventoryItemRepository;
import com.poultry.smartfarm.inventory.repository.MedicineRecordRepository;
import com.poultry.smartfarm.inventory.repository.PurchaseRepository;
import com.poultry.smartfarm.inventory.repository.StockMovementRepository;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.inventory.repository.VaccinationRecordRepository;
import com.poultry.smartfarm.inventory.service.InventoryService;
import com.poultry.smartfarm.inventory.service.SupplierAndPurchaseService;
import com.poultry.smartfarm.report.document.BatchReport;
import com.poultry.smartfarm.report.repository.BatchReportRepository;
import com.poultry.smartfarm.report.service.AdminBackupService;
import com.poultry.smartfarm.report.service.BatchPdfReportService;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.repository.UserRepository;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Phase5And6AnalyticsReportTests {

    private User currentUser;
    private Map<String, User> userStore;
    private Map<String, Farm> farmStore;
    private Map<String, Batch> batchStore;
    private Map<String, DailyRecord> dailyStore;
    private Map<String, WeightRecord> weightStore;
    private Map<String, EggRecord> eggStore;
    private Map<String, InventoryItem> inventoryStore;
    private Map<String, Sale> saleStore;
    private Map<String, BatchReport> reportStore;

    private BatchService batchService;
    private AnalyticsService analyticsService;
    private BatchPdfReportService pdfReportService;
    private AdminBackupService backupService;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId("u-1");
        currentUser.setUsername("farmadmin1");
        currentUser.setFullName("Ramesh Deshmukh");
        currentUser.setPasswordHash("$2a$12$secretbcryptvalue");
        currentUser.setRole(Role.FARM_ADMIN);
        currentUser.setPreferredLanguage("mr");
        currentUser.setAssignedFarmIds(List.of("farm-1"));

        userStore = new LinkedHashMap<>();
        userStore.put("u-1", currentUser);

        farmStore = new LinkedHashMap<>();
        Farm farm1 = new Farm();
        farm1.setId("farm-1");
        farm1.setFarmName("Sahyadri Poultry Farm");
        farmStore.put("farm-1", farm1);

        batchStore = new LinkedHashMap<>();
        Batch b = new Batch();
        b.setId("b-1");
        b.setBatchCode("BATCH-MH-ASEEL-01");
        b.setBatchName("Desi Aseel Flock");
        b.setFarmId("farm-1");
        b.setFarmName("Sahyadri Poultry Farm");
        b.setPoultryHouseName("Shed A1");
        b.setBreedName("Aseel");
        b.setBatchCategory("INDIGENOUS");
        b.setPurchaseDate(LocalDate.now().minusDays(45));
        b.setChickArrivalDate(LocalDate.now().minusDays(45));
        b.setStartingBirdQuantity(1000);
        b.setPurchasePricePerChick(38.0);
        b.setTotalMortality(20);
        b.setTotalCulls(5);
        b.setTotalBirdsSold(600);
        b.setTotalSoldLiveWeightKg(960.0);
        b.setTotalBirdSalesRevenue(211200.0);
        b.setLatestAverageWeightGrams(1600.0);
        batchStore.put("b-1", b);

        dailyStore = new LinkedHashMap<>();
        DailyRecord dr = new DailyRecord();
        dr.setId("dr-1");
        dr.setFarmId("farm-1");
        dr.setBatchId("b-1");
        dr.setBatchCode("BATCH-MH-ASEEL-01");
        dr.setRecordDate(LocalDate.now().minusDays(2));
        dr.setMortality(2);
        dr.setFeedConsumptionKg(85.0);
        dr.setWaterConsumptionLiters(170.0);
        dr.setAverageBirdWeightGrams(1600.0);
        dailyStore.put("dr-1", dr);

        weightStore = new LinkedHashMap<>();
        eggStore = new LinkedHashMap<>();
        inventoryStore = new LinkedHashMap<>();
        InventoryItem inv = new InventoryItem();
        inv.setId("inv-1");
        inv.setFarmId("farm-1");
        inv.setCategory("FEED");
        inv.setCurrentStock(500.0);
        inv.setUnitCostInr(42.0);
        inv.recalculateValuation();
        inventoryStore.put("inv-1", inv);

        saleStore = new LinkedHashMap<>();
        reportStore = new LinkedHashMap<>();

        FarmAccessGuard guard = new FarmAccessGuard(null) {
            @Override
            public User getCurrentUser() {
                return currentUser;
            }
        };

        AuditLogRepository auditRepo = InMemoryRepoProxy.createEmpty(AuditLogRepository.class);
        AuditLogService audit = new AuditLogService(auditRepo);

        UserRepository userRepo = InMemoryRepoProxy.create(UserRepository.class, userStore);
        FarmRepository farmRepo = InMemoryRepoProxy.create(FarmRepository.class, farmStore);
        PoultryHouseRepository houseRepo = InMemoryRepoProxy.createEmpty(PoultryHouseRepository.class);
        BreedRepository breedRepo = InMemoryRepoProxy.createEmpty(BreedRepository.class);
        BatchRepository batchRepo = InMemoryRepoProxy.create(BatchRepository.class, batchStore);
        DailyRecordRepository dailyRepo = InMemoryRepoProxy.create(DailyRecordRepository.class, dailyStore);
        MortalityRecordRepository mortRepo = InMemoryRepoProxy.createEmpty(MortalityRecordRepository.class);
        WeightRecordRepository weightRepo = InMemoryRepoProxy.create(WeightRecordRepository.class, weightStore);
        EggRecordRepository eggRepo = InMemoryRepoProxy.create(EggRecordRepository.class, eggStore);
        InventoryItemRepository invRepo = InMemoryRepoProxy.create(InventoryItemRepository.class, inventoryStore);
        StockMovementRepository smRepo = InMemoryRepoProxy.createEmpty(StockMovementRepository.class);
        FeedRecordRepository feedRepo = InMemoryRepoProxy.createEmpty(FeedRecordRepository.class);
        MedicineRecordRepository medRepo = InMemoryRepoProxy.createEmpty(MedicineRecordRepository.class);
        VaccinationRecordRepository vaxRepo = InMemoryRepoProxy.createEmpty(VaccinationRecordRepository.class);
        SupplierRepository supRepo = InMemoryRepoProxy.createEmpty(SupplierRepository.class);
        PurchaseRepository purRepo = InMemoryRepoProxy.createEmpty(PurchaseRepository.class);
        AppNotificationRepository notifRepo = InMemoryRepoProxy.createEmpty(AppNotificationRepository.class);
        CustomerRepository custRepo = InMemoryRepoProxy.createEmpty(CustomerRepository.class);
        WholesalerDealRepository wsRepo = InMemoryRepoProxy.createEmpty(WholesalerDealRepository.class);
        RetailerStoreRepository retRepo = InMemoryRepoProxy.createEmpty(RetailerStoreRepository.class);
        ChickenShopRepository shopRepo = InMemoryRepoProxy.createEmpty(ChickenShopRepository.class);
        ProcessingRecordRepository procRepo = InMemoryRepoProxy.createEmpty(ProcessingRecordRepository.class);
        SaleRepository saleRepo = InMemoryRepoProxy.create(SaleRepository.class, saleStore);
        ExpenseRepository expRepo = InMemoryRepoProxy.createEmpty(ExpenseRepository.class);
        PaymentRecordRepository payRepo = InMemoryRepoProxy.createEmpty(PaymentRecordRepository.class);
        BatchReportRepository rptRepo = InMemoryRepoProxy.create(BatchReportRepository.class, reportStore);

        batchService = new BatchService(batchRepo, farmRepo, houseRepo, breedRepo, guard, audit) {
            @Override
            public void syncHouseOccupancy(String houseId) {
                // no-op
            }
        };

        DailyRecordService dailyRecordService = new DailyRecordService(
                dailyRepo, mortRepo, weightRepo, batchRepo, breedRepo, batchService, guard, audit);
        PoultryAnalyticsCalculationService flockCalc = new PoultryAnalyticsCalculationService(
                batchService, batchRepo, dailyRepo, weightRepo, eggRepo, breedRepo, dailyRecordService, guard, audit);

        InventoryService invService = new InventoryService(invRepo, smRepo, supRepo, farmRepo, batchService, null, guard, audit);
        SupplierAndPurchaseService spService = new SupplierAndPurchaseService(supRepo, purRepo, farmRepo, invService, guard, audit);
        CommerceAndShopService commerceService = new CommerceAndShopService(
                custRepo, wsRepo, retRepo, shopRepo, procRepo, saleRepo, expRepo, payRepo, batchService, batchRepo, farmRepo, guard, audit);
        LedgerAndFinancialService finService = new LedgerAndFinancialService(
                saleRepo, expRepo, payRepo, custRepo, supRepo, purRepo, feedRepo, medRepo, vaxRepo,
                dailyRepo, batchRepo, farmRepo, retRepo, shopRepo, batchService, commerceService, spService, guard, audit);

        analyticsService = new AnalyticsService(
                farmRepo, batchRepo, breedRepo, dailyRepo, weightRepo, eggRepo,
                invRepo, saleRepo, procRepo, shopRepo, retRepo, flockCalc, finService, guard);

        pdfReportService = new BatchPdfReportService(
                rptRepo, batchService, breedRepo, dailyRepo, eggRepo, flockCalc, finService, guard, audit);
        pdfReportService.registerBatchCompletionHook();

        backupService = new AdminBackupService(
                userRepo, farmRepo, houseRepo, breedRepo, auditRepo,
                batchRepo, dailyRepo, mortRepo, weightRepo, eggRepo,
                invRepo, smRepo, feedRepo, medRepo, vaxRepo, supRepo, purRepo, notifRepo,
                custRepo, wsRepo, retRepo, shopRepo, procRepo, saleRepo, expRepo, payRepo,
                rptRepo, guard, audit);
    }

    @Test
    @SuppressWarnings("unchecked")
    void unifiedAnalyticsReturnsAll15RequiredChartsWithFarmIsolation() {
        Map<String, Object> result = analyticsService.buildUnifiedAnalyticsCharts(
                "farm-1", null, null, null, null, null, "ALL");

        Map<String, Object> charts = (Map<String, Object>) result.get("charts");
        assertNotNull(charts);
        assertEquals(15, charts.size(), "Must provide all 15 interactive Chart.js datasets");
        assertTrue(charts.containsKey("totalBirdsChart"));
        assertTrue(charts.containsKey("dailyMortalityChart"));
        assertTrue(charts.containsKey("dailyGrowthChart"));
        assertTrue(charts.containsKey("averageWeightChart"));
        assertTrue(charts.containsKey("feedConsumptionChart"));
        assertTrue(charts.containsKey("fcrChart"));
        assertTrue(charts.containsKey("waterConsumptionChart"));
        assertTrue(charts.containsKey("eggProductionChart"));
        assertTrue(charts.containsKey("chickenSalesChart"));
        assertTrue(charts.containsKey("meatSalesChart"));
        assertTrue(charts.containsKey("incomeVsExpensesChart"));
        assertTrue(charts.containsKey("profitLossChart"));
        assertTrue(charts.containsKey("inventoryChart"));
        assertTrue(charts.containsKey("farmWisePerformanceChart"));
        assertTrue(charts.containsKey("batchComparisonChart"));

        // Verify Farm Isolation rejects unassigned farm-2
        assertThrows(AccessDeniedException.class, () ->
                analyticsService.buildUnifiedAnalyticsCharts("farm-2", null, null, null, null, null, "ALL"));
    }

    @Test
    void completingBatchAutoGeneratesReportAndRendersValidUnicodePdf() {
        assertEquals(0, reportStore.size());

        // Complete batch -> triggers @PostConstruct hook automatically!
        batchService.completeBatch("b-1", LocalDate.now());

        assertEquals(1, reportStore.size(), "Completing a batch must automatically generate a BatchReport snapshot");
        BatchReport autoReport = reportStore.values().iterator().next();
        assertTrue(autoReport.isAutoGeneratedOnCompletion());
        assertEquals("COMPLETED", autoReport.getBatchStatus());
        assertTrue(autoReport.getNetProfitLossInr() > 0);

        // Render PDF in Marathi (Unicode Indian script) & English
        byte[] mrPdf = pdfReportService.renderPdfBytes(autoReport, "mr");
        assertNotNull(mrPdf);
        assertTrue(mrPdf.length > 800, "Generated Marathi PDF must contain valid OpenPDF tables and fonts");
        String pdfHeader = new String(mrPdf, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", pdfHeader);

        byte[] enPdf = pdfReportService.renderPdfBytes(autoReport, "en");
        assertEquals("%PDF-", new String(enPdf, 0, 5, StandardCharsets.US_ASCII));
    }

    @Test
    @SuppressWarnings("unchecked")
    void adminBackupEnforcesSuperAdminAndExportsAll27CollectionsWithRedactedPasswords() {
        // FARM_ADMIN must be blocked
        assertThrows(AccessDeniedException.class, () -> backupService.exportFullDatabaseSnapshot());

        // Elevate to SUPER_ADMIN
        currentUser.setRole(Role.SUPER_ADMIN);
        Map<String, Object> summary = backupService.getCollectionHealthSummary();
        assertEquals(27, summary.get("totalCollections"), "Must track all 27 MongoDB collections");

        Map<String, Object> backup = backupService.exportFullDatabaseSnapshot();
        assertEquals(27, backup.get("totalCollections"));
        Map<String, Object> cols = (Map<String, Object>) backup.get("collections");
        List<Map<String, Object>> exportedUsers = (List<Map<String, Object>>) cols.get("users");
        assertEquals("[REDACTED_BCRYPT_HASH]", exportedUsers.get(0).get("passwordHash"),
                "BCrypt password hashes must be redacted in backup JSON");
    }
}
