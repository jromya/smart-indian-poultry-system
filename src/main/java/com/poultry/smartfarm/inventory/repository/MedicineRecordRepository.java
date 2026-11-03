package com.poultry.smartfarm.inventory.repository;

import com.poultry.smartfarm.inventory.document.MedicineRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MedicineRecordRepository extends MongoRepository<MedicineRecord, String> {
    List<MedicineRecord> findByBatchIdOrderByAdministrationDateDescCreatedAtDesc(String batchId);
    List<MedicineRecord> findByFarmIdOrderByAdministrationDateDescCreatedAtDesc(String farmId);
    List<MedicineRecord> findByFarmIdInOrderByAdministrationDateDescCreatedAtDesc(List<String> farmIds);
}
