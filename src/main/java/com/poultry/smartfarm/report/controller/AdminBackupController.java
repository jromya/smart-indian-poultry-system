package com.poultry.smartfarm.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.report.service.AdminBackupService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/backup")
public class AdminBackupController {

    private final AdminBackupService backupService;
    private final ObjectMapper objectMapper;

    public AdminBackupController(AdminBackupService backupService, ObjectMapper objectMapper) {
        this.backupService = backupService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBackupSummary() {
        return ResponseEntity.ok(ApiResponse.ok(backupService.getCollectionHealthSummary()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<byte[]> downloadDatabaseBackupJson() throws Exception {
        Map<String, Object> snapshot = backupService.exportFullDatabaseSnapshot();
        byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(snapshot);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("smart-poultry-backup-" + LocalDate.now() + ".json")
                .build());
        headers.setContentLength(jsonBytes.length);
        return new ResponseEntity<>(jsonBytes, headers, HttpStatus.OK);
    }
}
