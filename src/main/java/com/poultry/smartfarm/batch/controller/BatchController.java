package com.poultry.smartfarm.batch.controller;

import com.poultry.smartfarm.batch.document.MortalityRecord;
import com.poultry.smartfarm.batch.dto.BatchCreateRequest;
import com.poultry.smartfarm.batch.dto.BatchResponse;
import com.poultry.smartfarm.batch.dto.BirdHarvestSaleRequest;
import com.poultry.smartfarm.batch.repository.MortalityRecordRepository;
import com.poultry.smartfarm.batch.service.BatchService;
import com.poultry.smartfarm.batch.service.PoultryAnalyticsCalculationService;
import com.poultry.smartfarm.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;
    private final PoultryAnalyticsCalculationService analyticsService;
    private final MortalityRecordRepository mortalityRepository;

    public BatchController(BatchService batchService,
                           PoultryAnalyticsCalculationService analyticsService,
                           MortalityRecordRepository mortalityRepository) {
        this.batchService = batchService;
        this.analyticsService = analyticsService;
        this.mortalityRepository = mortalityRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> listBatches(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(batchService.listBatches(farmId, category, status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatch(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(batchService.getBatchById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<BatchResponse>> createBatch(@Valid @RequestBody BatchCreateRequest request) {
        BatchResponse created = batchService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Poultry batch created successfully!", created));
    }

    @PatchMapping("/{id}/pricing")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<BatchResponse>> updatePricing(
            @PathVariable String id,
            @RequestParam(required = false) Double liveSellingPricePerKg,
            @RequestParam(required = false) Double estimatedFeedRatePerKg) {
        return ResponseEntity.ok(ApiResponse.ok("Batch pricing updated!",
                batchService.updatePricingTargets(id, liveSellingPricePerKg, estimatedFeedRatePerKg)));
    }

    @PostMapping("/{id}/bird-sale")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<BatchResponse>> recordBirdSale(
            @PathVariable String id,
            @Valid @RequestBody BirdHarvestSaleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Live bird sale recorded and flock count updated!",
                batchService.recordBirdHarvestSale(id, request)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<BatchResponse>> completeBatch(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate completionDate) {
        return ResponseEntity.ok(ApiResponse.ok("Batch marked as COMPLETED and locked against ordinary edits.",
                batchService.completeBatch(id, completionDate)));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN')")
    public ResponseEntity<ApiResponse<BatchResponse>> reopenBatch(
            @PathVariable String id,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.ok("Batch reopened for authorized correction with audit log.",
                batchService.reopenBatch(id, reason)));
    }

    @GetMapping("/{id}/broiler-analytics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBroilerAnalytics(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.calculateBroilerAnalytics(id, fromDate, toDate)));
    }

    @GetMapping("/{id}/layer-analytics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLayerAnalytics(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.calculateLayerAnalytics(id)));
    }

    @GetMapping("/{id}/mortality-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<MortalityRecord>>> getMortalityHistory(@PathVariable String id) {
        batchService.getBatchDocumentWithAccess(id);
        return ResponseEntity.ok(ApiResponse.ok(mortalityRepository.findByBatchIdOrderByRecordDateDesc(id)));
    }
}
