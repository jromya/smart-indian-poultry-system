package com.poultry.smartfarm.batch.repository;

import com.poultry.smartfarm.batch.document.DailyRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRecordRepository extends MongoRepository<DailyRecord, String> {

    boolean existsByBatchIdAndRecordDate(String batchId, LocalDate recordDate);

    Optional<DailyRecord> findByBatchIdAndRecordDate(String batchId, LocalDate recordDate);

    List<DailyRecord> findByBatchIdOrderByRecordDateAsc(String batchId);

    List<DailyRecord> findByBatchIdOrderByRecordDateDesc(String batchId);

    Optional<DailyRecord> findFirstByBatchIdAndRecordDateBeforeOrderByRecordDateDesc(String batchId, LocalDate recordDate);

    List<DailyRecord> findByBatchIdAndRecordDateBetweenOrderByRecordDateAsc(
            String batchId, LocalDate fromDate, LocalDate toDate);

    List<DailyRecord> findByFarmIdInOrderByRecordDateDesc(List<String> farmIds);
}
