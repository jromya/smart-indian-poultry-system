package com.poultry.smartfarm.batch.dto;

import com.poultry.smartfarm.batch.document.Batch;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BatchResponse {

    private String id;
    private String batchCode;
    private String farmId;
    private String farmName;
    private String poultryHouseId;
    private String poultryHouseName;
    private String batchName;
    private String breedId;
    private String breedCode;
    private String breedName;
    private String batchCategory;

    private LocalDate purchaseDate;
    private LocalDate chickArrivalDate;
    private Integer startingBirdQuantity;
    private Double purchasePricePerChick;
    private String supplierName;
    private Integer initialBirdAgeDays;
    private Integer plannedDurationDays;
    private LocalDate expectedCompletionDate;
    private LocalDate actualCompletionDate;
    private String status;

    // Auto-calculated live metrics
    private int currentAgeDays;
    private double currentAgeWeeks;
    private int totalBirdsPurchased;
    private int totalMortality;
    private int totalCulls;
    private int totalTransfersOut;
    private int totalBirdsSold;
    private int currentLiveBirds;
    private double mortalityPercentage;
    private double livabilityPercentage;

    private Double latestAverageWeightGrams;
    private Double latestUniformityPct;
    private Double liveSellingPricePerKg;
    private Double estimatedFeedRatePerKg;
    private int currentEggStockGood;
    private String notes;

    public static BatchResponse fromDocument(Batch b) {
        BatchResponse dto = new BatchResponse();
        dto.setId(b.getId());
        dto.setBatchCode(b.getBatchCode());
        dto.setFarmId(b.getFarmId());
        dto.setFarmName(b.getFarmName());
        dto.setPoultryHouseId(b.getPoultryHouseId());
        dto.setPoultryHouseName(b.getPoultryHouseName());
        dto.setBatchName(b.getBatchName());
        dto.setBreedId(b.getBreedId());
        dto.setBreedCode(b.getBreedCode());
        dto.setBreedName(b.getBreedName());
        dto.setBatchCategory(b.getBatchCategory());
        dto.setPurchaseDate(b.getPurchaseDate());
        dto.setChickArrivalDate(b.getChickArrivalDate());
        dto.setStartingBirdQuantity(b.getStartingBirdQuantity());
        dto.setPurchasePricePerChick(b.getPurchasePricePerChick());
        dto.setSupplierName(b.getSupplierName());

        int initAge = b.getInitialBirdAgeDays() != null ? b.getInitialBirdAgeDays() : 0;
        dto.setInitialBirdAgeDays(initAge);
        dto.setPlannedDurationDays(b.getPlannedDurationDays());

        if (b.getChickArrivalDate() != null && b.getPlannedDurationDays() != null) {
            dto.setExpectedCompletionDate(b.getChickArrivalDate().plusDays(b.getPlannedDurationDays()));
        }
        dto.setActualCompletionDate(b.getActualCompletionDate());
        dto.setStatus(b.getStatus());

        LocalDate refDate = ("COMPLETED".equalsIgnoreCase(b.getStatus()) && b.getActualCompletionDate() != null)
                ? b.getActualCompletionDate()
                : LocalDate.now();
        int daysOnFarm = b.getChickArrivalDate() != null
                ? Math.max(0, (int) ChronoUnit.DAYS.between(b.getChickArrivalDate(), refDate))
                : 0;
        int ageDays = initAge + daysOnFarm;
        dto.setCurrentAgeDays(ageDays);
        dto.setCurrentAgeWeeks(Math.round((ageDays / 7.0) * 10.0) / 10.0);

        int startQty = b.getStartingBirdQuantity() != null ? b.getStartingBirdQuantity() : 0;
        int additions = b.getTotalAdditions();
        int totalPurchased = startQty + additions;
        int mort = b.getTotalMortality();
        int culls = b.getTotalCulls();
        int transfers = b.getTotalTransfersOut();
        int sold = b.getTotalBirdsSold();
        int live = Math.max(0, totalPurchased - mort - culls - transfers - sold);

        dto.setTotalBirdsPurchased(totalPurchased);
        dto.setTotalMortality(mort);
        dto.setTotalCulls(culls);
        dto.setTotalTransfersOut(transfers);
        dto.setTotalBirdsSold(sold);
        dto.setCurrentLiveBirds(live);

        double mortPct = totalPurchased > 0 ? Math.round(((mort * 100.0) / totalPurchased) * 100.0) / 100.0 : 0.0;
        dto.setMortalityPercentage(mortPct);
        dto.setLivabilityPercentage(Math.max(0.0, Math.round((100.0 - mortPct) * 100.0) / 100.0));

        dto.setLatestAverageWeightGrams(b.getLatestAverageWeightGrams());
        dto.setLatestUniformityPct(b.getLatestUniformityPct());
        dto.setLiveSellingPricePerKg(b.getLiveSellingPricePerKg());
        dto.setEstimatedFeedRatePerKg(b.getEstimatedFeedRatePerKg());
        dto.setCurrentEggStockGood(Math.max(0, b.getTotalGoodEggsProduced() - b.getTotalEggsSold()));
        dto.setNotes(b.getNotes());
        return dto;
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
    public LocalDate getExpectedCompletionDate() { return expectedCompletionDate; }
    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) { this.expectedCompletionDate = expectedCompletionDate; }
    public LocalDate getActualCompletionDate() { return actualCompletionDate; }
    public void setActualCompletionDate(LocalDate actualCompletionDate) { this.actualCompletionDate = actualCompletionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCurrentAgeDays() { return currentAgeDays; }
    public void setCurrentAgeDays(int currentAgeDays) { this.currentAgeDays = currentAgeDays; }
    public double getCurrentAgeWeeks() { return currentAgeWeeks; }
    public void setCurrentAgeWeeks(double currentAgeWeeks) { this.currentAgeWeeks = currentAgeWeeks; }
    public int getTotalBirdsPurchased() { return totalBirdsPurchased; }
    public void setTotalBirdsPurchased(int totalBirdsPurchased) { this.totalBirdsPurchased = totalBirdsPurchased; }
    public int getTotalMortality() { return totalMortality; }
    public void setTotalMortality(int totalMortality) { this.totalMortality = totalMortality; }
    public int getTotalCulls() { return totalCulls; }
    public void setTotalCulls(int totalCulls) { this.totalCulls = totalCulls; }
    public int getTotalTransfersOut() { return totalTransfersOut; }
    public void setTotalTransfersOut(int totalTransfersOut) { this.totalTransfersOut = totalTransfersOut; }
    public int getTotalBirdsSold() { return totalBirdsSold; }
    public void setTotalBirdsSold(int totalBirdsSold) { this.totalBirdsSold = totalBirdsSold; }
    public int getCurrentLiveBirds() { return currentLiveBirds; }
    public void setCurrentLiveBirds(int currentLiveBirds) { this.currentLiveBirds = currentLiveBirds; }
    public double getMortalityPercentage() { return mortalityPercentage; }
    public void setMortalityPercentage(double mortalityPercentage) { this.mortalityPercentage = mortalityPercentage; }
    public double getLivabilityPercentage() { return livabilityPercentage; }
    public void setLivabilityPercentage(double livabilityPercentage) { this.livabilityPercentage = livabilityPercentage; }
    public Double getLatestAverageWeightGrams() { return latestAverageWeightGrams; }
    public void setLatestAverageWeightGrams(Double latestAverageWeightGrams) { this.latestAverageWeightGrams = latestAverageWeightGrams; }
    public Double getLatestUniformityPct() { return latestUniformityPct; }
    public void setLatestUniformityPct(Double latestUniformityPct) { this.latestUniformityPct = latestUniformityPct; }
    public Double getLiveSellingPricePerKg() { return liveSellingPricePerKg; }
    public void setLiveSellingPricePerKg(Double liveSellingPricePerKg) { this.liveSellingPricePerKg = liveSellingPricePerKg; }
    public Double getEstimatedFeedRatePerKg() { return estimatedFeedRatePerKg; }
    public void setEstimatedFeedRatePerKg(Double estimatedFeedRatePerKg) { this.estimatedFeedRatePerKg = estimatedFeedRatePerKg; }
    public int getCurrentEggStockGood() { return currentEggStockGood; }
    public void setCurrentEggStockGood(int currentEggStockGood) { this.currentEggStockGood = currentEggStockGood; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
