package com.poultry.smartfarm.batch.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.document.MortalityRecord;
import com.poultry.smartfarm.batch.document.WeightRecord;
import com.poultry.smartfarm.batch.dto.DailyRecordRequest;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.repository.MortalityRecordRepository;
import com.poultry.smartfarm.batch.repository.WeightRecordRepository;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.common.exception.ResourceNotFoundException;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DailyRecordService {

    private final DailyRecordRepository dailyRecordRepository;
    private final MortalityRecordRepository mortalityRepository;
    private final WeightRecordRepository weightRepository;
    private final BatchRepository batchRepository;
    private final BreedRepository breedRepository;
    private final BatchService batchService;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public DailyRecordService(DailyRecordRepository dailyRecordRepository,
                              MortalityRecordRepository mortalityRepository,
                              WeightRecordRepository weightRepository,
                              BatchRepository batchRepository,
                              BreedRepository breedRepository,
                              BatchService batchService,
                              FarmAccessGuard farmAccessGuard,
                              AuditLogService auditLogService) {
        this.dailyRecordRepository = dailyRecordRepository;
        this.mortalityRepository = mortalityRepository;
        this.weightRepository = weightRepository;
        this.batchRepository = batchRepository;
        this.breedRepository = breedRepository;
        this.batchService = batchService;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    public List<DailyRecord> getRecordsForBatch(String batchId) {
        batchService.getBatchDocumentWithAccess(batchId);
        return dailyRecordRepository.findByBatchIdOrderByRecordDateDesc(batchId);
    }

    public DailyRecord createDailyRecord(DailyRecordRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(req.getBatchId());
        batchService.ensureBatchEditable(batch);

        if (req.getRecordDate().isBefore(batch.getChickArrivalDate())) {
            throw new BadRequestException("Daily record date (" + req.getRecordDate()
                    + ") cannot be earlier than chick arrival date (" + batch.getChickArrivalDate() + ").");
        }

        if (dailyRecordRepository.existsByBatchIdAndRecordDate(batch.getId(), req.getRecordDate())) {
            throw new BadRequestException("Duplicate daily record blocked: A record already exists for batch "
                    + batch.getBatchCode() + " on " + req.getRecordDate()
                    + ". Use Authorized Correction to update an existing date.");
        }

        DailyRecord record = new DailyRecord();
        record.setFarmId(batch.getFarmId());
        record.setBatchId(batch.getId());
        record.setBatchCode(batch.getBatchCode());
        record.setRecordDate(req.getRecordDate());
        int dayNum = (batch.getInitialBirdAgeDays() != null ? batch.getInitialBirdAgeDays() : 0)
                + (int) ChronoUnit.DAYS.between(batch.getChickArrivalDate(), req.getRecordDate()) + 1;
        record.setDayNumber(Math.max(1, dayNum));

        copyEditableFields(req, record, actor.getUsername());

        // Temporary opening/closing before full chronological recalculation
        int opening = dailyRecordRepository
                .findFirstByBatchIdAndRecordDateBeforeOrderByRecordDateDesc(batch.getId(), req.getRecordDate())
                .map(DailyRecord::getClosingBirds)
                .orElse(batch.getStartingBirdQuantity());
        int closing = opening - record.getMortality() - record.getCulls() + record.getAdditions() - record.getTransfersOut();
        if (closing < 0) {
            throw new BadRequestException("Invalid bird counts: Closing birds would become negative (" + closing
                    + "). Opening birds available: " + opening + ".");
        }
        record.setOpeningBirds(opening);
        record.setClosingBirds(closing);

        DailyRecord saved = dailyRecordRepository.save(record);
        recalculateChronologicalChainAndBatchTotals(batch);
        syncMortalityAndWeightSideRecords(batch, saved, actor.getUsername());

        auditLogService.record(actor, batch.getFarmId(), "DAILY_RECORD_ADDED", "DAILY_RECORD", saved.getId(),
                "Added Day " + saved.getDayNumber() + " record (" + saved.getRecordDate() + ") for " + batch.getBatchCode()
                        + ": closing=" + saved.getClosingBirds() + ", feed=" + saved.getFeedConsumptionKg() + "kg");
        return dailyRecordRepository.findById(saved.getId()).orElse(saved);
    }

    public DailyRecord correctDailyRecord(String recordId, DailyRecordRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        if (actor.getRole() != Role.SUPER_ADMIN && actor.getRole() != Role.FARM_ADMIN && actor.getRole() != Role.FARM_MANAGER) {
            throw new AccessDeniedException("Only Super Admin, Farm Admin, or Farm Manager can perform an authorized daily record correction.");
        }
        if (req.getCorrectionReason() == null || req.getCorrectionReason().trim().length() < 4) {
            throw new BadRequestException("Please provide a correction reason (minimum 4 characters) for audit history.");
        }

        DailyRecord existing = dailyRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily record not found: " + recordId));
        Batch batch = batchService.getBatchDocumentWithAccess(existing.getBatchId());
        farmAccessGuard.requireFarmWriteAccess(batch.getFarmId());
        batchService.ensureBatchEditable(batch);

        copyEditableFields(req, existing, actor.getUsername());
        existing.setCorrectionReason(req.getCorrectionReason().trim());
        dailyRecordRepository.save(existing);

        recalculateChronologicalChainAndBatchTotals(batch);
        syncMortalityAndWeightSideRecords(batch, existing, actor.getUsername());

        auditLogService.record(actor, batch.getFarmId(), "DAILY_RECORD_CORRECTED", "DAILY_RECORD", existing.getId(),
                "Corrected record " + existing.getRecordDate() + " on " + batch.getBatchCode()
                        + ". Reason: " + existing.getCorrectionReason());
        return dailyRecordRepository.findById(existing.getId()).orElse(existing);
    }

    public void recalculateChronologicalChainAndBatchTotals(Batch batch) {
        List<DailyRecord> chronological = dailyRecordRepository.findByBatchIdOrderByRecordDateAsc(batch.getId());
        int runningOpening = batch.getStartingBirdQuantity();
        int sumMort = 0;
        int sumCulls = 0;
        int sumAdd = 0;
        int sumTrans = 0;
        Double latestWeight = batch.getChickInitialWeightGrams();

        for (DailyRecord r : chronological) {
            int closing = runningOpening - r.getMortality() - r.getCulls() + r.getAdditions() - r.getTransfersOut();
            if (closing < 0) {
                throw new BadRequestException("Correction rejected: Bird count on " + r.getRecordDate()
                        + " becomes negative (" + closing + ").");
            }
            r.setOpeningBirds(runningOpening);
            r.setClosingBirds(closing);
            dailyRecordRepository.save(r);

            runningOpening = closing;
            sumMort += r.getMortality();
            sumCulls += r.getCulls();
            sumAdd += r.getAdditions();
            sumTrans += r.getTransfersOut();
            if (r.getAverageBirdWeightGrams() != null && r.getAverageBirdWeightGrams() > 0) {
                latestWeight = r.getAverageBirdWeightGrams();
            }
        }

        batch.setTotalMortality(sumMort);
        batch.setTotalCulls(sumCulls);
        batch.setTotalAdditions(sumAdd);
        batch.setTotalTransfersOut(sumTrans);
        if (latestWeight != null && latestWeight > 0) {
            batch.setLatestAverageWeightGrams(latestWeight);
        }
        batchRepository.save(batch);
        batchService.syncHouseOccupancy(batch.getPoultryHouseId());
    }

    private void syncMortalityAndWeightSideRecords(Batch batch, DailyRecord saved, String username) {
        if (saved.getMortality() > 0 || saved.getCulls() > 0) {
            MortalityRecord mr = mortalityRepository.findByDailyRecordId(saved.getId()).orElseGet(MortalityRecord::new);
            mr.setFarmId(batch.getFarmId());
            mr.setBatchId(batch.getId());
            mr.setBatchCode(batch.getBatchCode());
            mr.setDailyRecordId(saved.getId());
            mr.setRecordDate(saved.getRecordDate());
            mr.setAgeDays(saved.getDayNumber());
            mr.setMortalityCount(saved.getMortality());
            mr.setCullCount(saved.getCulls());
            mr.setCause(saved.getMortalityCause() != null && !saved.getMortalityCause().isBlank()
                    ? saved.getMortalityCause() : "Routine daily observation");
            mr.setRecordedBy(username);
            mortalityRepository.save(mr);
        }

        if (saved.getAverageBirdWeightGrams() != null && saved.getAverageBirdWeightGrams() > 0) {
            WeightRecord wr = weightRepository.findByBatchIdAndRecordDate(batch.getId(), saved.getRecordDate())
                    .orElseGet(WeightRecord::new);
            wr.setFarmId(batch.getFarmId());
            wr.setBatchId(batch.getId());
            wr.setBatchCode(batch.getBatchCode());
            wr.setRecordDate(saved.getRecordDate());
            wr.setAgeDays(saved.getDayNumber());
            wr.setWeekNumber(Math.max(1, (int) Math.ceil(saved.getDayNumber() / 7.0)));
            wr.setAverageWeightGrams(saved.getAverageBirdWeightGrams());
            if (wr.getUniformityPct() == null) wr.setUniformityPct(86.0);
            wr.setExpectedBreedWeightGrams(estimateBreedWeightAtAge(batch, saved.getDayNumber()));
            wr.setRecordedBy(username);
            weightRepository.save(wr);
        }
    }

    public double estimateBreedWeightAtAge(Batch batch, int ageDays) {
        Breed breed = batch.getBreedId() != null ? breedRepository.findById(batch.getBreedId()).orElse(null) : null;
        int marketAge = (breed != null && breed.getExpectedGrowth() != null && breed.getExpectedGrowth().getStandardMarketAgeDays() != null)
                ? breed.getExpectedGrowth().getStandardMarketAgeDays() : 42;
        double targetWt = (breed != null && breed.getExpectedGrowth() != null && breed.getExpectedGrowth().getExpectedMarketWeightGrams() != null)
                ? breed.getExpectedGrowth().getExpectedMarketWeightGrams() : 2400.0;
        double initWt = batch.getChickInitialWeightGrams();
        double progress = Math.min(1.25, Math.max(0.0, (double) ageDays / Math.max(1, marketAge)));
        // Realistic biological S-curve / power progression per breed
        return Math.round((initWt + (targetWt - initWt) * Math.pow(progress, 1.18)) * 10.0) / 10.0;
    }

    private void copyEditableFields(DailyRecordRequest req, DailyRecord r, String username) {
        r.setMortality(req.getMortality());
        r.setCulls(req.getCulls());
        r.setAdditions(req.getAdditions());
        r.setTransfersOut(req.getTransfersOut());
        r.setMortalityCause(req.getMortalityCause());
        r.setFeedType(req.getFeedType() != null ? req.getFeedType().trim().toUpperCase() : "STARTER");
        r.setFeedConsumptionKg(req.getFeedConsumptionKg());
        r.setAverageBirdWeightGrams(req.getAverageBirdWeightGrams());
        r.setWaterConsumptionLiters(req.getWaterConsumptionLiters());
        r.setTemperatureCelsius(req.getTemperatureCelsius());
        r.setHumidityPct(req.getHumidityPct());
        r.setMedicineGiven(req.getMedicineGiven());
        r.setVaccinationGiven(req.getVaccinationGiven());
        r.setLabourCost(req.getLabourCost());
        r.setElectricityCost(req.getElectricityCost());
        r.setOtherExpenses(req.getOtherExpenses());
        r.setFarmerRemarks(req.getFarmerRemarks());
        r.setRecordedBy(username);
    }
}
