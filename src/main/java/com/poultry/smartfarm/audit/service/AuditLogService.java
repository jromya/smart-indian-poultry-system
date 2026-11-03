package com.poultry.smartfarm.audit.service;

import com.poultry.smartfarm.audit.document.AuditLog;
import com.poultry.smartfarm.audit.repository.AuditLogRepository;
import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(User actor, String farmId, String action, String entityType, String entityId, String summary) {
        String userId = actor != null ? actor.getId() : "SYSTEM";
        String username = actor != null ? actor.getUsername() : "SYSTEM";
        String role = (actor != null && actor.getRole() != null) ? actor.getRole().name() : "SYSTEM";

        AuditLog entry = new AuditLog(userId, username, role, farmId, action, entityType, entityId, summary);
        auditLogRepository.save(entry);
    }

    public List<AuditLog> getRecentLogsForUser(User currentUser) {
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return auditLogRepository.findTop30ByOrderByTimestampDesc();
        }
        if (currentUser.getRole() == Role.FARM_ADMIN || currentUser.getRole() == Role.FARM_MANAGER) {
            if (currentUser.getAssignedFarmIds() == null || currentUser.getAssignedFarmIds().isEmpty()) {
                return Collections.emptyList();
            }
            return auditLogRepository.findTop30ByFarmIdInOrderByTimestampDesc(currentUser.getAssignedFarmIds());
        }
        return Collections.emptyList();
    }
}
