package com.poultry.smartfarm.batch.repository;

import com.poultry.smartfarm.batch.document.WeightRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeightRecordRepository extends MongoRepository<WeightRecord, String> {

    List<WeightRecord> findByBatchIdOrderByRecordDateAsc(String batchId);

    Optional<WeightRecord> findByBatchIdAndRecordDate(String batchId, LocalDate recordDate);

    List<WeightRecord> findByBatchIdAndRecordDateBetweenOrderByRecordDateAsc(
            String batchId, LocalDate fromDate, LocalDate toDate);
}
