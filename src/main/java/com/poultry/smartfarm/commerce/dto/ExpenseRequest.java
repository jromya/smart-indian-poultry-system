package com.poultry.smartfarm.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class ExpenseRequest {

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    private String farmId;
    private String batchId;
    private String retailStoreId;
    private String chickenShopId;

    // LABOUR, ELECTRICITY, TRANSPORTATION, EQUIPMENT, PROCESSING, SHOP_RENT, OTHER
    @NotBlank(message = "Expense category is required")
    private String expenseCategory;

    @NotNull
    @Positive(message = "Expense amount must be greater than zero")
    private Double amountInr;

    private String paymentMode = "CASH";
    private String payeeName;
    private String description;

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getRetailStoreId() { return retailStoreId; }
    public void setRetailStoreId(String retailStoreId) { this.retailStoreId = retailStoreId; }
    public String getChickenShopId() { return chickenShopId; }
    public void setChickenShopId(String chickenShopId) { this.chickenShopId = chickenShopId; }
    public String getExpenseCategory() { return expenseCategory; }
    public void setExpenseCategory(String expenseCategory) { this.expenseCategory = expenseCategory; }
    public Double getAmountInr() { return amountInr; }
    public void setAmountInr(Double amountInr) { this.amountInr = amountInr; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
