package com.poultry.smartfarm.batch.service;

import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.document.EggRecord;
import com.poultry.smartfarm.batch.document.WeightRecord;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import com.poultry.smartfarm.batch.dto.EggRecordRequest;
import com.poultry.smartfarm.batch.dto.WeightRecordRequest;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.repository.EggRecordRepository;
import com.poultry.smartfarm.batch.repository.WeightRecordRepository;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.common.exception.BadRequestException;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PoultryAnalyticsCalculationService {

    private final BatchService batchService;
    private final BatchRepository batchRepository;
    private final DailyRecordRepository dailyRepository;
    private final WeightRecordRepository weightRepository;
    private final EggRecordRepository eggRepository;
    private final BreedRepository breedRepository;
    private final DailyRecordService dailyRecordService;
    private final FarmAccessGuard farmAccessGuard;
    private final AuditLogService auditLogService;

    public PoultryAnalyticsCalculationService(BatchService batchService,
                                              BatchRepository batchRepository,
                                              DailyRecordRepository dailyRepository,
                                              WeightRecordRepository weightRepository,
                                              EggRecordRepository eggRepository,
                                              BreedRepository breedRepository,
                                              DailyRecordService dailyRecordService,
                                              FarmAccessGuard farmAccessGuard,
                                              AuditLogService auditLogService) {
        this.batchService = batchService;
        this.batchRepository = batchRepository;
        this.dailyRepository = dailyRepository;
        this.weightRepository = weightRepository;
        this.eggRepository = eggRepository;
        this.breedRepository = breedRepository;
        this.dailyRecordService = dailyRecordService;
        this.farmAccessGuard = farmAccessGuard;
        this.auditLogService = auditLogService;
    }

    // -------------------------------------------------------------------------
    // 1. DEDICATED WEIGHT MANAGEMENT
    // -------------------------------------------------------------------------
    public List<WeightRecord> getWeightRecords(String batchId) {
        batchService.getBatchDocumentWithAccess(batchId);
        return weightRepository.findByBatchIdOrderByRecordDateAsc(batchId);
    }

    public WeightRecord recordWeightSample(WeightRecordRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(req.getBatchId());
        batchService.ensureBatchEditable(batch);

        int ageDays = (batch.getInitialBirdAgeDays() != null ? batch.getInitialBirdAgeDays() : 0)
                + Math.max(1, (int) ChronoUnit.DAYS.between(batch.getChickArrivalDate(), req.getRecordDate()) + 1);

        WeightRecord wr = weightRepository.findByBatchIdAndRecordDate(batch.getId(), req.getRecordDate())
                .orElseGet(WeightRecord::new);
        wr.setFarmId(batch.getFarmId());
        wr.setBatchId(batch.getId());
        wr.setBatchCode(batch.getBatchCode());
        wr.setRecordDate(req.getRecordDate());
        wr.setAgeDays(ageDays);
        wr.setWeekNumber(Math.max(1, (int) Math.ceil(ageDays / 7.0)));
        wr.setSampleBirdCount(req.getSampleBirdCount());
        wr.setAverageWeightGrams(req.getAverageWeightGrams());
        wr.setMinWeightGrams(req.getMinWeightGrams());
        wr.setMaxWeightGrams(req.getMaxWeightGrams());

        // Auto-estimate uniformity if min/max given and uniformity omitted
        double uniformity = 86.0;
        if (req.getUniformityPct() != null && req.getUniformityPct() > 0) {
            uniformity = req.getUniformityPct();
        } else if (req.getMinWeightGrams() != null && req.getMaxWeightGrams() != null && req.getAverageWeightGrams() > 0) {
            double spreadRatio = (req.getMaxWeightGrams() - req.getMinWeightGrams()) / req.getAverageWeightGrams();
            uniformity = Math.max(55.0, Math.min(98.0, Math.round((100.0 - (spreadRatio * 40.0)) * 10.0) / 10.0));
        }
        wr.setUniformityPct(uniformity);
        wr.setExpectedBreedWeightGrams(dailyRecordService.estimateBreedWeightAtAge(batch, ageDays));
        wr.setRemarks(req.getRemarks());
        wr.setRecordedBy(actor.getUsername());

        WeightRecord saved = weightRepository.save(wr);
        batch.setLatestAverageWeightGrams(saved.getAverageWeightGrams());
        batch.setLatestUniformityPct(saved.getUniformityPct());
        batchRepository.save(batch);

        auditLogService.record(actor, batch.getFarmId(), "WEIGHT_RECORDED", "WEIGHT_RECORD", saved.getId(),
                "Recorded sample avg weight " + saved.getAverageWeightGrams() + "g (Uniformity " + uniformity + "%) on " + batch.getBatchCode());
        return saved;
    }

    // -------------------------------------------------------------------------
    // 2. BROILER & MEAT PERFORMANCE ENGINE (PROPER FCR WITH REMOVALS & PERIOD)
    // -------------------------------------------------------------------------
    public Map<String, Object> calculateBroilerAnalytics(String batchId, LocalDate fromDate, LocalDate toDate) {
        Batch batch = batchService.getBatchDocumentWithAccess(batchId);
        BatchResponse batchView = BatchResponse.fromDocument(batch);
        Breed breed = batch.getBreedId() != null ? breedRepository.findById(batch.getBreedId()).orElse(null) : null;

        LocalDate periodStart = (fromDate != null) ? fromDate : batch.getChickArrivalDate();
        LocalDate periodEnd = (toDate != null) ? toDate : LocalDate.now();

        List<DailyRecord> allDaily = dailyRepository.findByBatchIdOrderByRecordDateAsc(batch.getId());
        List<DailyRecord> periodDaily = allDaily.stream()
                .filter(d -> !d.getRecordDate().isBefore(periodStart) && !d.getRecordDate().isAfter(periodEnd))
                .collect(Collectors.toList());

        List<WeightRecord> allWeights = weightRepository.findByBatchIdOrderByRecordDateAsc(batch.getId());

        // Feed breakdown by phase (Starter, Grower, Finisher)
        double starterFeedKg = 0.0;
        double growerFeedKg = 0.0;
        double finisherFeedKg = 0.0;
        double totalFeedAllDaysKg = 0.0;

        for (DailyRecord r : allDaily) {
            double f = r.getFeedConsumptionKg();
            totalFeedAllDaysKg += f;
            String ft = r.getFeedType() != null ? r.getFeedType().toUpperCase() : "";
            if (ft.contains("STARTER")) starterFeedKg += f;
            else if (ft.contains("GROWER")) growerFeedKg += f;
            else finisherFeedKg += f;
        }

        double periodFeedKg = periodDaily.stream().mapToDouble(DailyRecord::getFeedConsumptionKg).sum();

        // Proper FCR Calculation over selected measurement period accounting for bird removals:
        // Net Live Weight Gain (kg) = (Closing Live Biomass kg + Biomass Removed via Sales/Transfers kg) - Opening Biomass kg
        boolean isFullFromStart = !periodStart.isAfter(batch.getChickArrivalDate());

        int openingBirdsPeriod = isFullFromStart
                ? batch.getStartingBirdQuantity()
                : (!periodDaily.isEmpty() ? periodDaily.get(0).getOpeningBirds() : batchView.getCurrentLiveBirds());

        double openingAvgWeightG = isFullFromStart
                ? batch.getChickInitialWeightGrams()
                : findNearestWeightOnOrBefore(allWeights, periodStart.minusDays(1), batch.getChickInitialWeightGrams());

        int closingLiveBirdsPeriod = (!toDateIsHistorical(toDate))
                ? batchView.getCurrentLiveBirds()
                : (!periodDaily.isEmpty() ? periodDaily.get(periodDaily.size() - 1).getClosingBirds() : openingBirdsPeriod);

        double closingAvgWeightG = (!toDateIsHistorical(toDate))
                ? batch.getLatestAverageWeightGrams()
                : findNearestWeightOnOrBefore(allWeights, periodEnd, batch.getLatestAverageWeightGrams());

        int periodTransfersOut = periodDaily.stream().mapToInt(DailyRecord::getTransfersOut).sum();
        double transferRemovedBiomassKg = (periodTransfersOut * ((openingAvgWeightG + closingAvgWeightG) / 2.0)) / 1000.0;
        double soldRemovedBiomassKg = (!toDateIsHistorical(toDate)) ? batch.getTotalSoldLiveWeightKg() : 0.0;
        double totalRemovedBiomassKg = round2(soldRemovedBiomassKg + transferRemovedBiomassKg);

        double openingBiomassKg = round2((openingBirdsPeriod * openingAvgWeightG) / 1000.0);
        double closingLiveBiomassKg = round2((closingLiveBirdsPeriod * closingAvgWeightG) / 1000.0);
        double netLiveWeightGainKg = round2(Math.max(0.0, (closingLiveBiomassKg + totalRemovedBiomassKg) - openingBiomassKg));

        double calculatedFcr = netLiveWeightGainKg > 0.01
                ? Math.round((periodFeedKg / netLiveWeightGainKg) * 100.0) / 100.0
                : 0.0;

        double targetFcr = (breed != null && breed.getPerformanceTargets() != null && breed.getPerformanceTargets().getTargetFcr() != null)
                ? breed.getPerformanceTargets().getTargetFcr() : 1.65;
        double targetMarketWeightG = (breed != null && breed.getExpectedGrowth() != null && breed.getExpectedGrowth().getExpectedMarketWeightGrams() != null)
                ? breed.getExpectedGrowth().getExpectedMarketWeightGrams() : 2400.0;

        // Average Daily Gain (ADG) & Expected Market-Ready Date
        double currentWtG = batch.getLatestAverageWeightGrams();
        int ageDays = Math.max(1, batchView.getCurrentAgeDays());
        double adgGramsPerDay = Math.max(5.0, (currentWtG - batch.getChickInitialWeightGrams()) / ageDays);
        int daysRemainingToTargetWt = currentWtG >= targetMarketWeightG
                ? 0
                : (int) Math.ceil((targetMarketWeightG - currentWtG) / adgGramsPerDay);
        LocalDate expectedMarketReadyDate = LocalDate.now().plusDays(daysRemainingToTargetWt);

        // Feed cost per bird
        double totalFeedCostInr = round2(totalFeedAllDaysKg * batch.getEstimatedFeedRatePerKg());
        int denominatorBirds = Math.max(1, batchView.getCurrentLiveBirds() + batchView.getTotalBirdsSold());
        double feedCostPerBirdInr = round2(totalFeedCostInr / denominatorBirds);

        double totalLiveWeightOnFarmKg = round2((batchView.getCurrentLiveBirds() * currentWtG) / 1000.0);
        double estimatedLiveFlockValueInr = round2(totalLiveWeightOnFarmKg * batch.getLiveSellingPricePerKg());

        // Weekly Weight Comparison (Week 1 .. Current Week)
        List<Map<String, Object>> weeklyComparison = new ArrayList<>();
        Map<Integer, List<WeightRecord>> byWeek = allWeights.stream()
                .collect(Collectors.groupingBy(w -> w.getWeekNumber() != null ? w.getWeekNumber() : 1));
        int maxWeek = Math.max(1, (int) Math.ceil(ageDays / 7.0));
        for (int wk = 1; wk <= Math.min(maxWeek, 24); wk++) {
            List<WeightRecord> wkList = byWeek.get(wk);
            Double actualWt = (wkList != null && !wkList.isEmpty())
                    ? wkList.get(wkList.size() - 1).getAverageWeightGrams() : null;
            double breedTarget = dailyRecordService.estimateBreedWeightAtAge(batch, wk * 7);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("weekLabel", "Week " + wk + " (Day " + (wk * 7) + ")");
            row.put("actualWeightGrams", actualWt);
            row.put("breedTargetWeightGrams", breedTarget);
            weeklyComparison.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batch", batchView);
        result.put("periodFrom", periodStart);
        result.put("periodTo", periodEnd);
        result.put("starterFeedKg", round2(starterFeedKg));
        result.put("growerFeedKg", round2(growerFeedKg));
        result.put("finisherFeedKg", round2(finisherFeedKg));
        result.put("totalFeedKg", round2(totalFeedAllDaysKg));
        result.put("periodFeedKg", round2(periodFeedKg));
        result.put("openingBiomassKg", openingBiomassKg);
        result.put("closingLiveBiomassKg", closingLiveBiomassKg);
        result.put("removedBiomassKg", totalRemovedBiomassKg);
        result.put("netLiveWeightGainKg", netLiveWeightGainKg);
        result.put("fcr", calculatedFcr);
        result.put("breedTargetFcr", targetFcr);
        result.put("averageWeightGrams", currentWtG);
        result.put("breedTargetWeightGrams", targetMarketWeightG);
        result.put("totalLiveWeightKg", totalLiveWeightOnFarmKg);
        result.put("mortalityPercentage", batchView.getMortalityPercentage());
        result.put("weightUniformityPct", batch.getLatestUniformityPct());
        result.put("feedCostPerBirdInr", feedCostPerBirdInr);
        result.put("totalFeedCostEstimateInr", totalFeedCostInr);
        result.put("expectedMarketReadyDate", expectedMarketReadyDate);
        result.put("liveSellingPricePerKg", batch.getLiveSellingPricePerKg());
        result.put("estimatedLiveFlockValueInr", estimatedLiveFlockValueInr);
        result.put("totalBirdSalesRevenueInr", batch.getTotalBirdSalesRevenue());
        result.put("weeklyComparison", weeklyComparison);
        result.put("weightHistory", allWeights);
        return result;
    }

    private boolean toDateIsHistorical(LocalDate toDate) {
        return toDate != null && toDate.isBefore(LocalDate.now());
    }

    private double findNearestWeightOnOrBefore(List<WeightRecord> weights, LocalDate date, double fallback) {
        double found = fallback;
        for (WeightRecord w : weights) {
            if (!w.getRecordDate().isAfter(date) && w.getAverageWeightGrams() != null) {
                found = w.getAverageWeightGrams();
            }
        }
        return found;
    }

    // -------------------------------------------------------------------------
    // 3. LAYER & EGG PRODUCTION ENGINE (HEN-DAY % + GRADING + INVENTORY)
    // -------------------------------------------------------------------------
    public List<EggRecord> getEggRecordsForBatch(String batchId) {
        batchService.getBatchDocumentWithAccess(batchId);
        return eggRepository.findByBatchIdOrderByRecordDateDesc(batchId);
    }

    public EggRecord recordDailyEggCollection(EggRecordRequest req) {
        User actor = farmAccessGuard.getCurrentUser();
        Batch batch = batchService.getBatchDocumentWithAccess(req.getBatchId());
        batchService.ensureBatchEditable(batch);

        int damaged = req.getDamagedEggs();
        int broken = req.getBrokenEggs();
        int dirty = req.getDirtyEggs();
        int good = req.getTotalEggs() - damaged - broken - dirty;
        if (good < 0) {
            throw new BadRequestException("Damaged + Broken + Dirty eggs (" + (damaged + broken + dirty)
                    + ") cannot exceed Total Eggs collected (" + req.getTotalEggs() + ").");
        }

        // Determine live hen count on that date (from DailyRecord if logged, otherwise current live birds)
        int liveHens = dailyRepository.findByBatchIdAndRecordDate(batch.getId(), req.getRecordDate())
                .map(DailyRecord::getClosingBirds)
                .orElseGet(() -> BatchResponse.fromDocument(batch).getCurrentLiveBirds());
        if (liveHens <= 0) {
            throw new BadRequestException("Cannot record egg production when live hen count is 0.");
        }

        // Check available egg stock before allowing egg sale
        EggRecord existingOnDate = eggRepository.findByBatchIdAndRecordDate(batch.getId(), req.getRecordDate()).orElse(null);
        int priorGoodToday = existingOnDate != null ? existingOnDate.getGoodEggs() : 0;
        int priorSoldToday = existingOnDate != null ? existingOnDate.getEggsSoldCount() : 0;
        int projectedStockAfter = (batch.getTotalGoodEggsProduced() - priorGoodToday + good)
                - (batch.getTotalEggsSold() - priorSoldToday + req.getEggsSoldCount());
        if (projectedStockAfter < 0) {
            throw new BadRequestException("Eggs sold (" + req.getEggsSoldCount()
                    + ") exceed available good egg inventory!");
        }

        EggRecord er = existingOnDate != null ? existingOnDate : new EggRecord();
        er.setFarmId(batch.getFarmId());
        er.setBatchId(batch.getId());
        er.setBatchCode(batch.getBatchCode());
        er.setRecordDate(req.getRecordDate());
        er.setLiveHenCount(liveHens);
        er.setTotalEggs(req.getTotalEggs());
        er.setGoodEggs(good);
        er.setDamagedEggs(damaged);
        er.setBrokenEggs(broken);
        er.setDirtyEggs(dirty);

        // Auto-distribute ungraded good eggs to Medium if user didn't enter specific grade counts
        int gradedSum = req.getGradeSmall() + req.getGradeMedium() + req.getGradeLarge() + req.getGradeExtraLarge();
        if (gradedSum == 0 && good > 0) {
            er.setGradeSmall(0);
            er.setGradeMedium(good);
            er.setGradeLarge(0);
            er.setGradeExtraLarge(0);
        } else {
            er.setGradeSmall(req.getGradeSmall());
            er.setGradeMedium(req.getGradeMedium());
            er.setGradeLarge(req.getGradeLarge());
            er.setGradeExtraLarge(req.getGradeExtraLarge());
        }

        double henDayPct = Math.round(((req.getTotalEggs() * 100.0) / liveHens) * 100.0) / 100.0;
        er.setHenDayProductionPct(henDayPct);
        er.setFeedConsumedKg(req.getFeedConsumedKg());
        er.setAverageBirdWeightGrams(req.getAverageBirdWeightGrams());
        er.setEggsSoldCount(req.getEggsSoldCount());
        er.setPricePerEgg(req.getPricePerEgg());
        double rev = round2(req.getEggsSoldCount() * req.getPricePerEgg());
        er.setEggSaleRevenue(rev);
        er.setRemarks(req.getRemarks());
        er.setRecordedBy(actor.getUsername());

        EggRecord saved = eggRepository.save(er);
        recalculateBatchEggCounters(batch);

        auditLogService.record(actor, batch.getFarmId(), "EGG_RECORD_SAVED", "EGG_RECORD", saved.getId(),
                "Recorded " + saved.getTotalEggs() + " eggs (Good: " + good + ", HDEP: " + henDayPct + "%) on " + batch.getBatchCode());
        return saved;
    }

    public void recalculateBatchEggCounters(Batch batch) {
        List<EggRecord> allEggs = eggRepository.findByBatchIdOrderByRecordDateAsc(batch.getId());
        int totalGood = allEggs.stream().mapToInt(EggRecord::getGoodEggs).sum();
        int totalSold = allEggs.stream().mapToInt(EggRecord::getEggsSoldCount).sum();
        double totalRev = round2(allEggs.stream().mapToDouble(EggRecord::getEggSaleRevenue).sum());
        batch.setTotalGoodEggsProduced(totalGood);
        batch.setTotalEggsSold(totalSold);
        batch.setTotalEggSalesRevenue(totalRev);
        batchRepository.save(batch);
    }

    public Map<String, Object> calculateLayerAnalytics(String batchId) {
        Batch batch = batchService.getBatchDocumentWithAccess(batchId);
        BatchResponse batchView = BatchResponse.fromDocument(batch);
        Breed breed = batch.getBreedId() != null ? breedRepository.findById(batch.getBreedId()).orElse(null) : null;

        List<EggRecord> records = eggRepository.findByBatchIdOrderByRecordDateAsc(batch.getId());

        int sumTotalEggs = records.stream().mapToInt(EggRecord::getTotalEggs).sum();
        int sumGood = records.stream().mapToInt(EggRecord::getGoodEggs).sum();
        int sumDamaged = records.stream().mapToInt(EggRecord::getDamagedEggs).sum();
        int sumBroken = records.stream().mapToInt(EggRecord::getBrokenEggs).sum();
        int sumDirty = records.stream().mapToInt(EggRecord::getDirtyEggs).sum();
        int sumSmall = records.stream().mapToInt(EggRecord::getGradeSmall).sum();
        int sumMedium = records.stream().mapToInt(EggRecord::getGradeMedium).sum();
        int sumLarge = records.stream().mapToInt(EggRecord::getGradeLarge).sum();
        int sumXL = records.stream().mapToInt(EggRecord::getGradeExtraLarge).sum();
        int sumSold = records.stream().mapToInt(EggRecord::getEggsSoldCount).sum();
        double sumRevenue = round2(records.stream().mapToDouble(EggRecord::getEggSaleRevenue).sum());
        int eggStockGood = Math.max(0, sumGood - sumSold);

        double latestHenDayPct = !records.isEmpty()
                ? records.get(records.size() - 1).getHenDayProductionPct() : 0.0;
        double avgHenDayPct = !records.isEmpty()
                ? round2(records.stream().mapToDouble(EggRecord::getHenDayProductionPct).average().orElse(0.0)) : 0.0;
        double targetPeakPct = (breed != null && breed.getEggProduction() != null && breed.getEggProduction().getPeakHenDayProductionPct() != null)
                ? breed.getEggProduction().getPeakHenDayProductionPct() : 90.0;

        // Monthly aggregation for Monthly Egg Production Chart
        Map<YearMonth, List<EggRecord>> byMonth = records.stream()
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getRecordDate()), LinkedHashMap::new, Collectors.toList()));
        List<Map<String, Object>> monthlyChart = new ArrayList<>();
        byMonth.forEach((ym, list) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", ym.toString());
            m.put("totalEggs", list.stream().mapToInt(EggRecord::getTotalEggs).sum());
            m.put("goodEggs", list.stream().mapToInt(EggRecord::getGoodEggs).sum());
            m.put("avgHenDayPct", round2(list.stream().mapToDouble(EggRecord::getHenDayProductionPct).average().orElse(0.0)));
            m.put("revenueInr", round2(list.stream().mapToDouble(EggRecord::getEggSaleRevenue).sum()));
            monthlyChart.add(m);
        });

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("batch", batchView);
        out.put("liveHenCount", batchView.getCurrentLiveBirds());
        out.put("totalEggsCollected", sumTotalEggs);
        out.put("goodEggs", sumGood);
        out.put("damagedEggs", sumDamaged);
        out.put("brokenEggs", sumBroken);
        out.put("dirtyEggs", sumDirty);
        out.put("gradeSmall", sumSmall);
        out.put("gradeMedium", sumMedium);
        out.put("gradeLarge", sumLarge);
        out.put("gradeExtraLarge", sumXL);
        out.put("eggInventoryPieces", eggStockGood);
        out.put("eggInventoryTrays30", round2(eggStockGood / 30.0));
        out.put("eggsSoldPieces", sumSold);
        out.put("totalEggRevenueInr", sumRevenue);
        out.put("latestHenDayProductionPct", latestHenDayPct);
        out.put("averageHenDayProductionPct", avgHenDayPct);
        out.put("breedTargetPeakHenDayPct", targetPeakPct);
        out.put("dailyRecords", records);
        out.put("monthlySummary", monthlyChart);
        return out;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
