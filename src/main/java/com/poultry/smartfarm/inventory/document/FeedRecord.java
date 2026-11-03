package com.poultry.smartfarm.inventory.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "feed_records")
public class FeedRecord {

    @Id
    private String id;

    @Indexed
    private String farmId;

    @Indexed
    private String batchId;
    private String batchCode;
    private String batchName;

    @Indexed
    private String inventoryItemId;
    private String feedItemName;

    // STARTER, GROWER, FINISHER, LAYER_MASH, DESI_GRAIN_MIX
    private String feedCategory;
    private String supplierName;

    @Indexed
    private LocalDate recordDate;

    private Double quantityIssuedKg;
    private Double purchaseRatePerKg;
    private Double totalFeedCostInr;
    private Double remainingStockKg;

    private String remarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public FeedRecord() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public String getFeedItemName() { return feedItemName; }
    public void setFeedItemName(String feedItemName) { this.feedItemName = feedItemName; }
    public String getFeedCategory() { return feedCategory; }
    public void setFeedCategory(String feedCategory) { this.feedCategory = feedCategory; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Double getQuantityIssuedKg() { return quantityIssuedKg != null ? quantityIssuedKg : 0.0; }
    public void setQuantityIssuedKg(Double quantityIssuedKg) { this.quantityIssuedKg = quantityIssuedKg; }
    public Double getPurchaseRatePerKg() { return purchaseRatePerKg != null ? purchaseRatePerKg : 0.0; }
    public void setPurchaseRatePerKg(Double purchaseRatePerKg) { this.purchaseRatePerKg = purchaseRatePerKg; }
    public Double getTotalFeedCostInr() { return totalFeedCostInr != null ? totalFeedCostInr : 0.0; }
    public void setTotalFeedCostInr(Double totalFeedCostInr) { this.totalFeedCostInr = totalFeedCostInr; }
    public Double getRemainingStockKg() { return remainingStockKg; }
    public void setRemainingStockKg(Double remainingStockKg) { this.remainingStockKg = remainingStockKg; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
