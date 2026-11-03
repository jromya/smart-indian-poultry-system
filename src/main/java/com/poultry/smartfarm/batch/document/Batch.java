package com.poultry.smartfarm.batch.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "batches")
public class Batch {

    @Id
    private String id;

    @Indexed(unique = true)
    private String batchCode;

    @Indexed
    private String farmId;
    private String farmName;

    @Indexed
    private String poultryHouseId;
    private String poultryHouseName;

    private String batchName;

    private String breedId;
    private String breedCode;
    private String breedName;

    // BROILER, LAYER, INDIGENOUS
    @Indexed
    private String batchCategory;

    private LocalDate purchaseDate;
    private LocalDate chickArrivalDate;

    private Integer startingBirdQuantity;
    private Double purchasePricePerChick;
    private String supplierName;

    private Integer initialBirdAgeDays = 0;
    private Integer plannedDurationDays;
    private LocalDate actualCompletionDate;

    // ACTIVE, COMPLETED, REOPENED
    @Indexed
    private String status = "ACTIVE";

    // Cumulative counters kept strictly synchronized
    private Integer totalAdditions = 0;
    private Integer totalMortality = 0;
    private Integer totalCulls = 0;
    private Integer totalTransfersOut = 0;
    private Integer totalBirdsSold = 0;
    private Double totalSoldLiveWeightKg = 0.0;
    private Double totalBirdSalesRevenue = 0.0;

    // Broiler / Meat fields
    private Double chickInitialWeightGrams = 42.0;
    private Double latestAverageWeightGrams = 42.0;
    private Double latestUniformityPct = 85.0;
    private Double estimatedFeedRatePerKg = 42.0;
    private Double liveSellingPricePerKg = 125.0;

    // Layer egg stock counters
    private Integer totalGoodEggsProduced = 0;
    private Integer totalEggsSold = 0;
    private Double totalEggSalesRevenue = 0.0;

    private String reopenReason;
    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Batch() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getPoultryHouseId() { return poultryHouseId; }
    public void setPoultryHouseId(String poultryHouseId) { this.poultryHouseId = poultryHouseId; }
    public String getPoultryHouseName() { return poultryHouseName; }
    public void setPoultryHouseName(String poultryHouseName) { this.poultryHouseName = poultryHouseName; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public String getBreedId() { return breedId; }
    public void setBreedId(String breedId) { this.breedId = breedId; }
    public String getBreedCode() { return breedCode; }
    public void setBreedCode(String breedCode) { this.breedCode = breedCode; }
    public String getBreedName() { return breedName; }
    public void setBreedName(String breedName) { this.breedName = breedName; }
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
    public LocalDate getActualCompletionDate() { return actualCompletionDate; }
    public void setActualCompletionDate(LocalDate actualCompletionDate) { this.actualCompletionDate = actualCompletionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTotalAdditions() { return totalAdditions != null ? totalAdditions : 0; }
    public void setTotalAdditions(Integer totalAdditions) { this.totalAdditions = totalAdditions; }
    public Integer getTotalMortality() { return totalMortality != null ? totalMortality : 0; }
    public void setTotalMortality(Integer totalMortality) { this.totalMortality = totalMortality; }
    public Integer getTotalCulls() { return totalCulls != null ? totalCulls : 0; }
    public void setTotalCulls(Integer totalCulls) { this.totalCulls = totalCulls; }
    public Integer getTotalTransfersOut() { return totalTransfersOut != null ? totalTransfersOut : 0; }
    public void setTotalTransfersOut(Integer totalTransfersOut) { this.totalTransfersOut = totalTransfersOut; }
    public Integer getTotalBirdsSold() { return totalBirdsSold != null ? totalBirdsSold : 0; }
    public void setTotalBirdsSold(Integer totalBirdsSold) { this.totalBirdsSold = totalBirdsSold; }
    public Double getTotalSoldLiveWeightKg() { return totalSoldLiveWeightKg != null ? totalSoldLiveWeightKg : 0.0; }
    public void setTotalSoldLiveWeightKg(Double totalSoldLiveWeightKg) { this.totalSoldLiveWeightKg = totalSoldLiveWeightKg; }
    public Double getTotalBirdSalesRevenue() { return totalBirdSalesRevenue != null ? totalBirdSalesRevenue : 0.0; }
    public void setTotalBirdSalesRevenue(Double totalBirdSalesRevenue) { this.totalBirdSalesRevenue = totalBirdSalesRevenue; }
    public Double getChickInitialWeightGrams() { return chickInitialWeightGrams != null ? chickInitialWeightGrams : 40.0; }
    public void setChickInitialWeightGrams(Double chickInitialWeightGrams) { this.chickInitialWeightGrams = chickInitialWeightGrams; }
    public Double getLatestAverageWeightGrams() { return latestAverageWeightGrams != null ? latestAverageWeightGrams : getChickInitialWeightGrams(); }
    public void setLatestAverageWeightGrams(Double latestAverageWeightGrams) { this.latestAverageWeightGrams = latestAverageWeightGrams; }
    public Double getLatestUniformityPct() { return latestUniformityPct; }
    public void setLatestUniformityPct(Double latestUniformityPct) { this.latestUniformityPct = latestUniformityPct; }
    public Double getEstimatedFeedRatePerKg() { return estimatedFeedRatePerKg != null ? estimatedFeedRatePerKg : 42.0; }
    public void setEstimatedFeedRatePerKg(Double estimatedFeedRatePerKg) { this.estimatedFeedRatePerKg = estimatedFeedRatePerKg; }
    public Double getLiveSellingPricePerKg() { return liveSellingPricePerKg != null ? liveSellingPricePerKg : 120.0; }
    public void setLiveSellingPricePerKg(Double liveSellingPricePerKg) { this.liveSellingPricePerKg = liveSellingPricePerKg; }
    public Integer getTotalGoodEggsProduced() { return totalGoodEggsProduced != null ? totalGoodEggsProduced : 0; }
    public void setTotalGoodEggsProduced(Integer totalGoodEggsProduced) { this.totalGoodEggsProduced = totalGoodEggsProduced; }
    public Integer getTotalEggsSold() { return totalEggsSold != null ? totalEggsSold : 0; }
    public void setTotalEggsSold(Integer totalEggsSold) { this.totalEggsSold = totalEggsSold; }
    public Double getTotalEggSalesRevenue() { return totalEggSalesRevenue != null ? totalEggSalesRevenue : 0.0; }
    public void setTotalEggSalesRevenue(Double totalEggSalesRevenue) { this.totalEggSalesRevenue = totalEggSalesRevenue; }
    public String getReopenReason() { return reopenReason; }
    public void setReopenReason(String reopenReason) { this.reopenReason = reopenReason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
