package com.poultry.smartfarm.inventory.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "medicine_records")
public class MedicineRecord {

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
    private String medicineName;
    private String medicineBatchLot;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;

    @Indexed
    private LocalDate administrationDate;

    private Double quantityUsed;
    private String unit;
    private String dosageRecord;
    // DRINKING_WATER, FEED_MIX, INJECTION, EYE_DROP, SPRAY
    private String route = "DRINKING_WATER";
    private Integer durationDays = 3;
    private Integer withdrawalPeriodDays = 0;

    private String diseaseOrPurpose;
    private Double unitCostInr = 0.0;
    private Double totalMedicineCostInr = 0.0;
    private Double remainingStockAfter;

    private String veterinaryDoctorName;
    private String veterinaryRemarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public MedicineRecord() {
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
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getMedicineBatchLot() { return medicineBatchLot; }
    public void setMedicineBatchLot(String medicineBatchLot) { this.medicineBatchLot = medicineBatchLot; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public LocalDate getAdministrationDate() { return administrationDate; }
    public void setAdministrationDate(LocalDate administrationDate) { this.administrationDate = administrationDate; }
    public Double getQuantityUsed() { return quantityUsed != null ? quantityUsed : 0.0; }
    public void setQuantityUsed(Double quantityUsed) { this.quantityUsed = quantityUsed; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getDosageRecord() { return dosageRecord; }
    public void setDosageRecord(String dosageRecord) { this.dosageRecord = dosageRecord; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getWithdrawalPeriodDays() { return withdrawalPeriodDays; }
    public void setWithdrawalPeriodDays(Integer withdrawalPeriodDays) { this.withdrawalPeriodDays = withdrawalPeriodDays; }
    public String getDiseaseOrPurpose() { return diseaseOrPurpose; }
    public void setDiseaseOrPurpose(String diseaseOrPurpose) { this.diseaseOrPurpose = diseaseOrPurpose; }
    public Double getUnitCostInr() { return unitCostInr != null ? unitCostInr : 0.0; }
    public void setUnitCostInr(Double unitCostInr) { this.unitCostInr = unitCostInr; }
    public Double getTotalMedicineCostInr() { return totalMedicineCostInr != null ? totalMedicineCostInr : 0.0; }
    public void setTotalMedicineCostInr(Double totalMedicineCostInr) { this.totalMedicineCostInr = totalMedicineCostInr; }
    public Double getRemainingStockAfter() { return remainingStockAfter; }
    public void setRemainingStockAfter(Double remainingStockAfter) { this.remainingStockAfter = remainingStockAfter; }
    public String getVeterinaryDoctorName() { return veterinaryDoctorName; }
    public void setVeterinaryDoctorName(String veterinaryDoctorName) { this.veterinaryDoctorName = veterinaryDoctorName; }
    public String getVeterinaryRemarks() { return veterinaryRemarks; }
    public void setVeterinaryRemarks(String veterinaryRemarks) { this.veterinaryRemarks = veterinaryRemarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
