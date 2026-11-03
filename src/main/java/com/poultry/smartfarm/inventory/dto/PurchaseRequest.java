package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class PurchaseRequest {

    @NotBlank(message = "Farm ID is required")
    private String farmId;

    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotBlank(message = "Supplier ID is required")
    private String supplierId;

    @NotBlank(message = "Inventory item ID is required")
    private String inventoryItemId;

    @NotNull
    @Positive(message = "Quantity purchased must be greater than zero")
    private Double quantityPurchased;

    @NotNull
    @PositiveOrZero(message = "Purchase rate per unit cannot be negative")
    private Double purchaseRatePerUnit;

    @PositiveOrZero(message = "Transport/other charges cannot be negative")
    private Double transportAndOtherCharges = 0.0;

    @PositiveOrZero(message = "Amount paid cannot be negative")
    private Double amountPaid = 0.0;

    private String batchLotNumber;
    private LocalDate expiryDate;
    private String remarks;

    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public Double getQuantityPurchased() { return quantityPurchased; }
    public void setQuantityPurchased(Double quantityPurchased) { this.quantityPurchased = quantityPurchased; }
    public Double getPurchaseRatePerUnit() { return purchaseRatePerUnit; }
    public void setPurchaseRatePerUnit(Double purchaseRatePerUnit) { this.purchaseRatePerUnit = purchaseRatePerUnit; }
    public Double getTransportAndOtherCharges() { return transportAndOtherCharges != null ? transportAndOtherCharges : 0.0; }
    public void setTransportAndOtherCharges(Double transportAndOtherCharges) { this.transportAndOtherCharges = transportAndOtherCharges; }
    public Double getAmountPaid() { return amountPaid != null ? amountPaid : 0.0; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }
    public String getBatchLotNumber() { return batchLotNumber; }
    public void setBatchLotNumber(String batchLotNumber) { this.batchLotNumber = batchLotNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
