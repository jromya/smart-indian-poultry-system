package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StockTransferRequest {

    @NotBlank(message = "Source inventory item ID is required")
    private String sourceInventoryItemId;

    @NotBlank(message = "Destination farm ID is required")
    private String destinationFarmId;

    @NotNull
    @Positive(message = "Transfer quantity must be greater than zero")
    private Double quantity;

    private String destinationStorageLocation;
    private String remarks;

    public String getSourceInventoryItemId() { return sourceInventoryItemId; }
    public void setSourceInventoryItemId(String sourceInventoryItemId) { this.sourceInventoryItemId = sourceInventoryItemId; }
    public String getDestinationFarmId() { return destinationFarmId; }
    public void setDestinationFarmId(String destinationFarmId) { this.destinationFarmId = destinationFarmId; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getDestinationStorageLocation() { return destinationStorageLocation; }
    public void setDestinationStorageLocation(String destinationStorageLocation) { this.destinationStorageLocation = destinationStorageLocation; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
