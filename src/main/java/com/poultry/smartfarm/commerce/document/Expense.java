package com.poultry.smartfarm.commerce.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;

    @Indexed
    private LocalDate expenseDate;

    // FARM, RETAIL, CHICKEN_SHOP
    @Indexed
    private String businessContext = "FARM";

    @Indexed
    private String farmId;
    private String farmName;

    @Indexed
    private String batchId;
    private String batchCode;

    @Indexed
    private String ownerUserId;
    private String retailStoreId;
    private String chickenShopId;

    // LABOUR, ELECTRICITY, TRANSPORTATION, EQUIPMENT, FEED, MEDICINE, CHICK_PURCHASE, PROCESSING, SHOP_RENT, OTHER
    @Indexed
    private String expenseCategory;

    private Double amountInr;
    private Double paidAmountInr;
    private String paymentMode = "CASH";
    private String payeeName;
    private String description;

    // Prevents double counting when a stock purchase is logged for cash tracking vs actual P&L consumption
    private boolean stockPurchaseOnly = false;

    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public Expense() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getBusinessContext() { return businessContext; }
    public void setBusinessContext(String businessContext) { this.businessContext = businessContext; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getRetailStoreId() { return retailStoreId; }
    public void setRetailStoreId(String retailStoreId) { this.retailStoreId = retailStoreId; }
    public String getChickenShopId() { return chickenShopId; }
    public void setChickenShopId(String chickenShopId) { this.chickenShopId = chickenShopId; }
    public String getExpenseCategory() { return expenseCategory; }
    public void setExpenseCategory(String expenseCategory) { this.expenseCategory = expenseCategory; }
    public Double getAmountInr() { return amountInr != null ? amountInr : 0.0; }
    public void setAmountInr(Double amountInr) { this.amountInr = amountInr; }
    public Double getPaidAmountInr() { return paidAmountInr != null ? paidAmountInr : getAmountInr(); }
    public void setPaidAmountInr(Double paidAmountInr) { this.paidAmountInr = paidAmountInr; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isStockPurchaseOnly() { return stockPurchaseOnly; }
    public void setStockPurchaseOnly(boolean stockPurchaseOnly) { this.stockPurchaseOnly = stockPurchaseOnly; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
