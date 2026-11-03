package com.poultry.smartfarm.batch.controller;

import com.poultry.smartfarm.batch.document.DailyRecord;
import com.poultry.smartfarm.batch.dto.DailyRecordRequest;
import com.poultry.smartfarm.batch.service.DailyRecordService;
import com.poultry.smartfarm.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/daily-records")
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;

    public DailyRecordController(DailyRecordService dailyRecordService) {
        this.dailyRecordService = dailyRecordService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<DailyRecord>>> getRecords(@RequestParam String batchId) {
        return ResponseEntity.ok(ApiResponse.ok(dailyRecordService.getRecordsForBatch(batchId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<DailyRecord>> createDailyRecord(@Valid @RequestBody DailyRecordRequest request) {
        DailyRecord saved = dailyRecordService.createDailyRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Daily poultry entry saved! Closing live birds: " + saved.getClosingBirds(), saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ResponseEntity<ApiResponse<DailyRecord>> correctDailyRecord(
            @PathVariable String id,
            @Valid @RequestBody DailyRecordRequest request) {
        DailyRecord updated = dailyRecordService.correctDailyRecord(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Authorized correction applied and balances recalculated!", updated));
    }
}
