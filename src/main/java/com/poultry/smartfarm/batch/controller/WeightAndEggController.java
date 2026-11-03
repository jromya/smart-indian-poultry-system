package com.poultry.smartfarm.batch.controller;

import com.poultry.smartfarm.batch.document.EggRecord;
import com.poultry.smartfarm.batch.document.WeightRecord;
import com.poultry.smartfarm.batch.dto.EggRecordRequest;
import com.poultry.smartfarm.batch.dto.WeightRecordRequest;
import com.poultry.smartfarm.batch.service.PoultryAnalyticsCalculationService;
import com.poultry.smartfarm.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WeightAndEggController {

    private final PoultryAnalyticsCalculationService analyticsService;

    public WeightAndEggController(PoultryAnalyticsCalculationService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/weight-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<WeightRecord>>> getWeightRecords(@RequestParam String batchId) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getWeightRecords(batchId)));
    }

    @PostMapping("/weight-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<WeightRecord>> recordWeight(@Valid @RequestBody WeightRecordRequest request) {
        WeightRecord saved = analyticsService.recordWeightSample(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bird weight sample saved! Avg: " + saved.getAverageWeightGrams() + "g", saved));
    }

    @GetMapping("/egg-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<EggRecord>>> getEggRecords(@RequestParam String batchId) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getEggRecordsForBatch(batchId)));
    }

    @PostMapping("/egg-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<EggRecord>> recordEggCollection(@Valid @RequestBody EggRecordRequest request) {
        EggRecord saved = analyticsService.recordDailyEggCollection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Egg collection saved! Hen-Day Production: " + saved.getHenDayProductionPct() + "%", saved));
    }
}
