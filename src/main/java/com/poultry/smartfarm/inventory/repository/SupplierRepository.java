package com.poultry.smartfarm.inventory.repository;

import com.poultry.smartfarm.inventory.document.Supplier;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends MongoRepository<Supplier, String> {
    List<Supplier> findByFarmIdOrderBySupplierNameAsc(String farmId);
    List<Supplier> findByFarmIdInOrderBySupplierNameAsc(List<String> farmIds);
    Optional<Supplier> findBySupplierCodeIgnoreCase(String supplierCode);
    boolean existsBySupplierCodeIgnoreCase(String supplierCode);
}
