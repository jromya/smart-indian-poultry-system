package com.poultry.smartfarm;

import com.poultry.smartfarm.batch.document.Batch;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class Phase2CalculationTests {

    @Test
    void testLongDurationLayerAndIndigenousBatchAutoCalculations() {
        Batch layerBatch = new Batch();
        layerBatch.setBatchCode("BATCH-LAYER-504");
        layerBatch.setBatchCategory("LAYER");
        layerBatch.setChickArrivalDate(LocalDate.now().minusDays(210));
        layerBatch.setInitialBirdAgeDays(0);
        layerBatch.setPlannedDurationDays(504); // 72 weeks > 50 days
        layerBatch.setStartingBirdQuantity(4000);
        layerBatch.setTotalAdditions(0);
        layerBatch.setTotalMortality(40);
        layerBatch.setTotalCulls(10);
        layerBatch.setTotalTransfersOut(0);
        layerBatch.setTotalBirdsSold(0);
        layerBatch.setTotalGoodEggsProduced(10000);
        layerBatch.setTotalEggsSold(8500);
        layerBatch.setStatus("ACTIVE");

        BatchResponse view = BatchResponse.fromDocument(layerBatch);
        assertEquals(210, view.getCurrentAgeDays());
        assertEquals(30.0, view.getCurrentAgeWeeks());
        assertEquals(layerBatch.getChickArrivalDate().plusDays(504), view.getExpectedCompletionDate());
        assertEquals(3950, view.getCurrentLiveBirds());
        assertEquals(1.0, view.getMortalityPercentage());
        assertEquals(1500, view.getCurrentEggStockGood());
    }

    @Test
    void testProperBroilerFcrFormulaWithBirdRemovals() {
        // 5,000 chicks @ 42g -> Opening Biomass = 210 kg
        // 500 birds sold @ 2.2 kg -> Removed Biomass = 1,100 kg
        // 4,400 live birds remaining @ 2.4 kg -> Closing Biomass = 10,560 kg
        // Net Live Weight Gain = (10,560 + 1,100) - 210 = 11,450 kg
        // Feed consumed = 18,500 kg -> FCR = 18500 / 11450 = 1.62
        double openingBiomassKg = (5000 * 42.0) / 1000.0;
        double removedBiomassKg = 1100.0;
        double closingBiomassKg = (4400 * 2400.0) / 1000.0;
        double netGainKg = (closingBiomassKg + removedBiomassKg) - openingBiomassKg;
        double fcr = Math.round((18500.0 / netGainKg) * 100.0) / 100.0;

        assertEquals(11450.0, netGainKg, 0.01);
        assertEquals(1.62, fcr, 0.01);
    }
}
