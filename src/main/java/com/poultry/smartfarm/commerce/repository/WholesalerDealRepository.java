package com.poultry.smartfarm.commerce.repository;

import com.poultry.smartfarm.commerce.document.WholesalerDeal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WholesalerDealRepository extends MongoRepository<WholesalerDeal, String> {
    List<WholesalerDeal> findByFarmIdOrderByDealDateDescCreatedAtDesc(String farmId);
    List<WholesalerDeal> findByFarmIdInOrderByDealDateDescCreatedAtDesc(List<String> farmIds);
    List<WholesalerDeal> findByBatchIdOrderByDealDateDesc(String batchId);
    boolean existsByDealNumberIgnoreCase(String dealNumber);
}
