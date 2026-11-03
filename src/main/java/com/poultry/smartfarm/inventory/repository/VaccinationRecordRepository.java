package com.poultry.smartfarm.inventory.repository;

import com.poultry.smartfarm.inventory.document.VaccinationRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VaccinationRecordRepository extends MongoRepository<VaccinationRecord, String> {
    List<VaccinationRecord> findByBatchIdOrderByScheduledDateAsc(String batchId);
    List<VaccinationRecord> findByFarmIdOrderByScheduledDateAsc(String farmId);
    List<VaccinationRecord> findByFarmIdInOrderByScheduledDateAsc(List<String> farmIds);
    long countByBatchId(String batchId);
}
