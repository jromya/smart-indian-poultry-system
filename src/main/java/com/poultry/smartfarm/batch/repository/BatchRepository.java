package com.poultry.smartfarm.batch.repository;

import com.poultry.smartfarm.batch.document.Batch;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends MongoRepository<Batch, String> {

    boolean existsByBatchCodeIgnoreCase(String batchCode);

    Optional<Batch> findByBatchCodeIgnoreCase(String batchCode);

    List<Batch> findByFarmIdOrderByChickArrivalDateDesc(String farmId);

    List<Batch> findByFarmIdInOrderByChickArrivalDateDesc(List<String> farmIds);

    List<Batch> findAllByOrderByChickArrivalDateDesc();

    List<Batch> findByPoultryHouseIdAndStatusIn(String poultryHouseId, List<String> statuses);
}
