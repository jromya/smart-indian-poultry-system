package com.poultry.smartfarm.commerce.repository;

import com.poultry.smartfarm.commerce.document.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    List<Customer> findByFarmIdOrderByCustomerNameAsc(String farmId);
    List<Customer> findByFarmIdInOrderByCustomerNameAsc(List<String> farmIds);
    List<Customer> findByOwnerUserIdOrderByCustomerNameAsc(String ownerUserId);
    Optional<Customer> findByCustomerCodeIgnoreCase(String customerCode);
    boolean existsByCustomerCodeIgnoreCase(String customerCode);
}
