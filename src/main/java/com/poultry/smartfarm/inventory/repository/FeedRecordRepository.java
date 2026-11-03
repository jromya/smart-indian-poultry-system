package com.poultry.smartfarm.inventory.repository;

import com.poultry.smartfarm.inventory.document.FeedRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedRecordRepository extends MongoRepository<FeedRecord, String> {
    List<FeedRecord> findByBatchIdOrderByRecordDateDescCreatedAtDesc(String batchId);
    List<FeedRecord> findByFarmIdOrderByRecordDateDescCreatedAtDesc(String farmId);
    List<FeedRecord> findByFarmIdInOrderByRecordDateDescCreatedAtDesc(List<String> farmIds);
}
