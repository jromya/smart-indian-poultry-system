package com.poultry.smartfarm;

import com.poultry.smartfarm.audit.repository.AuditLogRepository;
import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.farm.repository.PoultryHouseRepository;
import com.poultry.smartfarm.inventory.document.FeedRecord;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.document.StockMovement;
import com.poultry.smartfarm.inventory.document.VaccinationRecord;
import com.poultry.smartfarm.inventory.dto.FeedIssueRequest;
import com.poultry.smartfarm.inventory.dto.MedicineRecordRequest;
import com.poultry.smartfarm.inventory.repository.AppNotificationRepository;
import com.poultry.smartfarm.inventory.repository.FeedRecordRepository;
import com.poultry.smartfarm.inventory.repository.InventoryItemRepository;
import com.poultry.smartfarm.inventory.repository.MedicineRecordRepository;
import com.poultry.smartfarm.inventory.repository.StockMovementRepository;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.inventory.repository.VaccinationRecordRepository;
import com.poultry.smartfarm.inventory.service.FeedAndHealthService;
import com.poultry.smartfarm.inventory.service.InventoryService;
import com.poultry.smartfarm.inventory.service.NotificationAlertService;
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

class Phase3InventoryAndVaccineTests {

    private Map<String, InventoryItem> inventoryStore;
    private Map<String, StockMovement> movementStore;
    private Map<String, Batch> batchStore;

    private FeedAndHealthService feedAndHealthService;

    @BeforeEach
    void setUp() {
        inventoryStore = new LinkedHashMap<>();
        movementStore = new LinkedHashMap<>();
        batchStore = new LinkedHashMap<>();

        User managerActor = new User();
        managerActor.setId("u1");
        managerActor.setUsername("manager1");
        managerActor.setRole(Role.FARM_MANAGER);
        managerActor.setAssignedFarmIds(List.of("farm-1"));

        FarmAccessGuard farmAccessGuard = new FarmAccessGuard(null) {
            @Override
            public User getCurrentUser() {
                return managerActor;
            }
        };

        AuditLogService auditLogService = new AuditLogService(InMemoryRepoProxy.createEmpty(AuditLogRepository.class));

        InventoryItemRepository inventoryRepo = InMemoryRepoProxy.create(InventoryItemRepository.class, inventoryStore);
        StockMovementRepository movementRepo = InMemoryRepoProxy.create(StockMovementRepository.class, movementStore);
        BatchRepository batchRepo = InMemoryRepoProxy.create(BatchRepository.class, batchStore);
        FeedRecordRepository feedRepo = InMemoryRepoProxy.createEmpty(FeedRecordRepository.class);
        MedicineRecordRepository medicineRepo = InMemoryRepoProxy.createEmpty(MedicineRecordRepository.class);
        VaccinationRecordRepository vaccinationRepo = InMemoryRepoProxy.createEmpty(VaccinationRecordRepository.class);
        DailyRecordRepository dailyRepo = InMemoryRepoProxy.createEmpty(DailyRecordRepository.class);
        FarmRepository farmRepo = InMemoryRepoProxy.createEmpty(FarmRepository.class);

        NotificationAlertService alertService = new NotificationAlertService(
                InMemoryRepoProxy.createEmpty(AppNotificationRepository.class),
                inventoryRepo, vaccinationRepo, farmRepo, farmAccessGuard);

        BatchService batchService = new BatchService(
                batchRepo, farmRepo,
                InMemoryRepoProxy.createEmpty(PoultryHouseRepository.class),
                InMemoryRepoProxy.createEmpty(BreedRepository.class),
                farmAccessGuard, auditLogService);

        InventoryService inventoryService = new InventoryService(
                inventoryRepo, movementRepo,
                InMemoryRepoProxy.createEmpty(SupplierRepository.class),
                farmRepo, batchService, alertService, farmAccessGuard, auditLogService);

        feedAndHealthService = new FeedAndHealthService(
                feedRepo, medicineRepo, vaccinationRepo,
                batchService, batchRepo, dailyRepo,
                inventoryService, alertService, farmAccessGuard, auditLogService);
    }

