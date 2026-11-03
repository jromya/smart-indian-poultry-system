package com.poultry.smartfarm.breed.repository;

import com.poultry.smartfarm.breed.document.Breed;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BreedRepository extends MongoRepository<Breed, String> {

    List<Breed> findByBreedCategoryIgnoreCaseOrderByBreedNameAsc(String breedCategory);

    List<Breed> findAllByOrderByBreedCategoryAscBreedNameAsc();

    Optional<Breed> findByBreedCodeIgnoreCase(String breedCode);

    boolean existsByBreedCodeIgnoreCase(String breedCode);

    boolean existsByBreedNameIgnoreCase(String breedName);
}
