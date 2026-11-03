package com.poultry.smartfarm.analytics.controller;

import com.poultry.smartfarm.analytics.service.AnalyticsService;
import com.poultry.smartfarm.common.dto.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/charts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnifiedAnalyticsCharts(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String breedId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String batchCategory,
            @RequestParam(required = false) String businessType) {
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.buildUnifiedAnalyticsCharts(
                        farmId, batchId, breedId, fromDate, toDate, batchCategory, businessType)));
    }
}
