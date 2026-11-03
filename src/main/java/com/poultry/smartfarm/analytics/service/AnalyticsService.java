package com.poultry.smartfarm.analytics.service;

import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.document.EggRecord;
import com.poultry.smartfarm.batch.document.WeightRecord;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.repository.EggRecordRepository;
import com.poultry.smartfarm.batch.repository.WeightRecordRepository;
import com.poultry.smartfarm.batch.service.PoultryAnalyticsCalculationService;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.commerce.document.ChickenShop;
import com.poultry.smartfarm.commerce.document.ProcessingRecord;
import com.poultry.smartfarm.commerce.document.RetailerStore;
import com.poultry.smartfarm.commerce.document.Sale;
import com.poultry.smartfarm.commerce.repository.ChickenShopRepository;
import com.poultry.smartfarm.commerce.repository.ProcessingRecordRepository;
import com.poultry.smartfarm.commerce.repository.RetailerStoreRepository;
import com.poultry.smartfarm.commerce.repository.SaleRepository;
import com.poultry.smartfarm.commerce.service.LedgerAndFinancialService;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.inventory.document.InventoryItem;
import com.poultry.smartfarm.inventory.repository.InventoryItemRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final FarmRepository farmRepository;
    private final BatchRepository batchRepository;
    private final BreedRepository breedRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final WeightRecordRepository weightRecordRepository;
    private final EggRecordRepository eggRecordRepository;
    private final InventoryItemRepository inventoryRepository;
    private final SaleRepository saleRepository;
    private final ProcessingRecordRepository processingRepository;
    private final ChickenShopRepository chickenShopRepository;
    private final RetailerStoreRepository retailerStoreRepository;
    private final PoultryAnalyticsCalculationService flockCalcService;
    private final LedgerAndFinancialService financialService;
    private final FarmAccessGuard farmAccessGuard;

    public AnalyticsService(FarmRepository farmRepository,
                            BatchRepository batchRepository,
                            BreedRepository breedRepository,
                            DailyRecordRepository dailyRecordRepository,
                            WeightRecordRepository weightRecordRepository,
                            EggRecordRepository eggRecordRepository,
                            InventoryItemRepository inventoryRepository,
                            SaleRepository saleRepository,
                            ProcessingRecordRepository processingRepository,
                            ChickenShopRepository chickenShopRepository,
                            RetailerStoreRepository retailerStoreRepository,
                            PoultryAnalyticsCalculationService flockCalcService,
                            LedgerAndFinancialService financialService,
                            FarmAccessGuard farmAccessGuard) {
        this.farmRepository = farmRepository;
        this.batchRepository = batchRepository;
        this.breedRepository = breedRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.weightRecordRepository = weightRecordRepository;
        this.eggRecordRepository = eggRecordRepository;
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
        this.processingRepository = processingRepository;
        this.chickenShopRepository = chickenShopRepository;
        this.retailerStoreRepository = retailerStoreRepository;
        this.flockCalcService = flockCalcService;
        this.financialService = financialService;
        this.farmAccessGuard = farmAccessGuard;
    }

    public Map<String, Object> buildUnifiedAnalyticsCharts(
            String farmId,
            String batchId,
            String breedId,
            LocalDate fromDate,
            LocalDate toDate,
            String batchCategory,
            String businessType) {

        User user = farmAccessGuard.getCurrentUser();
        LocalDate start = fromDate != null ? fromDate : LocalDate.now().minusMonths(12);
        LocalDate end = toDate != null ? toDate : LocalDate.now().plusDays(1);

        // Determine accessible farms (RBAC + strict Farm Isolation)
        List<Farm> accessibleFarms = new ArrayList<>();
        if (user.getRole() == Role.SUPER_ADMIN) {
            accessibleFarms = farmRepository.findAll();
        } else if (user.getRole().isFarmRole() && user.getAssignedFarmIds() != null && !user.getAssignedFarmIds().isEmpty()) {
            Set<String> assignedSet = Set.copyOf(user.getAssignedFarmIds());
            accessibleFarms = farmRepository.findAll().stream()
                    .filter(f -> assignedSet.contains(f.getId()))
                    .toList();
        }

        if (farmId != null && !farmId.isBlank() && user.getRole().isFarmRole()) {
            farmAccessGuard.requireFarmAccess(farmId);
        }

        List<String> targetFarmIds = (farmId != null && !farmId.isBlank())
                ? List.of(farmId)
                : accessibleFarms.stream().map(Farm::getId).toList();

        // Accessible batches filtered by farmId, batchId, breedId, batchCategory
        List<Batch> allAccessibleBatches = targetFarmIds.isEmpty()
                ? Collections.emptyList()
                : batchRepository.findByFarmIdInOrderByChickArrivalDateDesc(targetFarmIds);

        List<Batch> filteredBatches = allAccessibleBatches.stream()
                .filter(b -> batchId == null || batchId.isBlank() || batchId.equals(b.getId()))
                .filter(b -> breedId == null || breedId.isBlank() || breedId.equals(b.getBreedId()))
                .filter(b -> batchCategory == null || batchCategory.isBlank() || batchCategory.equalsIgnoreCase(b.getBatchCategory()))
                .toList();

        Set<String> batchIds = filteredBatches.stream().map(Batch::getId).collect(Collectors.toSet());
        List<Breed> allBreeds = breedRepository.findAll();

        // Daily records in date window for selected batches
        List<DailyRecord> dailyRecords = targetFarmIds.isEmpty()
                ? Collections.emptyList()
                : dailyRecordRepository.findByFarmIdInOrderByRecordDateDesc(targetFarmIds).stream()
                .filter(d -> batchIds.contains(d.getBatchId()))
                .filter(d -> d.getRecordDate() != null && !d.getRecordDate().isBefore(start) && !d.getRecordDate().isAfter(end))
                .sorted(Comparator.comparing(DailyRecord::getRecordDate))
                .toList();

        // =====================================================================
        // CHART 1: Total Birds Chart (Live vs Sold vs Mortality & Culls by Batch)
        // =====================================================================
        List<String> c1Labels = new ArrayList<>();
        List<Integer> c1Live = new ArrayList<>();
        List<Integer> c1Sold = new ArrayList<>();
        List<Integer> c1Mortality = new ArrayList<>();
        int grandLiveBirds = 0;
        int grandMortality = 0;
        for (Batch b : filteredBatches) {
            BatchResponse bv = BatchResponse.fromDocument(b);
            c1Labels.add(b.getBatchCode());
            c1Live.add(bv.getCurrentLiveBirds());
            c1Sold.add(b.getTotalBirdsSold());
            c1Mortality.add(b.getTotalMortality() + b.getTotalCulls());
            grandLiveBirds += bv.getCurrentLiveBirds();
            grandMortality += b.getTotalMortality();
        }
        Map<String, Object> totalBirdsChart = Map.of(
                "labels", c1Labels,
                "liveBirds", c1Live,
                "soldBirds", c1Sold,
                "mortalityAndCulls", c1Mortality
        );

        // =====================================================================
        // CHART 2: Daily Mortality Chart (Date-wise Mortality & Culls)
        // =====================================================================
        Map<String, Integer> mortByDate = new LinkedHashMap<>();
        Map<String, Integer> cullsByDate = new LinkedHashMap<>();
        for (DailyRecord dr : dailyRecords) {
            String dt = dr.getRecordDate().toString();
            mortByDate.put(dt, mortByDate.getOrDefault(dt, 0) + dr.getMortality());
            cullsByDate.put(dt, cullsByDate.getOrDefault(dt, 0) + dr.getCulls());
        }
        Map<String, Object> dailyMortalityChart = Map.of(
                "labels", new ArrayList<>(mortByDate.keySet()),
                "mortality", new ArrayList<>(mortByDate.values()),
                "culls", new ArrayList<>(cullsByDate.values())
        );

        // =====================================================================
        // CHART 3 & CHART 4: Daily Bird Growth (ADG g/day) & Average Bird Weight Curve
        // =====================================================================
        List<WeightRecord> weightRecords = new ArrayList<>();
        for (Batch b : filteredBatches) {
            weightRecords.addAll(weightRecordRepository.findByBatchIdOrderByRecordDateAsc(b.getId()));
        }
        weightRecords = weightRecords.stream()
                .filter(w -> w.getRecordDate() != null && !w.getRecordDate().isBefore(start) && !w.getRecordDate().isAfter(end))
                .sorted(Comparator.comparing(WeightRecord::getRecordDate))
                .toList();

        List<String> wtLabels = new ArrayList<>();
        List<Double> avgWeights = new ArrayList<>();
        List<Double> expectedWeights = new ArrayList<>();
        List<Double> dailyGrowthGains = new ArrayList<>();

        Map<String, Double> prevWtByBatch = new LinkedHashMap<>();
        Map<String, Integer> prevAgeByBatch = new LinkedHashMap<>();
        for (Batch b : filteredBatches) {
            prevWtByBatch.put(b.getId(), b.getChickInitialWeightGrams());
            prevAgeByBatch.put(b.getId(), 0);
        }

        if (!weightRecords.isEmpty()) {
            for (WeightRecord wr : weightRecords) {
                wtLabels.add(wr.getBatchCode() + " (" + wr.getRecordDate() + ")");
                double curWt = wr.getAverageWeightGrams() != null ? wr.getAverageWeightGrams() : 0.0;
                double expWt = wr.getExpectedBreedWeightGrams() != null ? wr.getExpectedBreedWeightGrams() : curWt;
                avgWeights.add(round2(curWt));
                expectedWeights.add(round2(expWt));

                double prevW = prevWtByBatch.getOrDefault(wr.getBatchId(), 40.0);
                int prevAge = prevAgeByBatch.getOrDefault(wr.getBatchId(), 0);
                int curAge = wr.getAgeDays() != null ? wr.getAgeDays() : prevAge + 7;
                int deltaDays = Math.max(1, curAge - prevAge);
                double adg = Math.max(0.0, round2((curWt - prevW) / deltaDays));
                dailyGrowthGains.add(adg);

                prevWtByBatch.put(wr.getBatchId(), curWt);
                prevAgeByBatch.put(wr.getBatchId(), curAge);
            }
        } else {
            // Fallback to DailyRecord averageBirdWeightGrams if no separate WeightRecord sampled yet
            for (DailyRecord dr : dailyRecords) {
                if (dr.getAverageBirdWeightGrams() != null && dr.getAverageBirdWeightGrams() > 0) {
                    wtLabels.add(dr.getBatchCode() + " (" + dr.getRecordDate() + ")");
                    double curWt = dr.getAverageBirdWeightGrams();
                    avgWeights.add(round2(curWt));
                    expectedWeights.add(round2(curWt));
                    double prevW = prevWtByBatch.getOrDefault(dr.getBatchId(), 40.0);
                    dailyGrowthGains.add(round2(Math.max(0.0, curWt - prevW)));
                    prevWtByBatch.put(dr.getBatchId(), curWt);
                }
            }
        }

        Map<String, Object> dailyGrowthChart = Map.of(
                "labels", wtLabels,
                "dailyGainGrams", dailyGrowthGains
        );
        Map<String, Object> averageWeightChart = Map.of(
                "labels", wtLabels,
                "actualWeightGrams", avgWeights,
                "expectedBreedWeightGrams", expectedWeights
        );

        // =====================================================================
        // CHART 5: Feed Consumption Chart (Date-wise kg + Phase Breakdown)
        // =====================================================================
        Map<String, Double> feedByDate = new LinkedHashMap<>();
        Map<String, Double> feedByType = new LinkedHashMap<>();
        for (DailyRecord dr : dailyRecords) {
            String dt = dr.getRecordDate().toString();
            feedByDate.put(dt, round2(feedByDate.getOrDefault(dt, 0.0) + dr.getFeedConsumptionKg()));
            String ft = dr.getFeedType() != null ? dr.getFeedType().toUpperCase() : "STARTER";
            feedByType.put(ft, round2(feedByType.getOrDefault(ft, 0.0) + dr.getFeedConsumptionKg()));
        }
        Map<String, Object> feedConsumptionChart = Map.of(
                "labels", new ArrayList<>(feedByDate.keySet()),
                "dailyFeedKg", new ArrayList<>(feedByDate.values()),
                "phaseLabels", new ArrayList<>(feedByType.keySet()),
                "phaseKg", new ArrayList<>(feedByType.values())
        );

        // =====================================================================
        // CHART 6: FCR Chart (Actual FCR vs Breed Target FCR across Batches)
        // =====================================================================
        List<String> fcrLabels = new ArrayList<>();
        List<Double> actualFcrs = new ArrayList<>();
        List<Double> targetFcrs = new ArrayList<>();
        for (Batch b : filteredBatches) {
            if (!"LAYER".equalsIgnoreCase(b.getBatchCategory())) {
                try {
                    Map<String, Object> bCalc = flockCalcService.calculateBroilerAnalytics(b.getId(), fromDate, toDate);
                    fcrLabels.add(b.getBatchCode() + " (" + b.getBreedName() + ")");
                    actualFcrs.add(((Number) bCalc.getOrDefault("fcr", 0.0)).doubleValue());
                    targetFcrs.add(((Number) bCalc.getOrDefault("breedTargetFcr", 1.65)).doubleValue());
                } catch (Exception ignored) {
                }
            }
        }
        Map<String, Object> fcrChart = Map.of(
                "labels", fcrLabels,
                "actualFcr", actualFcrs,
                "targetFcr", targetFcrs
        );

        // =====================================================================
        // CHART 7: Water Consumption Chart (Daily Liters)
        // =====================================================================
        Map<String, Double> waterByDate = new LinkedHashMap<>();
        for (DailyRecord dr : dailyRecords) {
            String dt = dr.getRecordDate().toString();
            waterByDate.put(dt, round2(waterByDate.getOrDefault(dt, 0.0) + dr.getWaterConsumptionLiters()));
        }
        Map<String, Object> waterConsumptionChart = Map.of(
                "labels", new ArrayList<>(waterByDate.keySet()),
                "liters", new ArrayList<>(waterByDate.values())
        );

        // =====================================================================
        // CHART 8: Egg Production Chart (Good vs Damaged/Broken + Hen-Day %)
        // =====================================================================
        List<EggRecord> eggRecords = targetFarmIds.isEmpty()
                ? Collections.emptyList()
                : eggRecordRepository.findByFarmIdInOrderByRecordDateDesc(targetFarmIds).stream()
                .filter(er -> batchIds.contains(er.getBatchId()))
                .filter(er -> er.getRecordDate() != null && !er.getRecordDate().isBefore(start) && !er.getRecordDate().isAfter(end))
                .sorted(Comparator.comparing(EggRecord::getRecordDate))
                .toList();

        List<String> eggLabels = new ArrayList<>();
        List<Integer> goodEggsList = new ArrayList<>();
        List<Integer> rejectedEggsList = new ArrayList<>();
        List<Double> henDayPctList = new ArrayList<>();
        int grandGoodEggs = 0;
        for (EggRecord er : eggRecords) {
            eggLabels.add(er.getBatchCode() + " (" + er.getRecordDate() + ")");
            goodEggsList.add(er.getGoodEggs());
            rejectedEggsList.add(er.getDamagedEggs() + er.getBrokenEggs() + er.getDirtyEggs());
            henDayPctList.add(er.getHenDayProductionPct());
            grandGoodEggs += er.getGoodEggs();
        }
        Map<String, Object> eggProductionChart = Map.of(
                "labels", eggLabels,
                "goodEggs", goodEggsList,
                "damagedBrokenDirtyEggs", rejectedEggsList,
                "henDayPct", henDayPctList
        );

        // =====================================================================
        // CHART 9 & CHART 10: Chicken Sales Chart & Dressed Meat Sales Chart
        // =====================================================================
        List<Sale> sales = new ArrayList<>();
        if (user.getRole() == Role.RETAILER || user.getRole() == Role.CHICKEN_SHOP_OWNER) {
            sales = saleRepository.findByOwnerUserIdOrderBySaleDateDescCreatedAtDesc(user.getId());
        } else if (user.getRole() == Role.SUPER_ADMIN && (farmId == null || farmId.isBlank())) {
            sales = saleRepository.findAll();
        } else if (!targetFarmIds.isEmpty()) {
            sales = saleRepository.findByFarmIdInOrderBySaleDateDescCreatedAtDesc(targetFarmIds);
        }

        sales = sales.stream()
                .filter(s -> s.getSaleDate() != null && !s.getSaleDate().isBefore(start) && !s.getSaleDate().isAfter(end))
                .filter(s -> batchId == null || batchId.isBlank() || batchId.equals(s.getBatchId()))
                .filter(s -> businessType == null || businessType.isBlank() || "ALL".equalsIgnoreCase(businessType)
                        || businessType.equalsIgnoreCase(s.getBusinessContext()))
                .sorted(Comparator.comparing(Sale::getSaleDate))
                .toList();

        Map<String, Double> chickenSalesByDate = new LinkedHashMap<>();
        Map<String, Double> eggSalesByDate = new LinkedHashMap<>();
        Map<String, Double> meatSalesByDate = new LinkedHashMap<>();
        for (Sale s : sales) {
            String dt = s.getSaleDate().toString();
            String sc = s.getSaleCategory() != null ? s.getSaleCategory().toUpperCase() : "";
            if ("LIVE_CHICKEN_SALE".equals(sc)) {
                chickenSalesByDate.put(dt, round2(chickenSalesByDate.getOrDefault(dt, 0.0) + s.getTotalSaleAmountInr()));
            } else if ("EGG_SALE".equals(sc)) {
                eggSalesByDate.put(dt, round2(eggSalesByDate.getOrDefault(dt, 0.0) + s.getTotalSaleAmountInr()));
            } else if ("DRESSED_MEAT_SALE".equals(sc)) {
                meatSalesByDate.put(dt, round2(meatSalesByDate.getOrDefault(dt, 0.0) + s.getTotalSaleAmountInr()));
            }
        }

        // Also enrich Meat Sales Chart with actual ProcessingRecords for Chicken Shop Owners & Super Admin
        List<ProcessingRecord> procRecords = Collections.emptyList();
        if (user.getRole() == Role.CHICKEN_SHOP_OWNER) {
            procRecords = processingRepository.findByOwnerUserIdOrderByRecordDateDescCreatedAtDesc(user.getId());
        } else if (user.getRole() == Role.SUPER_ADMIN && (businessType == null || "ALL".equalsIgnoreCase(businessType) || "CHICKEN_SHOP".equalsIgnoreCase(businessType))) {
            procRecords = processingRepository.findAll();
        }
        procRecords = procRecords.stream()
                .filter(p -> p.getRecordDate() != null && !p.getRecordDate().isBefore(start) && !p.getRecordDate().isAfter(end))
                .sorted(Comparator.comparing(ProcessingRecord::getRecordDate))
                .toList();

        List<String> meatLabels = new ArrayList<>();
        List<Double> meatKgProduced = new ArrayList<>();
        List<Double> meatRevenueInr = new ArrayList<>();
        if (!procRecords.isEmpty()) {
            for (ProcessingRecord pr : procRecords) {
                meatLabels.add((pr.getShopName() != null ? pr.getShopName() : "Shop") + " (" + pr.getRecordDate() + ")");
                meatKgProduced.add(round2(pr.getActualDressedMeatWeightKg()));
                meatRevenueInr.add(round2(pr.getTotalRevenueInr()));
            }
        } else {
            for (Map.Entry<String, Double> entry : meatSalesByDate.entrySet()) {
                meatLabels.add(entry.getKey());
                meatKgProduced.add(0.0);
                meatRevenueInr.add(entry.getValue());
            }
        }

        Map<String, Object> chickenSalesChart = Map.of(
                "labels", new ArrayList<>(chickenSalesByDate.keySet()),
                "chickenSalesInr", new ArrayList<>(chickenSalesByDate.values()),
                "eggSalesLabels", new ArrayList<>(eggSalesByDate.keySet()),
                "eggSalesInr", new ArrayList<>(eggSalesByDate.values())
        );
        Map<String, Object> meatSalesChart = Map.of(
                "labels", meatLabels,
                "actualDressedMeatKg", meatKgProduced,
                "meatRevenueInr", meatRevenueInr
        );

        // =====================================================================
        // CHART 11 & CHART 12: Income vs Expenses Chart & Profit / Loss Chart
        // =====================================================================
        Map<String, Object> finReport = financialService.calculateFinancialReport(farmId, batchId, fromDate, toDate);
        double totalRevenueInr = toDouble(finReport.get("totalRevenueInr"),
                toDouble(finReport.get("totalSalesRevenueInr"), 0.0));
        double totalExpensesInr = toDouble(finReport.get("totalExpensesInr"),
                toDouble(finReport.get("totalOperatingAndPurchaseCostsInr"), 0.0));
        double netProfitLossInr = toDouble(finReport.get("netProfitLossInr"), round2(totalRevenueInr - totalExpensesInr));

        List<String> incExpLabels = List.of(
                "Live Chicken Sales", "Egg Sales", "Dressed Meat Sales", "Manure/By-Products",
                "Chick Purchase", "Feed Consumed", "Medicine & Vaccine", "Labour & Utilities"
        );
        List<Double> incExpValues = List.of(
                toDouble(finReport.get("chickenSalesInr"), 0.0),
                toDouble(finReport.get("eggSalesInr"), 0.0),
                toDouble(finReport.get("meatSalesInr"), 0.0),
                toDouble(finReport.get("manureAndByProductSalesInr"), 0.0),
                toDouble(finReport.get("chickPurchaseExpensesInr"), 0.0),
                toDouble(finReport.get("feedExpensesInr"), 0.0),
                toDouble(finReport.get("medicineExpensesInr"), 0.0),
                round2(toDouble(finReport.get("labourExpensesInr"), 0.0)
                        + toDouble(finReport.get("electricityExpensesInr"), 0.0)
                        + toDouble(finReport.get("transportationExpensesInr"), 0.0)
                        + toDouble(finReport.get("otherExpensesInr"), 0.0))
        );
        Map<String, Object> incomeVsExpensesChart = Map.of(
                "labels", incExpLabels,
                "valuesInr", incExpValues,
                "totalRevenueInr", round2(totalRevenueInr),
                "totalExpensesInr", round2(totalExpensesInr)
        );

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> monthlyBreakdown = (List<Map<String, Object>>) finReport.getOrDefault("monthlyBreakdown", Collections.emptyList());
        List<String> plMonths = new ArrayList<>();
        List<Double> plMonthlyRev = new ArrayList<>();
        List<Double> plMonthlyExp = new ArrayList<>();
        List<Double> plMonthlyNet = new ArrayList<>();
        if (!monthlyBreakdown.isEmpty()) {
            for (Map<String, Object> row : monthlyBreakdown) {
                plMonths.add(String.valueOf(row.get("month")));
                plMonthlyRev.add(toDouble(row.get("revenueInr"), 0.0));
                plMonthlyExp.add(toDouble(row.get("expensesInr"), 0.0));
                plMonthlyNet.add(toDouble(row.get("profitLossInr"), 0.0));
            }
        } else {
            plMonths.add("Selected Period");
            plMonthlyRev.add(round2(totalRevenueInr));
            plMonthlyExp.add(round2(totalExpensesInr));
            plMonthlyNet.add(round2(netProfitLossInr));
        }
        Map<String, Object> profitLossChart = Map.of(
                "labels", plMonths,
                "revenueInr", plMonthlyRev,
                "expensesInr", plMonthlyExp,
                "netProfitLossInr", plMonthlyNet
        );

        // =====================================================================
        // CHART 13: Inventory Chart (Stock Valuation & Quantity by Category)
        // =====================================================================
        List<InventoryItem> invItems = targetFarmIds.isEmpty()
                ? Collections.emptyList()
                : inventoryRepository.findByFarmIdInOrderByCategoryAscItemNameAsc(targetFarmIds);
        Map<String, Double> invValuationByCat = new LinkedHashMap<>();
        Map<String, Double> invQtyByCat = new LinkedHashMap<>();
        for (InventoryItem item : invItems) {
            String cat = item.getCategory() != null ? item.getCategory().toUpperCase() : "OTHER";
            invValuationByCat.put(cat, round2(invValuationByCat.getOrDefault(cat, 0.0) + item.getTotalValuationInr()));
            invQtyByCat.put(cat, round2(invQtyByCat.getOrDefault(cat, 0.0) + item.getCurrentStock()));
        }
        Map<String, Object> inventoryChart = Map.of(
                "labels", new ArrayList<>(invValuationByCat.keySet()),
                "valuationInr", new ArrayList<>(invValuationByCat.values()),
                "stockUnits", new ArrayList<>(invQtyByCat.values())
        );

        // =====================================================================
        // CHART 14: Farm-Wise Performance Chart (Live Birds, Sales Revenue & Net Profit per Farm)
        // =====================================================================
        List<String> farmLabels = new ArrayList<>();
        List<Integer> farmLiveBirds = new ArrayList<>();
        List<Double> farmRevenues = new ArrayList<>();
        List<Double> farmProfits = new ArrayList<>();

        for (Farm f : accessibleFarms) {
            if (farmId != null && !farmId.isBlank() && !farmId.equals(f.getId())) continue;
            farmLabels.add(f.getFarmName());
            int fLive = allAccessibleBatches.stream()
                    .filter(b -> f.getId().equals(b.getFarmId()))
                    .mapToInt(b -> BatchResponse.fromDocument(b).getCurrentLiveBirds())
                    .sum();
            farmLiveBirds.add(fLive);

            if (user.getRole().isFarmRole()) {
                try {
                    Map<String, Object> fFin = financialService.calculateFinancialReport(f.getId(), null, fromDate, toDate);
                    farmRevenues.add(toDouble(fFin.get("totalRevenueInr"), 0.0));
                    farmProfits.add(toDouble(fFin.get("netProfitLossInr"), 0.0));
                } catch (Exception ignored) {
                    farmRevenues.add(0.0);
                    farmProfits.add(0.0);
                }
            } else {
                farmRevenues.add(0.0);
                farmProfits.add(0.0);
            }
        }
        // Also append Retail & Chicken Shop bars for Super Admin / Retail / Shop roles
        if (user.getRole() == Role.RETAILER || user.getRole() == Role.SUPER_ADMIN) {
            for (RetailerStore rs : retailerStoreRepository.findAll()) {
                if (user.getRole() == Role.RETAILER && !user.getId().equals(rs.getOwnerUserId())) continue;
                farmLabels.add("Retail: " + rs.getStoreName());
                farmLiveBirds.add(rs.getLiveBirdStockCount());
                double rev = round2(rs.getTotalChickenSalesRevenueInr() + rs.getTotalEggSalesRevenueInr());
                double prof = round2(rev - rs.getTotalStockPurchaseCostInr() - rs.getTotalOperatingExpensesInr());
                farmRevenues.add(rev);
                farmProfits.add(prof);
            }
        }
        if (user.getRole() == Role.CHICKEN_SHOP_OWNER || user.getRole() == Role.SUPER_ADMIN) {
            for (ChickenShop cs : chickenShopRepository.findAll()) {
                if (user.getRole() == Role.CHICKEN_SHOP_OWNER && !user.getId().equals(cs.getOwnerUserId())) continue;
                farmLabels.add("Shop: " + cs.getShopName());
                farmLiveBirds.add(cs.getLiveBirdsStockCount());
                double rev = round2(cs.getTotalMeatSalesRevenueInr() + cs.getTotalByProductSalesRevenueInr());
                double prof = round2(rev - cs.getTotalLivePurchaseCostInr() - cs.getTotalProcessingExpensesInr() - cs.getTotalOtherAllocatedExpensesInr());
                farmRevenues.add(rev);
                farmProfits.add(prof);
            }
        }
        Map<String, Object> farmWisePerformanceChart = Map.of(
                "labels", farmLabels,
                "liveBirds", farmLiveBirds,
                "revenueInr", farmRevenues,
                "netProfitInr", farmProfits
        );

        // =====================================================================
        // CHART 15: Batch Comparison Chart (Mortality %, Avg Weight kg, Direct Revenue ₹k)
        // =====================================================================
        List<String> bcLabels = new ArrayList<>();
        List<Double> bcMortalityPct = new ArrayList<>();
        List<Double> bcAvgWeightKg = new ArrayList<>();
        List<Double> bcRevenueThousands = new ArrayList<>();
        for (Batch b : filteredBatches) {
            BatchResponse bv = BatchResponse.fromDocument(b);
            bcLabels.add(b.getBatchCode());
            bcMortalityPct.add(bv.getMortalityPercentage());
            bcAvgWeightKg.add(round2(b.getLatestAverageWeightGrams() / 1000.0));
            bcRevenueThousands.add(round2((b.getTotalBirdSalesRevenue() + b.getTotalEggSalesRevenue()) / 1000.0));
        }
        Map<String, Object> batchComparisonChart = Map.of(
                "labels", bcLabels,
                "mortalityPct", bcMortalityPct,
                "averageWeightKg", bcAvgWeightKg,
                "revenueThousandsInr", bcRevenueThousands
        );

        // Assemble response with all 15 charts + KPIs + filter options
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kpis", Map.of(
                "totalBatchesAnalyzed", filteredBatches.size(),
                "totalLiveBirds", grandLiveBirds,
                "totalMortalityBirds", grandMortality,
                "totalGoodEggsCollected", grandGoodEggs,
                "totalRevenueInr", round2(totalRevenueInr),
                "totalExpensesInr", round2(totalExpensesInr),
                "netProfitLossInr", round2(netProfitLossInr)
        ));
        out.put("filtersMeta", Map.of(
                "farms", accessibleFarms.stream().map(f -> Map.of("id", f.getId(), "name", f.getFarmName())).toList(),
                "batches", allAccessibleBatches.stream().map(b -> Map.of(
                        "id", b.getId(),
                        "code", b.getBatchCode(),
                        "name", b.getBatchName(),
                        "category", b.getBatchCategory() != null ? b.getBatchCategory() : "BROILER"
                )).toList(),
                "breeds", allBreeds.stream().map(br -> Map.of("id", br.getId(), "name", br.getBreedName())).toList()
        ));

        Map<String, Object> charts = new LinkedHashMap<>();
        charts.put("totalBirdsChart", totalBirdsChart);
        charts.put("dailyMortalityChart", dailyMortalityChart);
        charts.put("dailyGrowthChart", dailyGrowthChart);
        charts.put("averageWeightChart", averageWeightChart);
        charts.put("feedConsumptionChart", feedConsumptionChart);
        charts.put("fcrChart", fcrChart);
        charts.put("waterConsumptionChart", waterConsumptionChart);
        charts.put("eggProductionChart", eggProductionChart);
        charts.put("chickenSalesChart", chickenSalesChart);
        charts.put("meatSalesChart", meatSalesChart);
        charts.put("incomeVsExpensesChart", incomeVsExpensesChart);
        charts.put("profitLossChart", profitLossChart);
        charts.put("inventoryChart", inventoryChart);
        charts.put("farmWisePerformanceChart", farmWisePerformanceChart);
        charts.put("batchComparisonChart", batchComparisonChart);
        out.put("charts", charts);

        return out;
    }

    private double toDouble(Object val, double fallback) {
        if (val instanceof Number n) {
            return round2(n.doubleValue());
        }
        return fallback;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
