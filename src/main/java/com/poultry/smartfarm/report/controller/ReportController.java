package com.poultry.smartfarm.report.controller;

import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.report.document.BatchReport;
import com.poultry.smartfarm.report.service.BatchPdfReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final BatchPdfReportService reportService;

    public ReportController(BatchPdfReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<BatchReport>>> listReports(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String batchId) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.listAccessibleReports(farmId, batchId)));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<ApiResponse<BatchReport>> generateBatchReport(
            @RequestParam String batchId,
            @RequestParam(required = false) String lang) {
        BatchReport created = reportService.generateAndSaveReport(batchId, lang, false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Batch performance & financial report generated!", created));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER', 'FARMER')")
    public ResponseEntity<byte[]> downloadReportPdf(
            @PathVariable String id,
            @RequestParam(required = false) String lang) {
        BatchReport rpt = reportService.getReportWithAccess(id);
        byte[] pdfBytes = reportService.renderPdfBytes(rpt, lang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(rpt.getReportNumber() + ".pdf")
                .build());
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
