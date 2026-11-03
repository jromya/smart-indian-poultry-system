package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StockAdjustmentRequest {

    @NotBlank(message = "Inventory item ID is required")
    private String inventoryItemId;

    // ADD or DEDUCT
    @NotBlank(message = "Adjustment type (ADD or DEDUCT) is required")
    private String adjustmentType;

    @NotNull
    @Positive(message = "Adjustment quantity must be greater than zero")
    private Double quantity;

    @NotBlank(message = "Reason for adjustment is required for audit trail")
    private String reason;

    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
