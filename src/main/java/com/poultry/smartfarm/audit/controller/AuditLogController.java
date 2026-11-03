package com.poultry.smartfarm.audit.controller;

import com.poultry.smartfarm.audit.document.AuditLog;
import com.poultry.smartfarm.audit.service.AuditLogService;
import com.poultry.smartfarm.common.dto.ApiResponse;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.security.FarmAccessGuard;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final FarmAccessGuard farmAccessGuard;

    public AuditLogController(AuditLogService auditLogService, FarmAccessGuard farmAccessGuard) {
        this.auditLogService = auditLogService;
        this.farmAccessGuard = farmAccessGuard;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'FARM_ADMIN', 'FARM_MANAGER')")
    public ApiResponse<List<AuditLog>> getRecentAuditLogs() {
        User currentUser = farmAccessGuard.getCurrentUser();
        return ApiResponse.ok("Audit history retrieved", auditLogService.getRecentLogsForUser(currentUser));
    }
}
