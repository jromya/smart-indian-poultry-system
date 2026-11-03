package com.poultry.smartfarm.inventory.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.inventory.document.FeedRecord;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.document.MedicineRecord;
import com.poultry.smartfarm.inventory.document.VaccinationRecord;
import com.poultry.smartfarm.inventory.dto.FeedIssueRequest;
import com.poultry.smartfarm.inventory.dto.MedicineRecordRequest;
import com.poultry.smartfarm.inventory.dto.VaccinationCompleteRequest;
import com.poultry.smartfarm.inventory.dto.VaccinationScheduleRequest;
import com.poultry.smartfarm.inventory.repository.FeedRecordRepository;
import com.poultry.smartfarm.inventory.repository.MedicineRecordRepository;
import com.poultry.smartfarm.inventory.repository.VaccinationRecordRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FeedAndHealthService {

    private final FeedRecordRepository feedRepository;
    private final MedicineRecordRepository medicineRepository;
    private final VaccinationRecordRepository vaccinationRepository;
    private final BatchService batchService;
    private final BatchRepository batchRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final InventoryService inventoryService;
    private final NotificationAlertService alertService;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public FeedAndHealthService(FeedRecordRepository feedRepository,
                                MedicineRecordRepository medicineRepository,
                                VaccinationRecordRepository vaccinationRepository,
                                BatchService batchService,
                                BatchRepository batchRepository,
                                DailyRecordRepository dailyRecordRepository,
                                InventoryService inventoryService,
                                NotificationAlertService alertService,
                                FarmAccessGuard farmAccessGuard,
                                AuditLogService auditLogService) {
        this.feedRepository = feedRepository;
        this.medicineRepository = medicineRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.batchService = batchService;
        this.batchRepository = batchRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.inventoryService = inventoryService;
        this.alertService = alertService;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    // =========================================================================
    // 1. FEED ALLOCATION & AUTOMATIC INVENTORY REDUCTION
    // =========================================================================
    public List<FeedRecord> listFeedRecords(String farmId, String batchId) {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (batchId != null && !batchId.isBlank()) {
            batchService.getBatchDocumentWithAccess(batchId);
            return feedRepository.findByBatchIdOrderByRecordDateDescCreatedAtDesc(batchId);
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return feedRepository.findByFarmIdOrderByRecordDateDescCreatedAtDesc(farmId);
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return feedRepository.findAll();
        }
        if (currentUser.getRole().isFarmRole() && currentUser.getAssignedFarmIds() != null && !currentUser.getAssignedFarmIds().isEmpty()) {
            return feedRepository.findByFarmIdInOrderByRecordDateDescCreatedAtDesc(currentUser.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public FeedRecord issueFeedToBatch(FeedIssueRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(req.getBatchId());
        batchService.ensureBatchEditable(batch);

        InventoryItem feedItem = inventoryService.getItemWithAccess(req.getInventoryItemId());
        if (!"FEED".equalsIgnoreCase(feedItem.getCategory())) {
            throw new BadRequestException("Selected inventory item '" + feedItem.getItemName()
                    + "' is in category " + feedItem.getCategory() + ", not FEED.");
        }
        if (feedItem.getExpiryDate() != null && feedItem.getExpiryDate().isBefore(req.getRecordDate())) {
            throw new BadRequestException("Safety block: Feed '" + feedItem.getItemName()
                    + "' expired on " + feedItem.getExpiryDate() + "! Do not issue expired feed.");
        }

        String feedCategory = (req.getFeedCategory() != null && !req.getFeedCategory().isBlank())
                ? req.getFeedCategory().trim().toUpperCase()
                : (feedItem.getSubCategory() != null ? feedItem.getSubCategory() : "STARTER");

        double qtyKg = round2(req.getQuantityIssuedKg());
        double ratePerKg = feedItem.getUnitCostInr();
        double totalCost = round2(qtyKg * ratePerKg);

        FeedRecord fr = new FeedRecord();
        fr.setFarmId(batch.getFarmId());
        fr.setBatchId(batch.getId());
        fr.setBatchCode(batch.getBatchCode());
        fr.setBatchName(batch.getBatchName());
        fr.setInventoryItemId(feedItem.getId());
        fr.setFeedItemName(feedItem.getItemName());
        fr.setFeedCategory(feedCategory);
        fr.setSupplierName(feedItem.getPreferredSupplierName());
        fr.setRecordDate(req.getRecordDate());
        fr.setQuantityIssuedKg(qtyKg);
        fr.setPurchaseRatePerKg(ratePerKg);
        fr.setTotalFeedCostInr(totalCost);
        fr.setRemarks(req.getRemarks());
        fr.setRecordedBy(actor.getUsername());
        FeedRecord saved = feedRepository.save(fr);

        // Automatically reduce inventory stock & record BATCH_ISSUE movement
        InventoryItem updatedStock = inventoryService.deductItemForOperation(
                feedItem.getId(), qtyKg, "FEED_RECORD", saved.getId(), batch,
                "Feed issued (" + feedCategory + ") to " + batch.getBatchCode(), actor);

        saved.setRemainingStockKg(updatedStock.getCurrentStock());
        saved = feedRepository.save(saved);

        // Update batch feed rate reference
        if (ratePerKg > 0) {
            batch.setEstimatedFeedRatePerKg(ratePerKg);
            batchRepository.save(batch);
        }

        // Sync to DailyRecord on that date if requested and record exists
        if (req.isSyncDailyRecordFeed()) {
            dailyRecordRepository.findByBatchIdAndRecordDate(batch.getId(), req.getRecordDate()).ifPresent(dr -> {
                dr.setFeedConsumptionKg(qtyKg);
                dr.setFeedType(feedCategory);
                dailyRecordRepository.save(dr);
            });
        }

        auditLogService.record(actor, batch.getFarmId(), "FEED_ISSUED_TO_BATCH", "FEED_RECORD", saved.getId(),
                "Issued " + qtyKg + " kg of " + feedItem.getItemName() + " to " + batch.getBatchCode()
                        + " (Cost ₹" + totalCost + ", Remaining stock " + updatedStock.getCurrentStock() + " kg)");
        return saved;
    }

    // =========================================================================
    // 2. MEDICINE ADMINISTRATION & AUTOMATIC INVENTORY REDUCTION
    // =========================================================================
    public List<MedicineRecord> listMedicineRecords(String farmId, String batchId) {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (batchId != null && !batchId.isBlank()) {
            batchService.getBatchDocumentWithAccess(batchId);
            return medicineRepository.findByBatchIdOrderByAdministrationDateDescCreatedAtDesc(batchId);
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            return medicineRepository.findByFarmIdOrderByAdministrationDateDescCreatedAtDesc(farmId);
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return medicineRepository.findAll();
        }
        if (currentUser.getRole().isFarmRole() && currentUser.getAssignedFarmIds() != null && !currentUser.getAssignedFarmIds().isEmpty()) {
            return medicineRepository.findByFarmIdInOrderByAdministrationDateDescCreatedAtDesc(currentUser.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public MedicineRecord recordMedicineAdministration(MedicineRecordRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(req.getBatchId());
        batchService.ensureBatchEditable(batch);

        InventoryItem medItem = inventoryService.getItemWithAccess(req.getInventoryItemId());
        if (!"MEDICINE".equalsIgnoreCase(medItem.getCategory()) && !"VACCINE".equalsIgnoreCase(medItem.getCategory())) {
            throw new BadRequestException("Selected item '" + medItem.getItemName() + "' is not a Medicine/Supplement.");
        }
        if (medItem.getExpiryDate() != null && medItem.getExpiryDate().isBefore(req.getAdministrationDate())) {
            throw new BadRequestException("Veterinary safety block: '" + medItem.getItemName()
                    + "' expired on " + medItem.getExpiryDate() + "! Cannot administer expired medicine.");
        }

        double qty = round2(req.getQuantityUsed());
        double unitCost = medItem.getUnitCostInr();
        double totalCost = round2(qty * unitCost);

        MedicineRecord mr = new MedicineRecord();
        mr.setFarmId(batch.getFarmId());
        mr.setBatchId(batch.getId());
        mr.setBatchCode(batch.getBatchCode());
        mr.setBatchName(batch.getBatchName());
        mr.setInventoryItemId(medItem.getId());
        mr.setMedicineName(medItem.getItemName());
        mr.setMedicineBatchLot(medItem.getBatchLotNumber());
        mr.setExpiryDate(medItem.getExpiryDate());
        mr.setAdministrationDate(req.getAdministrationDate());
        mr.setQuantityUsed(qty);
        mr.setUnit(medItem.getUnit());
        mr.setDosageRecord(req.getDosageRecord().trim());
        mr.setRoute(req.getRoute() != null ? req.getRoute().trim().toUpperCase() : "DRINKING_WATER");
        mr.setDurationDays(req.getDurationDays());
        mr.setWithdrawalPeriodDays(req.getWithdrawalPeriodDays());
        mr.setDiseaseOrPurpose(req.getDiseaseOrPurpose());
        mr.setUnitCostInr(unitCost);
        mr.setTotalMedicineCostInr(totalCost);
        mr.setVeterinaryDoctorName(req.getVeterinaryDoctorName());
        mr.setVeterinaryRemarks(req.getVeterinaryRemarks());
        mr.setRecordedBy(actor.getUsername());
        MedicineRecord saved = medicineRepository.save(mr);

        // Automatically deduct medicine stock & record BATCH_ISSUE stock movement
        InventoryItem updatedStock = inventoryService.deductItemForOperation(
                medItem.getId(), qty, "MEDICINE_RECORD", saved.getId(), batch,
                "Medicine administered (" + medItem.getItemName() + ") to " + batch.getBatchCode(), actor);

        saved.setRemainingStockAfter(updatedStock.getCurrentStock());
        saved = medicineRepository.save(saved);

        // Sync medicine note to DailyRecord if present on that date
        dailyRecordRepository.findByBatchIdAndRecordDate(batch.getId(), req.getAdministrationDate()).ifPresent(dr -> {
            dr.setMedicineGiven(medItem.getItemName() + " (" + req.getDosageRecord().trim() + ")");
            dailyRecordRepository.save(dr);
        });

        auditLogService.record(actor, batch.getFarmId(), "MEDICINE_ADMINISTERED", "MEDICINE_RECORD", saved.getId(),
                "Administered " + qty + " " + medItem.getUnit() + " of " + medItem.getItemName()
                        + " to " + batch.getBatchCode() + " (Remaining: " + updatedStock.getCurrentStock() + " " + medItem.getUnit() + ")");
        return saved;
    }

    // =========================================================================
    // 3. BREED-CONFIGURABLE VACCINATION SCHEDULES & COMPLETION
    // =========================================================================
    public List<VaccinationRecord> listVaccinations(String farmId, String batchId) {
        User currentUser = farmAccessGuard.getCurrentUser();
        if (batchId != null && !batchId.isBlank()) {
            Batch b = batchService.getBatchDocumentWithAccess(batchId);
            alertService.refreshFarmAlertsInternal(b.getFarmId());
            return vaccinationRepository.findByBatchIdOrderByScheduledDateAsc(batchId);
        }
        if (farmId != null && !farmId.isBlank()) {
            farmAccessGuard.requireFarmAccess(farmId);
            alertService.refreshFarmAlertsInternal(farmId);
            return vaccinationRepository.findByFarmIdOrderByScheduledDateAsc(farmId);
        }
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return vaccinationRepository.findAll();
        }
        if (currentUser.getRole().isFarmRole() && currentUser.getAssignedFarmIds() != null && !currentUser.getAssignedFarmIds().isEmpty()) {
            return vaccinationRepository.findByFarmIdInOrderByScheduledDateAsc(currentUser.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }

    public VaccinationRecord addCustomVaccinationSchedule(VaccinationScheduleRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(req.getBatchId());
        farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());

        VaccinationRecord v = new VaccinationRecord();
        v.setFarmId(batch.getFarmId());
        v.setBatchId(batch.getId());
        v.setBatchCode(batch.getBatchCode());
        v.setBatchName(batch.getBatchName());
        v.setBreedCode(batch.getBreedCode());
        v.setBreedName(batch.getBreedName());
        v.setVaccineName(req.getVaccineName().trim());
        v.setTargetDisease(req.getTargetDisease().trim());
        v.setRecommendedAgeDays(req.getRecommendedAgeDays());
        v.setScheduledDate(req.getScheduledDate());
        v.setRoute(req.getRoute() != null ? req.getRoute().trim().toUpperCase() : "EYE_DROP");
        v.setDosagePerBird(req.getDosagePerBird());
        v.setVeterinaryGuidanceNotes(req.getVeterinaryGuidanceNotes());
        v.setStatus(req.getScheduledDate().isBefore(LocalDate.now()) ? "OVERDUE" : "SCHEDULED");

        VaccinationRecord saved = vaccinationRepository.save(v);
        alertService.refreshFarmAlertsInternal(batch.getFarmId());

        auditLogService.record(actor, batch.getFarmId(), "VACCINATION_SCHEDULED", "VACCINATION_RECORD", saved.getId(),
                "Scheduled vaccine " + saved.getVaccineName() + " for " + batch.getBatchCode() + " on " + saved.getScheduledDate());
        return saved;
    }

    /**
     * Generates a breed-specific Indian veterinary vaccination schedule based on batch category & breed!
     */
    public List<VaccinationRecord> generateBreedSpecificSchedule(String batchId) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(batchId);
        farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());

        if (vaccinationRepository.countByBatchId(batch.getId()) > 0) {
            throw new BadRequestException("Batch " + batch.getBatchCode()
                    + " already has a vaccination schedule. Add custom vaccine rows individually if needed.");
        }

        List<VaccinationRecord> generated = buildStandardBreedScheduleDocuments(batch, false);
        List<VaccinationRecord> saved = vaccinationRepository.saveAll(generated);
        alertService.refreshFarmAlertsInternal(batch.getFarmId());

        auditLogService.record(actor, batch.getFarmId(), "BREED_VACCINE_SCHEDULE_GENERATED", "BATCH", batch.getId(),
                "Generated " + saved.size() + " veterinary vaccine milestones tailored for " + batch.getBreedName()
                        + " (" + batch.getBatchCategory() + ")");
        return saved;
    }

    public List<VaccinationRecord> buildStandardBreedScheduleDocuments(Batch batch, boolean markPastAsCompletedForSeed) {
        List<VaccinationRecord> list = new ArrayList<>();
        String cat = batch.getBatchCategory() != null ? batch.getBatchCategory().toUpperCase() : "BROILER";

        if ("BROILER".equals(cat)) {
            list.add(makeVax(batch, 1, "Marek's Disease HVT", "Marek's Disease", "SUBCUTANEOUS", "0.2 ml neck S/C at hatchery",
                    "Commercial broiler day-1 immunity", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 6, "Ranikhet Lasota / F1 Strain", "Newcastle (Ranikhet) Disease", "EYE_DROP", "1 eye drop per chick",
                    "Keep vaccine chilled on ice; administer early morning", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 13, "IBD / Gumboro Intermediate", "Infectious Bursal Disease", "DRINKING_WATER", "Chlorine-free water + skim milk stabilizer",
                    "Withhold water 1.5 hrs prior in warm weather", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 21, "Ranikhet Lasota Booster", "Newcastle Disease Booster", "DRINKING_WATER", "1 dose/bird in cool morning water",
                    "Essential 3rd-week broiler respiratory protection", markPastAsCompletedForSeed));
        } else if ("LAYER".equals(cat)) {
            list.add(makeVax(batch, 1, "Marek's HVT + Rispens", "Marek's Disease", "SUBCUTANEOUS", "0.2 ml S/C",
                    "Long-cycle layer tumor protection", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 6, "Ranikhet RDF1 + IB", "Newcastle & Infectious Bronchitis", "EYE_DROP", "1 drop/bird",
                    "Primary mucosal immunity", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 14, "IBD / Gumboro Intermediate", "Infectious Bursal Disease", "DRINKING_WATER", "Skim milk stabilized water",
                    "Bursal protection for pullets", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 24, "IBD Gumboro Booster", "Infectious Bursal Disease", "DRINKING_WATER", "1 dose/bird",
                    "Second pullet IBD booster", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 42, "Fowl Pox Live Vaccine", "Fowl Pox (Cutaneous/Diphtheritic)", "WING_WEB", "Wing-web twin needle prick",
                    "Check 'take' swelling after 7 days", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 56, "Ranikhet R2B Mukteswar", "Virulent Newcastle Disease", "INTRAMUSCULAR", "0.5 ml breast/thigh IM",
                    "De-worm flock 5 days before R2B injection", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 115, "ND + IB + EDS Killed Emulsion", "Egg Drop Syndrome & ND", "INTRAMUSCULAR", "0.5 ml IM before point-of-lay",
                    "Protects peak 72-week egg production & shell quality", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 213, "Mid-Lay Lasota Live Booster", "Newcastle Titre Maintenance", "DRINKING_WATER", "1 dose/hen in morning water",
                    "Scheduled mid-lay immunity booster", false));
        } else {
            // INDIGENOUS INDIAN (Aseel, Kadaknath, etc.) & IMPROVED RURAL (Vanaraja, Giriraja, Gramapriya, Srinidhi)
            list.add(makeVax(batch, 1, "Marek's Disease HVT", "Marek's Disease", "SUBCUTANEOUS", "0.2 ml S/C",
                    "ICAR-DPR recommended Day-1 vaccine for Desi & Rural chicks", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 7, "Ranikhet F1 / Lasota", "Ranikhet (Newcastle) Disease", "EYE_DROP", "1 eye/nasal drop per chick",
                    "Critical #1 killer prevention for Indian desi flocks", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 14, "IBD / Gumboro Live", "Gumboro Disease", "DRINKING_WATER", "Cool earthen-pot or plastic drinker water",
                    "Never use metal vessels for live drinking water vaccine", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 28, "Ranikhet Lasota Booster", "Ranikhet Booster", "DRINKING_WATER", "1 dose/bird",
                    "4th-week booster before semi-scavenging/free-range access", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 42, "Fowl Pox Vaccine", "Fowl Pox (Mosquito-borne)", "WING_WEB", "Wing-web stab",
                    "Essential for free-range Kadaknath/Aseel/Vanaraja birds", markPastAsCompletedForSeed));
            list.add(makeVax(batch, 60, "Ranikhet R2B Mukteswar", "Long-term Ranikhet Immunity", "SUBCUTANEOUS", "0.5 ml S/C after deworming",
                    "Provides 6-month protection for 110–180d indigenous batches", markPastAsCompletedForSeed));
        }
        return list;
    }

    private VaccinationRecord makeVax(Batch batch, int ageDays, String vaccineName, String disease,
                                      String route, String dosage, String notes, boolean markPastCompleted) {
        LocalDate sched = batch.getChickArrivalDate().plusDays(Math.max(0, ageDays - 1));
        VaccinationRecord v = new VaccinationRecord();
        v.setFarmId(batch.getFarmId());
        v.setBatchId(batch.getId());
        v.setBatchCode(batch.getBatchCode());
        v.setBatchName(batch.getBatchName());
        v.setBreedCode(batch.getBreedCode());
        v.setBreedName(batch.getBreedName());
        v.setRecommendedAgeDays(ageDays);
        v.setVaccineName(vaccineName);
        v.setTargetDisease(disease);
        v.setScheduledDate(sched);
        v.setRoute(route);
        v.setDosagePerBird(dosage);
        v.setVeterinaryGuidanceNotes(notes);

        if (markPastCompleted && sched.isBefore(LocalDate.now().minusDays(1))) {
            v.setStatus("COMPLETED");
            v.setAdministeredDate(sched);
            v.setDosesUsed((double) batch.getStartingBirdQuantity());
            v.setAdministeredBy("manager1");
        } else if (sched.isBefore(LocalDate.now())) {
            v.setStatus("OVERDUE");
        } else {
            v.setStatus("SCHEDULED");
        }
        return v;
    }

    public VaccinationRecord completeVaccination(String vaccinationId, VaccinationCompleteRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        VaccinationRecord v = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination record not found: " + vaccinationId));
        Batch batch = batchService.getBatchDocumentWithAccess(v.getBatchId());

        if ("COMPLETED".equalsIgnoreCase(v.getStatus())) {
            throw new BadRequestException("This vaccination (" + v.getVaccineName() + ") is already marked COMPLETED.");
        }

        double vaccineCost = 0.0;
        if (req.getInventoryItemId() != null && !req.getInventoryItemId().isBlank() && req.getDosesUsed() > 0) {
            InventoryItem vaxStock = inventoryService.getItemWithAccess(req.getInventoryItemId());
            if (vaxStock.getExpiryDate() != null && vaxStock.getExpiryDate().isBefore(req.getAdministeredDate())) {
                throw new BadRequestException("Safety block: Vaccine stock '" + vaxStock.getItemName()
                        + "' expired on " + vaxStock.getExpiryDate() + "!");
            }
            vaccineCost = round2(req.getDosesUsed() * vaxStock.getUnitCostInr());
            v.setInventoryItemId(vaxStock.getId());
            if (req.getVaccineBatchLot() == null || req.getVaccineBatchLot().isBlank()) {
                v.setVaccineBatchLot(vaxStock.getBatchLotNumber());
            }
            if (req.getExpiryDate() == null) {
                v.setExpiryDate(vaxStock.getExpiryDate());
            }
            inventoryService.deductItemForOperation(vaxStock.getId(), req.getDosesUsed(),
                    "VACCINATION_RECORD", v.getId(), batch,
                    "Vaccine administered: " + v.getVaccineName() + " to " + batch.getBatchCode(), actor);
        }

        v.setAdministeredDate(req.getAdministeredDate());
        v.setDosesUsed(round2(req.getDosesUsed()));
        v.setTotalVaccineCostInr(vaccineCost);
        if (req.getVaccineBatchLot() != null && !req.getVaccineBatchLot().isBlank()) {
            v.setVaccineBatchLot(req.getVaccineBatchLot().trim());
        }
        if (req.getExpiryDate() != null) {
            v.setExpiryDate(req.getExpiryDate());
        }
        if (req.getVeterinaryGuidanceNotes() != null && !req.getVeterinaryGuidanceNotes().isBlank()) {
            v.setVeterinaryGuidanceNotes(req.getVeterinaryGuidanceNotes().trim());
        }
        v.setAdministeredBy(actor.getUsername());
        v.setStatus("COMPLETED");

        VaccinationRecord saved = vaccinationRepository.save(v);

        // Sync vaccination note into DailyRecord if logged on that date
        dailyRecordRepository.findByBatchIdAndRecordDate(batch.getId(), req.getAdministeredDate()).ifPresent(dr -> {
            dr.setVaccinationGiven(saved.getVaccineName() + " (" + saved.getRoute() + ")");
            dailyRecordRepository.save(dr);
        });

        alertService.refreshFarmAlertsInternal(batch.getFarmId());

        auditLogService.record(actor, batch.getFarmId(), "VACCINATION_COMPLETED", "VACCINATION_RECORD", saved.getId(),
                "Completed vaccine " + saved.getVaccineName() + " on " + batch.getBatchCode()
                        + " (" + saved.getDosesUsed() + " doses, Cost ₹" + vaccineCost + ")");
        return saved;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
