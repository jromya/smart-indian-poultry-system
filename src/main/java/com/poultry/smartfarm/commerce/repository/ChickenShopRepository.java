package com.poultry.smartfarm.commerce.repository;

import com.poultry.smartfarm.commerce.document.ChickenShop;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChickenShopRepository extends MongoRepository<ChickenShop, String> {
    Optional<ChickenShop> findByOwnerUserId(String ownerUserId);
    Optional<ChickenShop> findByShopCodeIgnoreCase(String shopCode);
}
