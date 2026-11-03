package com.poultry.smartfarm.commerce.repository;

import com.poultry.smartfarm.commerce.document.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByFarmIdOrderByExpenseDateDescCreatedAtDesc(String farmId);
    List<Expense> findByFarmIdInOrderByExpenseDateDescCreatedAtDesc(List<String> farmIds);
    List<Expense> findByBatchIdOrderByExpenseDateDesc(String batchId);
    List<Expense> findByOwnerUserIdOrderByExpenseDateDescCreatedAtDesc(String ownerUserId);
}