    @Test
    void issuingFeedToBatchAutomaticallyReducesInventoryAndLogsMovement() {
        Batch batch = new Batch();
        batch.setId("b-1");
        batch.setBatchCode("BATCH-MH-2026-B01");
        batch.setFarmId("farm-1");
        batch.setStatus("ACTIVE");
        batchStore.put("b-1", batch);

        InventoryItem starter = new InventoryItem();
        starter.setId("inv-feed-1");
        starter.setFarmId("farm-1");
        starter.setItemCode("FEED-STARTER");
        starter.setItemName("Broiler Starter Crumbs");
        starter.setCategory("FEED");
        starter.setSubCategory("STARTER");
        starter.setUnit("KG");
        starter.setCurrentStock(1000.0);
        starter.setUnitCostInr(44.0);
        starter.recalculateValuation();
        inventoryStore.put("inv-feed-1", starter);

        FeedIssueRequest req = new FeedIssueRequest();
        req.setBatchId("b-1");
        req.setInventoryItemId("inv-feed-1");
        req.setRecordDate(LocalDate.now());
        req.setQuantityIssuedKg(250.0);
        req.setSyncDailyRecordFeed(false);

        FeedRecord saved = feedAndHealthService.issueFeedToBatch(req);

        assertEquals(750.0, starter.getCurrentStock(), 0.01);
        assertEquals(33000.0, starter.getTotalValuationInr(), 0.01);
        assertEquals(750.0, saved.getRemainingStockKg(), 0.01);
        assertEquals(11000.0, saved.getTotalFeedCostInr(), 0.01);
        assertEquals(1, movementStore.size());
        StockMovement mv = movementStore.values().iterator().next();
        assertEquals("BATCH_ISSUE", mv.getMovementType());
        assertEquals(1000.0, mv.getStockBefore(), 0.01);
        assertEquals(750.0, mv.getStockAfter(), 0.01);
    }

    @Test
    void blocksIssuingMoreFeedThanAvailableStock() {
        Batch batch = new Batch();
        batch.setId("b-1");
        batch.setBatchCode("BATCH-01");
        batch.setFarmId("farm-1");
        batch.setStatus("ACTIVE");
        batchStore.put("b-1", batch);

        InventoryItem lowFeed = new InventoryItem();
        lowFeed.setId("inv-low");
        lowFeed.setFarmId("farm-1");
        lowFeed.setItemName("Finisher Feed");
        lowFeed.setCategory("FEED");
        lowFeed.setUnit("KG");
        lowFeed.setCurrentStock(100.0);
        inventoryStore.put("inv-low", lowFeed);

        FeedIssueRequest req = new FeedIssueRequest();
        req.setBatchId("b-1");
        req.setInventoryItemId("inv-low");
        req.setRecordDate(LocalDate.now());
        req.setQuantityIssuedKg(180.0);

        assertThrows(BadRequestException.class, () -> feedAndHealthService.issueFeedToBatch(req));
    }

    @Test
    void blocksExpiredMedicineAdministration() {
        Batch batch = new Batch();
        batch.setId("b-1");
        batch.setBatchCode("BATCH-01");
        batch.setFarmId("farm-1");
        batch.setStatus("ACTIVE");
        batchStore.put("b-1", batch);

        InventoryItem expiredMed = new InventoryItem();
        expiredMed.setId("med-exp");
        expiredMed.setFarmId("farm-1");
        expiredMed.setItemName("Expired Antibiotic");
        expiredMed.setCategory("MEDICINE");
        expiredMed.setUnit("LITERS");
        expiredMed.setCurrentStock(5.0);
        expiredMed.setExpiryDate(LocalDate.now().minusDays(2));
        inventoryStore.put("med-exp", expiredMed);

        MedicineRecordRequest req = new MedicineRecordRequest();
        req.setBatchId("b-1");
        req.setInventoryItemId("med-exp");
        req.setAdministrationDate(LocalDate.now());
        req.setQuantityUsed(1.0);
        req.setDosageRecord("1ml/2L");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> feedAndHealthService.recordMedicineAdministration(req));
        assertTrue(ex.getMessage().toLowerCase().contains("expired"));
    }

    @Test
    void generatesDistinctBreedSpecificVaccinationSchedules() {
        Batch broiler = new Batch();
        broiler.setChickArrivalDate(LocalDate.now());
        broiler.setBatchCategory("BROILER");
        broiler.setBreedName("Ross 308");

        Batch layer = new Batch();
        layer.setChickArrivalDate(LocalDate.now());
        layer.setBatchCategory("LAYER");
        layer.setBreedName("BV-300 Layer");

        Batch kadaknath = new Batch();
        kadaknath.setChickArrivalDate(LocalDate.now());
        kadaknath.setBatchCategory("INDIGENOUS");
        kadaknath.setBreedName("Kadaknath");

        List<VaccinationRecord> bSchedule = feedAndHealthService.buildStandardBreedScheduleDocuments(broiler, false);
        List<VaccinationRecord> lSchedule = feedAndHealthService.buildStandardBreedScheduleDocuments(layer, false);
        List<VaccinationRecord> kSchedule = feedAndHealthService.buildStandardBreedScheduleDocuments(kadaknath, false);

        assertEquals(4, bSchedule.size(), "Commercial broiler schedule should have 4 short-cycle vaccines");
        assertEquals(8, lSchedule.size(), "Commercial 72-week layer schedule should include EDS/Fowl Pox/R2B (8 vaccines)");
        assertEquals(6, kSchedule.size(), "Indigenous Kadaknath schedule should include Fowl Pox & R2B (6 vaccines)");
    }
}
