package com.poultry.smartfarm.audit.repository;

import com.poultry.smartfarm.audit.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findTop30ByOrderByTimestampDesc();

    List<AuditLog> findTop30ByFarmIdInOrderByTimestampDesc(List<String> farmIds);
}
