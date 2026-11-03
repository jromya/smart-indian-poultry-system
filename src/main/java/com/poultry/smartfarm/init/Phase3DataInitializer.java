package com.poultry.smartfarm.init;

import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.inventory.document.FeedRecord;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.document.MedicineRecord;
import com.poultry.smartfarm.inventory.document.Purchase;
import com.poultry.smartfarm.inventory.document.StockMovement;
import com.poultry.smartfarm.inventory.document.Supplier;
import com.poultry.smartfarm.inventory.document.VaccinationRecord;
import com.poultry.smartfarm.inventory.repository.FeedRecordRepository;
import com.poultry.smartfarm.inventory.repository.InventoryItemRepository;
import com.poultry.smartfarm.inventory.repository.MedicineRecordRepository;
import com.poultry.smartfarm.inventory.repository.PurchaseRepository;
import com.poultry.smartfarm.inventory.repository.StockMovementRepository;
import com.poultry.smartfarm.inventory.repository.SupplierRepository;
import com.poultry.smartfarm.inventory.repository.VaccinationRecordRepository;
import com.poultry.smartfarm.inventory.service.FeedAndHealthService;
import com.poultry.smartfarm.inventory.service.NotificationAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(3)
public class Phase3DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Phase3DataInitializer.class);

    private final SupplierRepository supplierRepository;
    private final InventoryItemRepository inventoryRepository;
    private final PurchaseRepository purchaseRepository;
    private final StockMovementRepository movementRepository;
    private final FeedRecordRepository feedRepository;
    private final MedicineRecordRepository medicineRepository;
    private final VaccinationRecordRepository vaccinationRepository;
    private final FarmRepository farmRepository;
    private final BatchRepository batchRepository;
    private final FeedAndHealthService feedAndHealthService;
    private final NotificationAlertService alertService;

    @Value("${app.security.seed-demo-users:true}")
    private boolean seedDemoData;

    public Phase3DataInitializer(SupplierRepository supplierRepository,
                                 InventoryItemRepository inventoryRepository,
                                 PurchaseRepository purchaseRepository,
                                 StockMovementRepository movementRepository,
                                 FeedRecordRepository feedRepository,
                                 MedicineRecordRepository medicineRepository,
                                 VaccinationRecordRepository vaccinationRepository,
                                 FarmRepository farmRepository,
                                 BatchRepository batchRepository,
                                 FeedAndHealthService feedAndHealthService,
                                 NotificationAlertService alertService) {
        this.supplierRepository = supplierRepository;
        this.inventoryRepository = inventoryRepository;
        this.purchaseRepository = purchaseRepository;
        this.movementRepository = movementRepository;
        this.feedRepository = feedRepository;
        this.medicineRepository = medicineRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.farmRepository = farmRepository;
        this.batchRepository = batchRepository;
        this.feedAndHealthService = feedAndHealthService;
        this.alertService = alertService;
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData || supplierRepository.count() > 0) {
            return;
        }

        Farm farm1 = farmRepository.findByFarmCodeIgnoreCase("FARM-MH-001").orElse(null);
        Farm farm2 = farmRepository.findByFarmCodeIgnoreCase("FARM-MP-002").orElse(null);
        if (farm1 == null) return;

        LocalDate today = LocalDate.now();

        // 1. Seed Suppliers
        Supplier supFeed = createSup(farm1, "SUP-MH-FEED01", "Godrej Agrovet Poultry Feeds", "Ramesh Kulkarni",
                "9822011223", "27AAACG1234F1Z5", "Baramati MIDC", "Pune", "Maharashtra", List.of("FEED"));
        Supplier supVet = createSup(farm1, "SUP-MH-VET01", "Venky's Animal Health & Bio-Med", "Dr. Sneha Joshi",
                "9823044556", "27AAACV5678H1Z2", "Sinhagad Road", "Pune", "Maharashtra", List.of("MEDICINE", "VACCINE"));
        Supplier supEqp = createSup(farm1, "SUP-MH-EQP01", "Sahyadri Poultry Equipment & Packaging", "Nitin Shinde",
                "9765088990", "27AABCS9988K1Z9", "Satara Road", "Nashik", "Maharashtra", List.of("EQUIPMENT", "PACKAGING", "OTHER"));

        // 2. Seed Inventory Items across all 6 categories (including Low-Stock & Expiring-Soon items for live alerts!)
        InventoryItem starterFeed = createItem(farm1, "FEED-STARTER", "Godrej Broiler Starter Crumbs", "FEED", "STARTER",
                "KG", 850.0, 300.0, 44.0, "LOT-GF-901", today.plusDays(95), supFeed, "Feed Godown Bay 1");
        InventoryItem finisherFeed = createItem(farm1, "FEED-FINISHER", "Godrej Broiler Finisher Pellets", "FEED", "FINISHER",
                "KG", 240.0, 400.0, 41.5, "LOT-GF-918", today.plusDays(80), supFeed, "Feed Godown Bay 2"); // LOW STOCK!
        InventoryItem layerMash = createItem(farm1, "FEED-LAYER", "High-Calcium Peak Layer Mash", "FEED", "LAYER_MASH",
                "KG", 3200.0, 600.0, 36.0, "LOT-LM-402", today.plusDays(110), supFeed, "Feed Godown Bay 3");

        InventoryItem enroMed = createItem(farm1, "MED-ENRO10", "Enrofloxacin 10% Oral Solution", "MEDICINE", "ANTIBIOTIC",
                "LITERS", 4.5, 2.0, 680.0, "ENRO-26B", today.plusDays(18), supVet, "Vet Medicine Cabinet"); // EXPIRING IN 18 DAYS!
        InventoryItem vimeralMed = createItem(farm1, "MED-BCOMP", "Multivitamin B-Complex + Electrolytes", "MEDICINE", "VITAMIN",
                "LITERS", 12.0, 3.0, 490.0, "VIT-88A", today.plusDays(240), supVet, "Vet Medicine Cabinet");

        InventoryItem lasotaVax = createItem(farm1, "VAX-LASOTA", "Ranikhet Lasota Live Vaccine", "VACCINE", "LIVE_VACCINE",
                "DOSES", 6000.0, 2000.0, 0.18, "VH-LAS-77", today.plusDays(120), supVet, "2-8°C Vaccine Refrigerator");

        createItem(farm1, "EQP-DRINKER", "Automatic Jumbo Bell Drinkers", "EQUIPMENT", "DRINKER",
                "PIECES", 65.0, 10.0, 320.0, "EQ-2026", null, supEqp, "Equipment Store");
        createItem(farm1, "PKG-TRAY30", "30-Egg Pulp Paper Trays", "PACKAGING", "EGG_TRAY",
                "TRAYS", 950.0, 250.0, 4.5, "TR-550", null, supEqp, "Egg Grading Room");
        createItem(farm1, "OTH-LIME", "Biosecurity Hydrated Lime Powder", "OTHER", "SANITATION",
                "KG", 180.0, 50.0, 14.0, "LM-11", null, supEqp, "Gate Disinfection Store");

        // 3. Seed Sample Purchase Invoices + Supplier Ledger Balances
        seedPurchase(farm1, supFeed, finisherFeed, "INV-GF-2026-104", today.minusDays(14), 2500.0, 41.5, 1250.0, 80000.0);
        seedPurchase(farm1, supFeed, layerMash, "INV-GF-2026-119", today.minusDays(6), 4000.0, 36.0, 1600.0, 145600.0);
        seedPurchase(farm1, supVet, enroMed, "INV-VH-2026-042", today.minusDays(20), 6.0, 680.0, 0.0, 4080.0);

        supFeed.setTotalPurchasesAmount(250600.0);
        supFeed.setTotalPaidAmount(225600.0);
        supFeed.setOutstandingPayable(25000.0);
        supplierRepository.save(supFeed);

        supVet.setTotalPurchasesAmount(4080.0);
        supVet.setTotalPaidAmount(4080.0);
        supVet.setOutstandingPayable(0.0);
        supplierRepository.save(supVet);

        // 4. Seed Batch Feed Records, Medicine Records & Breed-Specific Vaccination Schedules
        Batch broiler = batchRepository.findByBatchCodeIgnoreCase("BATCH-MH-2026-B01").orElse(null);
        Batch layer = batchRepository.findByBatchCodeIgnoreCase("BATCH-MH-2026-L01").orElse(null);
        Batch kadaknath = batchRepository.findByBatchCodeIgnoreCase("BATCH-MP-2026-K01").orElse(null);

        if (broiler != null) {
            seedFeedIssue(farm1, broiler, finisherFeed, today.minusDays(1), 620.0, "FINISHER", 240.0);
            seedFeedIssue(farm1, broiler, starterFeed, today.minusDays(22), 310.0, "STARTER", 850.0);

            MedicineRecord mr = new MedicineRecord();
            mr.setFarmId(farm1.getId());
            mr.setBatchId(broiler.getId());
            mr.setBatchCode(broiler.getBatchCode());
            mr.setBatchName(broiler.getBatchName());
            mr.setInventoryItemId(vimeralMed.getId());
            mr.setMedicineName(vimeralMed.getItemName());
            mr.setMedicineBatchLot(vimeralMed.getBatchLotNumber());
            mr.setExpiryDate(vimeralMed.getExpiryDate());
            mr.setAdministrationDate(today.minusDays(4));
            mr.setQuantityUsed(1.5);
            mr.setUnit("LITERS");
            mr.setDosageRecord("1 ml per 2 Liters cool morning drinking water");
            mr.setRoute("DRINKING_WATER");
            mr.setDurationDays(3);
            mr.setWithdrawalPeriodDays(0);
            mr.setDiseaseOrPurpose("Heat-stress & growth immunity support");
            mr.setUnitCostInr(490.0);
            mr.setTotalMedicineCostInr(735.0);
            mr.setRemainingStockAfter(12.0);
            mr.setVeterinaryDoctorName("Dr. Sneha Joshi (BVSc)");
            mr.setVeterinaryRemarks("Flock active; zero withdrawal needed for vitamins.");
            mr.setRecordedBy("manager1");
            medicineRepository.save(mr);

            List<VaccinationRecord> broilerVax = feedAndHealthService.buildStandardBreedScheduleDocuments(broiler, true);
            vaccinationRepository.saveAll(broilerVax);
        }

        if (layer != null) {
            seedFeedIssue(farm1, layer, layerMash, today.minusDays(1), 428.0, "LAYER_MASH", 3200.0);
            List<VaccinationRecord> layerVax = feedAndHealthService.buildStandardBreedScheduleDocuments(layer, true);
            vaccinationRepository.saveAll(layerVax);
        }

        if (farm2 != null) {
            Supplier supMp = createSup(farm2, "SUP-MP-DESI01", "Malwa Desi Grain & Herbal Vet Supply", "Mohan Patidar",
                    "9425077881", "23AABCM1122D1Z4", "Jhabua Mandi Road", "Jhabua", "Madhya Pradesh", List.of("FEED", "MEDICINE", "VACCINE"));
            createItem(farm2, "FEED-DESIMIX", "Kadaknath Maize-Bajra-Azolla Desi Mix", "FEED", "DESI_GRAIN_MIX",
                    "KG", 920.0, 200.0, 31.0, "MP-DM-12", today.plusDays(90), supMp, "Jhabua Shed Store");
            if (kadaknath != null) {
                List<VaccinationRecord> desiVax = feedAndHealthService.buildStandardBreedScheduleDocuments(kadaknath, true);
                vaccinationRepository.saveAll(desiVax);
            }
            alertService.refreshFarmAlertsInternal(farm2.getId());
        }

        alertService.refreshFarmAlertsInternal(farm1.getId());
        log.info("Seeded Phase 3 suppliers, 6-category inventory, purchases, stock movements, feed/medicine logs & breed vaccination schedules!");
    }

    private Supplier createSup(Farm farm, String code, String name, String person, String phone,
                               String gst, String addr, String city, String state, List<String> cats) {
        Supplier s = new Supplier();
        s.setFarmId(farm.getId());
        s.setFarmName(farm.getFarmName());
        s.setSupplierCode(code);
        s.setSupplierName(name);
        s.setContactPerson(person);
        s.setPhone(phone);
        s.setGstNumber(gst);
        s.setAddress(addr);
        s.setCity(city);
        s.setState(state);
        s.setSuppliedCategories(cats);
        return supplierRepository.save(s);
    }

    private InventoryItem createItem(Farm farm, String code, String name, String cat, String subCat,
                                     String unit, double stock, double reorder, double cost,
                                     String lot, LocalDate expiry, Supplier sup, String loc) {
        InventoryItem i = new InventoryItem();
        i.setFarmId(farm.getId());
        i.setFarmName(farm.getFarmName());
        i.setItemCode(code);
        i.setItemName(name);
        i.setCategory(cat);
        i.setSubCategory(subCat);
        i.setUnit(unit);
        i.setCurrentStock(stock);
        i.setReorderLevel(reorder);
        i.setUnitCostInr(cost);
        i.setBatchLotNumber(lot);
        i.setExpiryDate(expiry);
        if (sup != null) {
            i.setPreferredSupplierId(sup.getId());
            i.setPreferredSupplierName(sup.getSupplierName());
        }
        i.setStorageLocation(loc);
        i.recalculateValuation();
        InventoryItem saved = inventoryRepository.save(i);

        StockMovement sm = new StockMovement();
        sm.setFarmId(farm.getId());
        sm.setFarmName(farm.getFarmName());
        sm.setInventoryItemId(saved.getId());
        sm.setItemCode(saved.getItemCode());
        sm.setItemName(saved.getItemName());
        sm.setCategory(saved.getCategory());
        sm.setMovementType("OPENING_STOCK");
        sm.setQuantity(stock);
        sm.setUnit(unit);
        sm.setStockBefore(0.0);
        sm.setStockAfter(stock);
        sm.setUnitCostInr(cost);
        sm.setTotalMovementValueInr(Math.round(stock * cost * 100.0) / 100.0);
        sm.setReferenceType("SEED");
        sm.setRemarks("Initial stock balance");
        sm.setPerformedBy("farmadmin1");
        sm.setMovementAt(LocalDateTime.now().minusDays(5));
        movementRepository.save(sm);
        return saved;
    }

    private void seedPurchase(Farm farm, Supplier sup, InventoryItem item, String invNo, LocalDate date,
                              double qty, double rate, double transport, double paid) {
        double total = Math.round(((qty * rate) + transport) * 100.0) / 100.0;
        double bal = Math.round(Math.max(0.0, total - paid) * 100.0) / 100.0;
        Purchase p = new Purchase();
        p.setFarmId(farm.getId());
        p.setFarmName(farm.getFarmName());
        p.setInvoiceNumber(invNo);
        p.setPurchaseDate(date);
        p.setSupplierId(sup.getId());
        p.setSupplierName(sup.getSupplierName());
        p.setInventoryItemId(item.getId());
        p.setItemCode(item.getItemCode());
        p.setItemName(item.getItemName());
        p.setCategory(item.getCategory());
        p.setSubCategory(item.getSubCategory());
        p.setQuantityPurchased(qty);
        p.setUnit(item.getUnit());
        p.setPurchaseRatePerUnit(rate);
        p.setTransportAndOtherCharges(transport);
        p.setTotalInvoiceAmount(total);
        p.setAmountPaid(paid);
        p.setBalancePayable(bal);
        p.setPaymentStatus(bal <= 0 ? "PAID" : "PARTIAL");
        p.setBatchLotNumber(item.getBatchLotNumber());
        p.setExpiryDate(item.getExpiryDate());
        p.setRecordedBy("farmadmin1");
        Purchase saved = purchaseRepository.save(p);

        StockMovement sm = new StockMovement();
        sm.setFarmId(farm.getId());
        sm.setFarmName(farm.getFarmName());
        sm.setInventoryItemId(item.getId());
        sm.setItemCode(item.getItemCode());
        sm.setItemName(item.getItemName());
        sm.setCategory(item.getCategory());
        sm.setMovementType("PURCHASE_IN");
        sm.setQuantity(qty);
        sm.setUnit(item.getUnit());
        sm.setStockBefore(0.0);
        sm.setStockAfter(qty);
        sm.setUnitCostInr(rate);
        sm.setTotalMovementValueInr(Math.round(qty * rate * 100.0) / 100.0);
        sm.setReferenceType("PURCHASE");
        sm.setReferenceId(saved.getId());
        sm.setRemarks("Invoice #" + invNo + " from " + sup.getSupplierName());
        sm.setPerformedBy("farmadmin1");
        sm.setMovementAt(date.atTime(10, 30));
        movementRepository.save(sm);
    }

    private void seedFeedIssue(Farm farm, Batch batch, InventoryItem feed, LocalDate date,
                               double qtyKg, String feedCat, double remAfter) {
        FeedRecord fr = new FeedRecord();
        fr.setFarmId(farm.getId());
        fr.setBatchId(batch.getId());
        fr.setBatchCode(batch.getBatchCode());
        fr.setBatchName(batch.getBatchName());
        fr.setInventoryItemId(feed.getId());
        fr.setFeedItemName(feed.getItemName());
        fr.setFeedCategory(feedCat);
        fr.setSupplierName(feed.getPreferredSupplierName());
        fr.setRecordDate(date);
        fr.setQuantityIssuedKg(qtyKg);
        fr.setPurchaseRatePerKg(feed.getUnitCostInr());
        fr.setTotalFeedCostInr(Math.round(qtyKg * feed.getUnitCostInr() * 100.0) / 100.0);
        fr.setRemainingStockKg(remAfter);
        fr.setRemarks("Daily shed ration issued from godown");
        fr.setRecordedBy("farmer1");
        FeedRecord saved = feedRepository.save(fr);

        StockMovement sm = new StockMovement();
        sm.setFarmId(farm.getId());
        sm.setFarmName(farm.getFarmName());
        sm.setInventoryItemId(feed.getId());
        sm.setItemCode(feed.getItemCode());
        sm.setItemName(feed.getItemName());
        sm.setCategory("FEED");
        sm.setMovementType("BATCH_ISSUE");
        sm.setQuantity(qtyKg);
        sm.setUnit("KG");
        sm.setStockBefore(remAfter + qtyKg);
        sm.setStockAfter(remAfter);
        sm.setUnitCostInr(feed.getUnitCostInr());
        sm.setTotalMovementValueInr(fr.getTotalFeedCostInr());
        sm.setReferenceType("FEED_RECORD");
        sm.setReferenceId(saved.getId());
        sm.setBatchId(batch.getId());
        sm.setBatchCode(batch.getBatchCode());
        sm.setRemarks("Issued " + feedCat + " feed to " + batch.getBatchCode());
        sm.setPerformedBy("farmer1");
        sm.setMovementAt(date.atTime(7, 15));
        movementRepository.save(sm);
    }
}
