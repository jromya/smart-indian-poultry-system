package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class InventoryItemRequest {

    @NotBlank(message = "Farm ID is required")
    private String farmId;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotBlank(message = "Category is required")
    private String category;

    private String subCategory;

    @NotBlank(message = "Unit is required")
    private String unit = "KG";

    @NotNull
    @PositiveOrZero(message = "Opening stock cannot be negative")
    private Double openingStock = 0.0;

    @NotNull
    @PositiveOrZero(message = "Reorder alert level cannot be negative")
    private Double reorderLevel = 50.0;

    @NotNull
    @PositiveOrZero(message = "Unit cost cannot be negative")
    private Double unitCostInr = 0.0;

    private String batchLotNumber;
    private LocalDate expiryDate;
    private String preferredSupplierId;
    private String storageLocation;

    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getOpeningStock() { return openingStock; }
    public void setOpeningStock(Double openingStock) { this.openingStock = openingStock; }
    public Double getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Double reorderLevel) { this.reorderLevel = reorderLevel; }
    public Double getUnitCostInr() { return unitCostInr; }
    public void setUnitCostInr(Double unitCostInr) { this.unitCostInr = unitCostInr; }
    public String getBatchLotNumber() { return batchLotNumber; }
    public void setBatchLotNumber(String batchLotNumber) { this.batchLotNumber = batchLotNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getPreferredSupplierId() { return preferredSupplierId; }
    public void setPreferredSupplierId(String preferredSupplierId) { this.preferredSupplierId = preferredSupplierId; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
}
