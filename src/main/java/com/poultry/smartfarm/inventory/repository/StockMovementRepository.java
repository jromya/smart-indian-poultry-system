package com.poultry.smartfarm.inventory.repository;

import com.poultry.smartfarm.inventory.document.StockMovement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StockMovementRepository extends MongoRepository<StockMovement, String> {
    List<StockMovement> findTop100ByFarmIdOrderByMovementAtDesc(String farmId);
    List<StockMovement> findTop100ByFarmIdInOrderByMovementAtDesc(List<String> farmIds);
    List<StockMovement> findByInventoryItemIdOrderByMovementAtDesc(String inventoryItemId);
}
