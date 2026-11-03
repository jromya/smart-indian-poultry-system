package com.poultry.smartfarm.commerce.repository;

import com.poultry.smartfarm.commerce.document.PaymentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRecordRepository extends MongoRepository<PaymentRecord, String> {
    List<PaymentRecord> findByFarmIdOrderByPaymentDateDescCreatedAtDesc(String farmId);
    List<PaymentRecord> findByFarmIdInOrderByPaymentDateDescCreatedAtDesc(List<String> farmIds);
    List<PaymentRecord> findByOwnerUserIdOrderByPaymentDateDescCreatedAtDesc(String ownerUserId);
    List<PaymentRecord> findByCustomerIdOrderByPaymentDateDesc(String customerId);
    List<PaymentRecord> findBySupplierIdOrderByPaymentDateDesc(String supplierId);
}
