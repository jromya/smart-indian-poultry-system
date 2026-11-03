package com.poultry.smartfarm.commerce.repository;

import com.poultry.smartfarm.commerce.document.Sale;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SaleRepository extends MongoRepository<Sale, String> {
    List<Sale> findByFarmIdOrderBySaleDateDescCreatedAtDesc(String farmId);
    List<Sale> findByFarmIdInOrderBySaleDateDescCreatedAtDesc(List<String> farmIds);
    List<Sale> findByBatchIdOrderBySaleDateDesc(String batchId);
    List<Sale> findByOwnerUserIdOrderBySaleDateDescCreatedAtDesc(String ownerUserId);
    List<Sale> findByCustomerIdOrderBySaleDateDesc(String customerId);
    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);
}
