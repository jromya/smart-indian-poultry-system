package com.poultry.smartfarm.inventory.repository;

import com.poultry.smartfarm.inventory.document.Purchase;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PurchaseRepository extends MongoRepository<Purchase, String> {
    List<Purchase> findByFarmIdOrderByPurchaseDateDescCreatedAtDesc(String farmId);
    List<Purchase> findByFarmIdInOrderByPurchaseDateDescCreatedAtDesc(List<String> farmIds);
    List<Purchase> findBySupplierIdOrderByPurchaseDateDesc(String supplierId);
}
