package com.poultry.smartfarm.inventory.repository;

import com.poultry.smartfarm.inventory.document.InventoryItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends MongoRepository<InventoryItem, String> {
    List<InventoryItem> findByFarmIdOrderByCategoryAscItemNameAsc(String farmId);
    List<InventoryItem> findByFarmIdInOrderByCategoryAscItemNameAsc(List<String> farmIds);
    List<InventoryItem> findByFarmIdAndCategoryIgnoreCaseOrderByItemNameAsc(String farmId, String category);
    Optional<InventoryItem> findByFarmIdAndItemCodeIgnoreCase(String farmId, String itemCode);
    boolean existsByFarmIdAndItemCodeIgnoreCase(String farmId, String itemCode);
}
