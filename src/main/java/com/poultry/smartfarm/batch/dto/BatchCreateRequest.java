package com.poultry.smartfarm.batch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class BatchCreateRequest {

    @NotBlank(message = "Unique Batch ID / Code is required")
    private String batchCode;

    @NotBlank(message = "Farm selection is required")
    private String farmId;

    @NotBlank(message = "Poultry house / shed selection is required")
    private String poultryHouseId;

    @NotBlank(message = "Batch Name is required")
    private String batchName;

    @NotBlank(message = "Breed selection is required")
    private String breedId;

    @NotBlank(message = "Category is required (BROILER, LAYER, INDIGENOUS)")
    private String batchCategory;

    private LocalDate purchaseDate;

    @NotNull(message = "Chick arrival date is required")
    private LocalDate chickArrivalDate;

    @NotNull(message = "Starting bird quantity is required")
    @Min(value = 1, message = "Starting bird quantity must be at least 1")
    private Integer startingBirdQuantity;

    @Min(value = 0, message = "Purchase price per chick cannot be negative")
    private Double purchasePricePerChick;

    private String supplierName;
    private Integer initialBirdAgeDays = 0;
    private Integer plannedDurationDays;
    private Double chickInitialWeightGrams;
    private Double estimatedFeedRatePerKg;
    private Double liveSellingPricePerKg;
    private String notes;

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getPoultryHouseId() { return poultryHouseId; }
    public void setPoultryHouseId(String poultryHouseId) { this.poultryHouseId = poultryHouseId; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public String getBreedId() { return breedId; }
    public void setBreedId(String breedId) { this.breedId = breedId; }
    public String getBatchCategory() { return batchCategory; }
    public void setBatchCategory(String batchCategory) { this.batchCategory = batchCategory; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getChickArrivalDate() { return chickArrivalDate; }
    public void setChickArrivalDate(LocalDate chickArrivalDate) { this.chickArrivalDate = chickArrivalDate; }
    public Integer getStartingBirdQuantity() { return startingBirdQuantity; }
    public void setStartingBirdQuantity(Integer startingBirdQuantity) { this.startingBirdQuantity = startingBirdQuantity; }
    public Double getPurchasePricePerChick() { return purchasePricePerChick; }
    public void setPurchasePricePerChick(Double purchasePricePerChick) { this.purchasePricePerChick = purchasePricePerChick; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Integer getInitialBirdAgeDays() { return initialBirdAgeDays; }
    public void setInitialBirdAgeDays(Integer initialBirdAgeDays) { this.initialBirdAgeDays = initialBirdAgeDays; }
    public Integer getPlannedDurationDays() { return plannedDurationDays; }
    public void setPlannedDurationDays(Integer plannedDurationDays) { this.plannedDurationDays = plannedDurationDays; }
    public Double getChickInitialWeightGrams() { return chickInitialWeightGrams; }
    public void setChickInitialWeightGrams(Double chickInitialWeightGrams) { this.chickInitialWeightGrams = chickInitialWeightGrams; }
    public Double getEstimatedFeedRatePerKg() { return estimatedFeedRatePerKg; }
    public void setEstimatedFeedRatePerKg(Double estimatedFeedRatePerKg) { this.estimatedFeedRatePerKg = estimatedFeedRatePerKg; }
    public Double getLiveSellingPricePerKg() { return liveSellingPricePerKg; }
    public void setLiveSellingPricePerKg(Double liveSellingPricePerKg) { this.liveSellingPricePerKg = liveSellingPricePerKg; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
