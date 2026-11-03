package com.poultry.smartfarm.batch.repository;

import com.poultry.smartfarm.batch.document.EggRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EggRecordRepository extends MongoRepository<EggRecord, String> {

    boolean existsByBatchIdAndRecordDate(String batchId, LocalDate recordDate);

    Optional<EggRecord> findByBatchIdAndRecordDate(String batchId, LocalDate recordDate);

    List<EggRecord> findByBatchIdOrderByRecordDateAsc(String batchId);

    List<EggRecord> findByBatchIdOrderByRecordDateDesc(String batchId);

    List<EggRecord> findByFarmIdInOrderByRecordDateDesc(List<String> farmIds);
}
