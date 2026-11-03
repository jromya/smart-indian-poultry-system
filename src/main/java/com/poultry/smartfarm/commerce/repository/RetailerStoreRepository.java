package com.poultry.smartfarm.commerce.repository;

import com.poultry.smartfarm.commerce.document.RetailerStore;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RetailerStoreRepository extends MongoRepository<RetailerStore, String> {
    Optional<RetailerStore> findByOwnerUserId(String ownerUserId);
    Optional<RetailerStore> findByStoreCodeIgnoreCase(String storeCode);
}
