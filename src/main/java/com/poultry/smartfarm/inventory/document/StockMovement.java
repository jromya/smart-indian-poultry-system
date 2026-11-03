package com.poultry.smartfarm.inventory.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "stock_movements")
public class StockMovement {

    @Id
    private String id;

    @Indexed
    private String farmId;
    private String farmName;

    @Indexed
    private String inventoryItemId;
    private String itemCode;
    private String itemName;
    private String category;

    // PURCHASE_IN, BATCH_ISSUE, ADJUSTMENT_ADD, ADJUSTMENT_DEDUCT, TRANSFER_OUT, TRANSFER_IN, OPENING_STOCK
    @Indexed
    private String movementType;

    private Double quantity;
    private String unit;
    private Double stockBefore;
    private Double stockAfter;
    private Double unitCostInr;
    private Double totalMovementValueInr;

    private String referenceType;
    private String referenceId;
    private String batchId;
    private String batchCode;
    private String counterpartyFarmId;
    private String counterpartyFarmName;
    private String remarks;
    private String performedBy;

    @Indexed
    private LocalDateTime movementAt = LocalDateTime.now();

    public StockMovement() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getStockBefore() { return stockBefore; }
    public void setStockBefore(Double stockBefore) { this.stockBefore = stockBefore; }
    public Double getStockAfter() { return stockAfter; }
    public void setStockAfter(Double stockAfter) { this.stockAfter = stockAfter; }
    public Double getUnitCostInr() { return unitCostInr; }
    public void setUnitCostInr(Double unitCostInr) { this.unitCostInr = unitCostInr; }
    public Double getTotalMovementValueInr() { return totalMovementValueInr; }
    public void setTotalMovementValueInr(Double totalMovementValueInr) { this.totalMovementValueInr = totalMovementValueInr; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getCounterpartyFarmId() { return counterpartyFarmId; }
    public void setCounterpartyFarmId(String counterpartyFarmId) { this.counterpartyFarmId = counterpartyFarmId; }
    public String getCounterpartyFarmName() { return counterpartyFarmName; }
    public void setCounterpartyFarmName(String counterpartyFarmName) { this.counterpartyFarmName = counterpartyFarmName; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getMovementAt() { return movementAt; }
    public void setMovementAt(LocalDateTime movementAt) { this.movementAt = movementAt; }
}
