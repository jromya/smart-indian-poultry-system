package com.poultry.smartfarm.commerce.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "sales")
public class Sale {

    @Id
    private String id;

    @Indexed(unique = true)
    private String invoiceNumber;

    @Indexed
    private LocalDate saleDate;

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

    // LIVE_CHICKEN_SALE, EGG_SALE, DRESSED_MEAT_SALE, MANURE_BYPRODUCT_SALE
    @Indexed
    private String saleCategory;

    private String productDescription;

    // WHOLESALER, CHICKEN_RETAILER, MEAT_SHOP, EGG_RETAILER, DIRECT_CUSTOMER
    private String buyerCategory = "DIRECT_CUSTOMER";

    @Indexed
    private String customerId;
    private String customerName;
    private String customerPhone;

    private Double quantity;
    private String unit;
    private Double liveWeightKg;
    private Double unitSellingPriceInr;
    private Double totalSaleAmountInr;

    private Double cashReceivedInr = 0.0;
    private Double outstandingBalanceInr = 0.0;
    // PAID, PARTIAL, CREDIT_PENDING
    private String paymentStatus = "PAID";
    private String paymentMode = "CASH";

    private String remarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public Sale() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
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
    public String getSaleCategory() { return saleCategory; }
    public void setSaleCategory(String saleCategory) { this.saleCategory = saleCategory; }
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public String getBuyerCategory() { return buyerCategory; }
    public void setBuyerCategory(String buyerCategory) { this.buyerCategory = buyerCategory; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Double getQuantity() { return quantity != null ? quantity : 0.0; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getLiveWeightKg() { return liveWeightKg; }
    public void setLiveWeightKg(Double liveWeightKg) { this.liveWeightKg = liveWeightKg; }
    public Double getUnitSellingPriceInr() { return unitSellingPriceInr != null ? unitSellingPriceInr : 0.0; }
    public void setUnitSellingPriceInr(Double unitSellingPriceInr) { this.unitSellingPriceInr = unitSellingPriceInr; }
    public Double getTotalSaleAmountInr() { return totalSaleAmountInr != null ? totalSaleAmountInr : 0.0; }
    public void setTotalSaleAmountInr(Double totalSaleAmountInr) { this.totalSaleAmountInr = totalSaleAmountInr; }
    public Double getCashReceivedInr() { return cashReceivedInr != null ? cashReceivedInr : 0.0; }
    public void setCashReceivedInr(Double cashReceivedInr) { this.cashReceivedInr = cashReceivedInr; }
    public Double getOutstandingBalanceInr() { return outstandingBalanceInr != null ? outstandingBalanceInr : 0.0; }
    public void setOutstandingBalanceInr(Double outstandingBalanceInr) { this.outstandingBalanceInr = outstandingBalanceInr; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
