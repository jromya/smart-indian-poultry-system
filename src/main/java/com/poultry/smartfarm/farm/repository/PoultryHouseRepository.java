package com.poultry.smartfarm.farm.repository;

import com.poultry.smartfarm.farm.document.PoultryHouse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PoultryHouseRepository extends MongoRepository<PoultryHouse, String> {

    List<PoultryHouse> findByFarmId(String farmId);

    List<PoultryHouse> findByFarmIdIn(List<String> farmIds);

    boolean existsByFarmIdAndHouseCodeIgnoreCase(String farmId, String houseCode);
}
