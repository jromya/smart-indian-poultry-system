package com.poultry.smartfarm.inventory.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "vaccination_records")
public class VaccinationRecord {

    @Id
    private String id;

    @Indexed
    private String farmId;

    @Indexed
    private String batchId;
    private String batchCode;
    private String batchName;
    private String breedCode;
    private String breedName;

    private String vaccineName;
    private String targetDisease;
    private Integer recommendedAgeDays;

    @Indexed
    private LocalDate scheduledDate;
    private LocalDate administeredDate;

    // SCHEDULED, COMPLETED, OVERDUE, SKIPPED
    @Indexed
    private String status = "SCHEDULED";

    private String inventoryItemId;
    private Double dosesUsed = 0.0;
    private String route;
    private String dosagePerBird;
    private String vaccineBatchLot;
    private LocalDate expiryDate;
    private Double totalVaccineCostInr = 0.0;

    private String administeredBy;
    private String veterinaryGuidanceNotes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public VaccinationRecord() {
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
    public String getBreedCode() { return breedCode; }
    public void setBreedCode(String breedCode) { this.breedCode = breedCode; }
    public String getBreedName() { return breedName; }
    public void setBreedName(String breedName) { this.breedName = breedName; }
    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
    public String getTargetDisease() { return targetDisease; }
    public void setTargetDisease(String targetDisease) { this.targetDisease = targetDisease; }
    public Integer getRecommendedAgeDays() { return recommendedAgeDays; }
    public void setRecommendedAgeDays(Integer recommendedAgeDays) { this.recommendedAgeDays = recommendedAgeDays; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public LocalDate getAdministeredDate() { return administeredDate; }
    public void setAdministeredDate(LocalDate administeredDate) { this.administeredDate = administeredDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public Double getDosesUsed() { return dosesUsed != null ? dosesUsed : 0.0; }
    public void setDosesUsed(Double dosesUsed) { this.dosesUsed = dosesUsed; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getDosagePerBird() { return dosagePerBird; }
    public void setDosagePerBird(String dosagePerBird) { this.dosagePerBird = dosagePerBird; }
    public String getVaccineBatchLot() { return vaccineBatchLot; }
    public void setVaccineBatchLot(String vaccineBatchLot) { this.vaccineBatchLot = vaccineBatchLot; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Double getTotalVaccineCostInr() { return totalVaccineCostInr != null ? totalVaccineCostInr : 0.0; }
    public void setTotalVaccineCostInr(Double totalVaccineCostInr) { this.totalVaccineCostInr = totalVaccineCostInr; }
    public String getAdministeredBy() { return administeredBy; }
    public void setAdministeredBy(String administeredBy) { this.administeredBy = administeredBy; }
    public String getVeterinaryGuidanceNotes() { return veterinaryGuidanceNotes; }
    public void setVeterinaryGuidanceNotes(String veterinaryGuidanceNotes) { this.veterinaryGuidanceNotes = veterinaryGuidanceNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
