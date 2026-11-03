package com.poultry.smartfarm.init;

import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.document.EggRecord;
import com.poultry.smartfarm.batch.document.MortalityRecord;
import com.poultry.smartfarm.batch.document.WeightRecord;
import com.poultry.smartfarm.batch.repository.BatchRepository;
import com.poultry.smartfarm.batch.repository.DailyRecordRepository;
import com.poultry.smartfarm.batch.repository.EggRecordRepository;
import com.poultry.smartfarm.batch.repository.MortalityRecordRepository;
import com.poultry.smartfarm.batch.repository.WeightRecordRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.breed.document.Breed;
import com.poultry.smartfarm.breed.repository.BreedRepository;
import com.poultry.smartfarm.farm.document.Farm;
import com.poultry.smartfarm.farm.document.PoultryHouse;
import com.poultry.smartfarm.farm.repository.FarmRepository;
import com.poultry.smartfarm.farm.repository.PoultryHouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Order(2)
public class Phase2DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Phase2DataInitializer.class);

    private final BatchRepository batchRepository;
    private final DailyRecordRepository dailyRepository;
    private final WeightRecordRepository weightRepository;
    private final EggRecordRepository eggRepository;
    private final MortalityRecordRepository mortalityRepository;
    private final FarmRepository farmRepository;
    private final PoultryHouseRepository houseRepository;
    private final BreedRepository breedRepository;
    private final BatchService batchService;

    @Value("${app.security.seed-demo-users:true}")
    private boolean seedDemoData;

    public Phase2DataInitializer(BatchRepository batchRepository,
                                 DailyRecordRepository dailyRepository,
                                 WeightRecordRepository weightRepository,
                                 EggRecordRepository eggRepository,
                                 MortalityRecordRepository mortalityRepository,
                                 FarmRepository farmRepository,
                                 PoultryHouseRepository houseRepository,
                                 BreedRepository breedRepository,
                                 BatchService batchService) {
        this.batchRepository = batchRepository;
        this.dailyRepository = dailyRepository;
        this.weightRepository = weightRepository;
        this.eggRepository = eggRepository;
        this.mortalityRepository = mortalityRepository;
        this.farmRepository = farmRepository;
        this.houseRepository = houseRepository;
        this.breedRepository = breedRepository;
        this.batchService = batchService;
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData || batchRepository.count() > 0) {
            return;
        }

        Farm farm1 = farmRepository.findByFarmCodeIgnoreCase("FARM-MH-001").orElse(null);
        Farm farm2 = farmRepository.findByFarmCodeIgnoreCase("FARM-MP-002").orElse(null);
        if (farm1 == null) return;

        List<PoultryHouse> farm1Houses = houseRepository.findByFarmId(farm1.getId());
        PoultryHouse shed1 = farm1Houses.stream().filter(h -> "SHED-01".equalsIgnoreCase(h.getHouseCode())).findFirst().orElse(null);
        PoultryHouse shed2 = farm1Houses.stream().filter(h -> "SHED-02".equalsIgnoreCase(h.getHouseCode())).findFirst().orElse(null);

        Breed ross308 = breedRepository.findByBreedCodeIgnoreCase("COM-ROSS308").orElse(null);
        Breed bv300 = breedRepository.findByBreedCodeIgnoreCase("COM-BV300").orElse(null);
        Breed kadaknath = breedRepository.findByBreedCodeIgnoreCase("IND-KADAKNATH").orElse(null);

        LocalDate today = LocalDate.now();

        // 1. Seed 35-Day Commercial Broiler Batch (Ross 308)
        if (shed1 != null && ross308 != null) {
            LocalDate arrival = today.minusDays(34);
            Batch b = new Batch();
            b.setBatchCode("BATCH-MH-2026-B01");
            b.setFarmId(farm1.getId());
            b.setFarmName(farm1.getFarmName());
            b.setPoultryHouseId(shed1.getId());
            b.setPoultryHouseName(shed1.getHouseName());
            b.setBatchName("Winter Commercial Broiler Lot 1");
            b.setBreedId(ross308.getId());
            b.setBreedCode(ross308.getBreedCode());
            b.setBreedName(ross308.getBreedName());
            b.setBatchCategory("BROILER");
            b.setPurchaseDate(arrival.minusDays(1));
            b.setChickArrivalDate(arrival);
            b.setStartingBirdQuantity(4500);
            b.setPurchasePricePerChick(38.0);
            b.setSupplierName("Venky's Hatcheries Nashik");
            b.setInitialBirdAgeDays(0);
            b.setPlannedDurationDays(40);
            b.setChickInitialWeightGrams(42.0);
            b.setEstimatedFeedRatePerKg(42.0);
            b.setLiveSellingPricePerKg(126.0);
            b.setTotalBirdsSold(300);
            b.setTotalSoldLiveWeightKg(615.0);
            b.setTotalBirdSalesRevenue(77490.0);
            b.setStatus("ACTIVE");
            b = batchRepository.save(b);

            int opening = 4500;
            int sumMort = 0;
            int sumCulls = 0;
            double latestWt = 42.0;

            for (int day = 1; day <= 35; day++) {
                LocalDate date = arrival.plusDays(day - 1);
                int mort = (day <= 7) ? 4 : (day % 3 == 0 ? 3 : 2);
                int cull = (day % 12 == 0) ? 1 : 0;
                int closing = opening - mort - cull;

                String feedType = (day <= 14) ? "STARTER" : (day <= 28 ? "GROWER" : "FINISHER");
                double gramsPerBird = Math.min(165.0, 14.0 + (day * 4.1));
                double feedKg = Math.round((opening * gramsPerBird / 1000.0) * 10.0) / 10.0;
                double waterL = Math.round((feedKg * 1.85) * 10.0) / 10.0;

                Double sampleWt = null;
                if (day % 7 == 0) {
                    latestWt = Math.round((42.0 + Math.pow(day / 40.0, 1.18) * 2358.0) * 10.0) / 10.0;
                    sampleWt = latestWt;

                    WeightRecord wr = new WeightRecord();
                    wr.setFarmId(farm1.getId());
                    wr.setBatchId(b.getId());
                    wr.setBatchCode(b.getBatchCode());
                    wr.setRecordDate(date);
                    wr.setAgeDays(day);
                    wr.setWeekNumber(day / 7);
                    wr.setSampleBirdCount(60);
                    wr.setAverageWeightGrams(sampleWt);
                    wr.setMinWeightGrams(Math.round(sampleWt * 0.91 * 10.0) / 10.0);
                    wr.setMaxWeightGrams(Math.round(sampleWt * 1.09 * 10.0) / 10.0);
                    wr.setUniformityPct(87.5);
                    wr.setExpectedBreedWeightGrams(Math.round((42.0 + Math.pow(day / 40.0, 1.18) * 2358.0) * 10.0) / 10.0);
                    wr.setRecordedBy("farmer1");
                    weightRepository.save(wr);
                }

                DailyRecord dr = new DailyRecord();
                dr.setFarmId(farm1.getId());
                dr.setBatchId(b.getId());
                dr.setBatchCode(b.getBatchCode());
                dr.setRecordDate(date);
                dr.setDayNumber(day);
                dr.setOpeningBirds(opening);
                dr.setMortality(mort);
                dr.setCulls(cull);
                dr.setClosingBirds(closing);
                dr.setFeedType(feedType);
                dr.setFeedConsumptionKg(feedKg);
                dr.setAverageBirdWeightGrams(sampleWt);
                dr.setWaterConsumptionLiters(waterL);
                dr.setTemperatureCelsius(day <= 7 ? 32.0 : 26.5);
                dr.setHumidityPct(62.0);
                dr.setLabourCost(850.0);
                dr.setElectricityCost(220.0);
                dr.setFarmerRemarks("Healthy flock activity");
                dr.setRecordedBy("farmer1");
                dr = dailyRepository.save(dr);

                MortalityRecord mr = new MortalityRecord();
                mr.setFarmId(farm1.getId());
                mr.setBatchId(b.getId());
                mr.setBatchCode(b.getBatchCode());
                mr.setDailyRecordId(dr.getId());
                mr.setRecordDate(date);
                mr.setAgeDays(day);
                mr.setMortalityCount(mort);
                mr.setCullCount(cull);
                mr.setCause(day <= 7 ? "Early chick brooding stress" : "Normal flock attrition");
                mr.setRecordedBy("farmer1");
                mortalityRepository.save(mr);

                opening = closing;
                sumMort += mort;
                sumCulls += cull;
            }

            b.setTotalMortality(sumMort);
            b.setTotalCulls(sumCulls);
            b.setLatestAverageWeightGrams(latestWt);
            b.setLatestUniformityPct(87.5);
            batchRepository.save(b);
            batchService.syncHouseOccupancy(shed1.getId());
        }

        // 2. Seed Long-Cycle Commercial Layer Batch (BV-300: 504 days planned!)
        if (shed2 != null && bv300 != null) {
            LocalDate arrival = today.minusDays(210);
            Batch layer = new Batch();
            layer.setBatchCode("BATCH-MH-2026-L01");
            layer.setFarmId(farm1.getId());
            layer.setFarmName(farm1.getFarmName());
            layer.setPoultryHouseId(shed2.getId());
            layer.setPoultryHouseName(shed2.getHouseName());
            layer.setBatchName("BV-300 Peak White Egg Layer Flock");
            layer.setBreedId(bv300.getId());
            layer.setBreedCode(bv300.getBreedCode());
            layer.setBreedName(bv300.getBreedName());
            layer.setBatchCategory("LAYER");
            layer.setPurchaseDate(arrival);
            layer.setChickArrivalDate(arrival);
            layer.setStartingBirdQuantity(4000);
            layer.setPurchasePricePerChick(46.0);
            layer.setSupplierName("Venkateshwara BV-300 Pune");
            layer.setInitialBirdAgeDays(0);
            layer.setPlannedDurationDays(504); // 72 weeks! Never forced to 50 days
            layer.setChickInitialWeightGrams(38.0);
            layer.setLatestAverageWeightGrams(1510.0);
            layer.setTotalMortality(38);
            layer.setTotalCulls(7);
            layer.setStatus("ACTIVE");
            layer = batchRepository.save(layer);

            int liveHens = 4000 - 38 - 7;
            int sumGoodEggs = 0;
            int sumSoldEggs = 0;
            double sumEggRev = 0.0;

            for (int d = 39; d >= 0; d--) {
                LocalDate date = today.minusDays(d);
                int totalEggs = 3680 + (d % 7) * 14;
                int damaged = 16;
                int broken = 12;
                int dirty = 18;
                int good = totalEggs - damaged - broken - dirty;
                int sold = 3540;
                double price = 5.80;
                double rev = Math.round(sold * price * 100.0) / 100.0;

                EggRecord er = new EggRecord();
                er.setFarmId(farm1.getId());
                er.setBatchId(layer.getId());
                er.setBatchCode(layer.getBatchCode());
                er.setRecordDate(date);
                er.setLiveHenCount(liveHens);
                er.setTotalEggs(totalEggs);
                er.setGoodEggs(good);
                er.setDamagedEggs(damaged);
                er.setBrokenEggs(broken);
                er.setDirtyEggs(dirty);
                er.setGradeSmall((int) (good * 0.10));
                er.setGradeMedium((int) (good * 0.50));
                er.setGradeLarge((int) (good * 0.32));
                er.setGradeExtraLarge(good - er.getGradeSmall() - er.getGradeMedium() - er.getGradeLarge());
                er.setHenDayProductionPct(Math.round(((totalEggs * 100.0) / liveHens) * 100.0) / 100.0);
                er.setFeedConsumedKg(428.0);
                er.setAverageBirdWeightGrams(1510.0);
                er.setEggsSoldCount(sold);
                er.setPricePerEgg(price);
                er.setEggSaleRevenue(rev);
                er.setRecordedBy("manager1");
                eggRepository.save(er);

                sumGoodEggs += good;
                sumSoldEggs += sold;
                sumEggRev += rev;
            }

            layer.setTotalGoodEggsProduced(sumGoodEggs);
            layer.setTotalEggsSold(sumSoldEggs);
            layer.setTotalEggSalesRevenue(Math.round(sumEggRev * 100.0) / 100.0);
            batchRepository.save(layer);
            batchService.syncHouseOccupancy(shed2.getId());
        }

        // 3. Seed 120-Day Indigenous Kadaknath Batch on Farm 2 (MP - Isolated!)
        if (farm2 != null && kadaknath != null) {
            PoultryHouse shedK1 = houseRepository.findByFarmId(farm2.getId()).stream().findFirst().orElse(null);
            if (shedK1 != null) {
                LocalDate arrival = today.minusDays(85);
                Batch kBatch = new Batch();
                kBatch.setBatchCode("BATCH-MP-2026-K01");
                kBatch.setFarmId(farm2.getId());
                kBatch.setFarmName(farm2.getFarmName());
                kBatch.setPoultryHouseId(shedK1.getId());
                kBatch.setPoultryHouseName(shedK1.getHouseName());
                kBatch.setBatchName("GI Kadaknath Premium Free-Range Flock");
                kBatch.setBreedId(kadaknath.getId());
                kBatch.setBreedCode(kadaknath.getBreedCode());
                kBatch.setBreedName(kadaknath.getBreedName());
                kBatch.setBatchCategory("INDIGENOUS");
                kBatch.setPurchaseDate(arrival);
                kBatch.setChickArrivalDate(arrival);
                kBatch.setStartingBirdQuantity(1200);
                kBatch.setPurchasePricePerChick(70.0);
                kBatch.setSupplierName("Jhabua Krishi Vigyan Kendra Hatchery");
                kBatch.setPlannedDurationDays(120);
                kBatch.setChickInitialWeightGrams(30.0);
                kBatch.setLatestAverageWeightGrams(980.0);
                kBatch.setLiveSellingPricePerKg(420.0);
                kBatch.setTotalMortality(24);
                kBatch.setStatus("ACTIVE");
                batchRepository.save(kBatch);
                batchService.syncHouseOccupancy(shedK1.getId());
            }
        }

        log.info("Seeded Phase 2 sample Broiler (Ross 308), Layer (BV-300, 504d), and Indigenous (Kadaknath, 120d) batches!");
    }
}
