package com.poultry.smartfarm.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class SaleRequest {

    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    @NotNull(message = "Sale date is required")
    private LocalDate saleDate;

    private String farmId;
    private String batchId;
    private String retailStoreId;
    private String chickenShopId;

    // LIVE_CHICKEN_SALE, EGG_SALE, DRESSED_MEAT_SALE, MANURE_BYPRODUCT_SALE
    @NotBlank(message = "Sale category is required")
    private String saleCategory;

    private String productDescription;
    private String customerId;
    private String walkInCustomerName;
    private String walkInCustomerPhone;
    private String buyerCategory = "DIRECT_CUSTOMER";

    @NotNull
    @Positive(message = "Quantity must be greater than zero")
    private Double quantity;

    @NotBlank(message = "Unit is required")
    private String unit = "KG";

    private Double liveWeightKg;

    @NotNull
    @Positive(message = "Selling price per unit must be greater than zero")
    private Double unitSellingPriceInr;

    @PositiveOrZero(message = "Cash received cannot be negative")
    private Double cashReceivedInr;

    private String paymentMode = "CASH";
    private String remarks;

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getRetailStoreId() { return retailStoreId; }
    public void setRetailStoreId(String retailStoreId) { this.retailStoreId = retailStoreId; }
    public String getChickenShopId() { return chickenShopId; }
    public void setChickenShopId(String chickenShopId) { this.chickenShopId = chickenShopId; }
    public String getSaleCategory() { return saleCategory; }
    public void setSaleCategory(String saleCategory) { this.saleCategory = saleCategory; }
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getWalkInCustomerName() { return walkInCustomerName; }
    public void setWalkInCustomerName(String walkInCustomerName) { this.walkInCustomerName = walkInCustomerName; }
    public String getWalkInCustomerPhone() { return walkInCustomerPhone; }
    public void setWalkInCustomerPhone(String walkInCustomerPhone) { this.walkInCustomerPhone = walkInCustomerPhone; }
    public String getBuyerCategory() { return buyerCategory; }
    public void setBuyerCategory(String buyerCategory) { this.buyerCategory = buyerCategory; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getLiveWeightKg() { return liveWeightKg; }
    public void setLiveWeightKg(Double liveWeightKg) { this.liveWeightKg = liveWeightKg; }
    public Double getUnitSellingPriceInr() { return unitSellingPriceInr; }
    public void setUnitSellingPriceInr(Double unitSellingPriceInr) { this.unitSellingPriceInr = unitSellingPriceInr; }
    public Double getCashReceivedInr() { return cashReceivedInr; }
    public void setCashReceivedInr(Double cashReceivedInr) { this.cashReceivedInr = cashReceivedInr; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
