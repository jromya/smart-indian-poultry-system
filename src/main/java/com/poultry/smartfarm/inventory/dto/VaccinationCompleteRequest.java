package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class VaccinationCompleteRequest {

    @NotNull(message = "Administered date is required")
    private LocalDate administeredDate;

    // Optional: if provided, deducts dosesUsed from Vaccine InventoryItem automatically!
    private String inventoryItemId;

    @PositiveOrZero(message = "Doses used cannot be negative")
    private Double dosesUsed = 0.0;

    private String vaccineBatchLot;
    private LocalDate expiryDate;
    private String veterinaryGuidanceNotes;

    public LocalDate getAdministeredDate() { return administeredDate; }
    public void setAdministeredDate(LocalDate administeredDate) { this.administeredDate = administeredDate; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public Double getDosesUsed() { return dosesUsed != null ? dosesUsed : 0.0; }
    public void setDosesUsed(Double dosesUsed) { this.dosesUsed = dosesUsed; }
    public String getVaccineBatchLot() { return vaccineBatchLot; }
    public void setVaccineBatchLot(String vaccineBatchLot) { this.vaccineBatchLot = vaccineBatchLot; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getVeterinaryGuidanceNotes() { return veterinaryGuidanceNotes; }
    public void setVeterinaryGuidanceNotes(String veterinaryGuidanceNotes) { this.veterinaryGuidanceNotes = veterinaryGuidanceNotes; }
}
