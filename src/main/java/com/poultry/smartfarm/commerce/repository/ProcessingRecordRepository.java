package com.poultry.smartfarm.commerce.repository;

import com.poultry.smartfarm.commerce.document.ProcessingRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProcessingRecordRepository extends MongoRepository<ProcessingRecord, String> {
    List<ProcessingRecord> findByShopIdOrderByRecordDateDescCreatedAtDesc(String shopId);
    List<ProcessingRecord> findByOwnerUserIdOrderByRecordDateDescCreatedAtDesc(String ownerUserId);
}
