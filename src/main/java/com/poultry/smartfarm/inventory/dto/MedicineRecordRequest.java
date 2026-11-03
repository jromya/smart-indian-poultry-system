package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class MedicineRecordRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "Medicine inventory item ID is required")
    private String inventoryItemId;

    @NotNull(message = "Administration date is required")
    private LocalDate administrationDate;

    @NotNull
    @Positive(message = "Quantity used must be greater than zero")
    private Double quantityUsed;

    @NotBlank(message = "Dosage record is required")
    private String dosageRecord;

    private String route = "DRINKING_WATER";

    @PositiveOrZero
    private Integer durationDays = 3;

    @PositiveOrZero
    private Integer withdrawalPeriodDays = 0;

    private String diseaseOrPurpose;
    private String veterinaryDoctorName;
    private String veterinaryRemarks;

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public LocalDate getAdministrationDate() { return administrationDate; }
    public void setAdministrationDate(LocalDate administrationDate) { this.administrationDate = administrationDate; }
    public Double getQuantityUsed() { return quantityUsed; }
    public void setQuantityUsed(Double quantityUsed) { this.quantityUsed = quantityUsed; }
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
    public String getVeterinaryDoctorName() { return veterinaryDoctorName; }
    public void setVeterinaryDoctorName(String veterinaryDoctorName) { this.veterinaryDoctorName = veterinaryDoctorName; }
    public String getVeterinaryRemarks() { return veterinaryRemarks; }
    public void setVeterinaryRemarks(String veterinaryRemarks) { this.veterinaryRemarks = veterinaryRemarks; }
}
