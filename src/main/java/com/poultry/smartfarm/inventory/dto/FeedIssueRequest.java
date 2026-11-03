package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class FeedIssueRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "Feed inventory item ID is required")
    private String inventoryItemId;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    @NotNull
    @Positive(message = "Feed quantity (kg) must be greater than zero")
    private Double quantityIssuedKg;

    private String feedCategory;
    private boolean syncDailyRecordFeed = true;
    private String remarks;

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Double getQuantityIssuedKg() { return quantityIssuedKg; }
    public void setQuantityIssuedKg(Double quantityIssuedKg) { this.quantityIssuedKg = quantityIssuedKg; }
    public String getFeedCategory() { return feedCategory; }
    public void setFeedCategory(String feedCategory) { this.feedCategory = feedCategory; }
    public boolean isSyncDailyRecordFeed() { return syncDailyRecordFeed; }
    public void setSyncDailyRecordFeed(boolean syncDailyRecordFeed) { this.syncDailyRecordFeed = syncDailyRecordFeed; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
