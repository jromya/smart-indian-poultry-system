package com.poultry.smartfarm.batch.repository;

import com.poultry.smartfarm.batch.document.MortalityRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MortalityRecordRepository extends MongoRepository<MortalityRecord, String> {

    List<MortalityRecord> findByBatchIdOrderByRecordDateDesc(String batchId);

    Optional<MortalityRecord> findByDailyRecordId(String dailyRecordId);
}
