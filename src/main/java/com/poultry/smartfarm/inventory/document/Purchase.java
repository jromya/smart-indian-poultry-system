package com.poultry.smartfarm.inventory.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "purchases")
public class Purchase {

    @Id
    private String id;

    @Indexed
    private String farmId;
    private String farmName;

    @Indexed
    private String invoiceNumber;

    @Indexed
    private LocalDate purchaseDate;

    @Indexed
    private String supplierId;
    private String supplierName;

    @Indexed
    private String inventoryItemId;
    private String itemCode;
    private String itemName;

    // FEED, MEDICINE, VACCINE, EQUIPMENT, PACKAGING, OTHER
    private String category;
    private String subCategory;

    private Double quantityPurchased;
    private String unit;
    private Double purchaseRatePerUnit;
    private Double transportAndOtherCharges = 0.0;
    private Double totalInvoiceAmount;

    private Double amountPaid = 0.0;
    private Double balancePayable = 0.0;
    // PAID, PARTIAL, CREDIT_PENDING
    private String paymentStatus = "PAID";

    private String batchLotNumber;
    private LocalDate expiryDate;
    private String remarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public Purchase() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public Double getQuantityPurchased() { return quantityPurchased != null ? quantityPurchased : 0.0; }
    public void setQuantityPurchased(Double quantityPurchased) { this.quantityPurchased = quantityPurchased; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getPurchaseRatePerUnit() { return purchaseRatePerUnit != null ? purchaseRatePerUnit : 0.0; }
    public void setPurchaseRatePerUnit(Double purchaseRatePerUnit) { this.purchaseRatePerUnit = purchaseRatePerUnit; }
    public Double getTransportAndOtherCharges() { return transportAndOtherCharges != null ? transportAndOtherCharges : 0.0; }
    public void setTransportAndOtherCharges(Double transportAndOtherCharges) { this.transportAndOtherCharges = transportAndOtherCharges; }
    public Double getTotalInvoiceAmount() { return totalInvoiceAmount != null ? totalInvoiceAmount : 0.0; }
    public void setTotalInvoiceAmount(Double totalInvoiceAmount) { this.totalInvoiceAmount = totalInvoiceAmount; }
    public Double getAmountPaid() { return amountPaid != null ? amountPaid : 0.0; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }
    public Double getBalancePayable() { return balancePayable != null ? balancePayable : 0.0; }
    public void setBalancePayable(Double balancePayable) { this.balancePayable = balancePayable; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getBatchLotNumber() { return batchLotNumber; }
    public void setBatchLotNumber(String batchLotNumber) { this.batchLotNumber = batchLotNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
