package com.poultry.smartfarm.inventory.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "inventory_items")
@CompoundIndex(name = "farm_itemcode_unique_idx", def = "{'farmId': 1, 'itemCode': 1}", unique = true)
public class InventoryItem {

    @Id
    private String id;

    @Indexed
    private String farmId;
    private String farmName;

    private String itemCode;
    private String itemName;

    // FEED, MEDICINE, VACCINE, EQUIPMENT, PACKAGING, OTHER
    @Indexed
    private String category;

    // e.g., STARTER, GROWER, FINISHER, LAYER_MASH, DESI_GRAIN_MIX, ANTIBIOTIC, VITAMIN, LIVE_VACCINE, DRINKER, EGG_TRAY
    private String subCategory;

    // KG, LITERS, ML, DOSES, VIALS, BOTTLES, PIECES, TRAYS, BAGS
    private String unit = "KG";

    private Double currentStock = 0.0;
    private Double reorderLevel = 50.0;
    private Double unitCostInr = 0.0;
    private Double totalValuationInr = 0.0;

    private String batchLotNumber;
    private LocalDate expiryDate;

    private String preferredSupplierId;
    private String preferredSupplierName;
    private String storageLocation;

    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public InventoryItem() {
    }

    public void recalculateValuation() {
        double stock = currentStock != null ? currentStock : 0.0;
        double rate = unitCostInr != null ? unitCostInr : 0.0;
        this.totalValuationInr = Math.round(stock * rate * 100.0) / 100.0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
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
    public Double getCurrentStock() { return currentStock != null ? currentStock : 0.0; }
    public void setCurrentStock(Double currentStock) { this.currentStock = currentStock; }
    public Double getReorderLevel() { return reorderLevel != null ? reorderLevel : 0.0; }
    public void setReorderLevel(Double reorderLevel) { this.reorderLevel = reorderLevel; }
    public Double getUnitCostInr() { return unitCostInr != null ? unitCostInr : 0.0; }
    public void setUnitCostInr(Double unitCostInr) { this.unitCostInr = unitCostInr; }
    public Double getTotalValuationInr() { return totalValuationInr != null ? totalValuationInr : 0.0; }
    public void setTotalValuationInr(Double totalValuationInr) { this.totalValuationInr = totalValuationInr; }
    public String getBatchLotNumber() { return batchLotNumber; }
    public void setBatchLotNumber(String batchLotNumber) { this.batchLotNumber = batchLotNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getPreferredSupplierId() { return preferredSupplierId; }
    public void setPreferredSupplierId(String preferredSupplierId) { this.preferredSupplierId = preferredSupplierId; }
    public String getPreferredSupplierName() { return preferredSupplierName; }
    public void setPreferredSupplierName(String preferredSupplierName) { this.preferredSupplierName = preferredSupplierName; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
