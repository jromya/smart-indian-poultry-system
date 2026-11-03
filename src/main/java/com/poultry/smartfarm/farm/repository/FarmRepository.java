package com.poultry.smartfarm.farm.repository;

import com.poultry.smartfarm.farm.document.Farm;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FarmRepository extends MongoRepository<Farm, String> {

    Optional<Farm> findByFarmCodeIgnoreCase(String farmCode);

    boolean existsByFarmCodeIgnoreCase(String farmCode);

    List<Farm> findByIdIn(List<String> ids);
}
