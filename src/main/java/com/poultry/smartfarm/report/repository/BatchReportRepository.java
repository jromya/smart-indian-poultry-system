package com.poultry.smartfarm.report.repository;

import com.poultry.smartfarm.report.document.BatchReport;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BatchReportRepository extends MongoRepository<BatchReport, String> {
    List<BatchReport> findByFarmIdOrderByGeneratedAtDesc(String farmId);
    List<BatchReport> findByFarmIdInOrderByGeneratedAtDesc(List<String> farmIds);
    List<BatchReport> findByBatchIdOrderByGeneratedAtDesc(String batchId);
    List<BatchReport> findAllByOrderByGeneratedAtDesc();
    Optional<BatchReport> findFirstByBatchIdOrderByGeneratedAtDesc(String batchId);
}
