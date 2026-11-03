package com.poultry.smartfarm.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class RetailStockPurchaseRequest {

    private String storeId;

    // LIVE_BIRDS or EGGS
    @NotBlank(message = "Product type (LIVE_BIRDS or EGGS) is required")
    private String productType;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotNull
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotNull
    @PositiveOrZero(message = "Purchase rate per unit cannot be negative")
    private Double unitPurchaseCostInr;

    @PositiveOrZero
    private Double transportExpenseInr = 0.0;

    private String supplierFarmName;

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPurchaseCostInr() { return unitPurchaseCostInr; }
    public void setUnitPurchaseCostInr(Double unitPurchaseCostInr) { this.unitPurchaseCostInr = unitPurchaseCostInr; }
    public Double getTransportExpenseInr() { return transportExpenseInr != null ? transportExpenseInr : 0.0; }
    public void setTransportExpenseInr(Double transportExpenseInr) { this.transportExpenseInr = transportExpenseInr; }
    public String getSupplierFarmName() { return supplierFarmName; }
    public void setSupplierFarmName(String supplierFarmName) { this.supplierFarmName = supplierFarmName; }
}
