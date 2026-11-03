package com.poultry.smartfarm.commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class ChickenShopProcessRequest {

    private String shopId;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    private String supplierOrFarmName;
    private String birdType = "Commercial Broiler";

    @PositiveOrZero(message = "Live birds purchased cannot be negative")
    private Integer liveBirdsPurchased = 0;

    @NotNull
    @Positive(message = "Number of birds processed must be greater than zero")
    private Integer birdsProcessedCount;

    @NotNull
    @Positive(message = "Live weight (kg) must be greater than zero")
    private Double liveWeightKg;

    @NotNull
    @Positive(message = "Live purchase rate per kg must be greater than zero")
    private Double livePurchaseRatePerKg;

    @PositiveOrZero
    private Double processingExpensesInr = 0.0;

    @PositiveOrZero
    private Double otherAllocatedExpensesInr = 0.0;

    // Actual Dressed Meat Weight (NEVER assumed fixed yield!)
    @NotNull
    @Positive(message = "Actual dressed meat weight (kg) must be recorded")
    private Double actualDressedMeatWeightKg;

    @PositiveOrZero
    private Double meatSoldImmediatelyKg = 0.0;

    @PositiveOrZero
    private Double sellingPricePerKg = 220.0;

    @PositiveOrZero
    private Double wasteAndFeathersKg = 0.0;

    @PositiveOrZero
    private Double byProductSalesInr = 0.0;

    private String remarks;

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public String getSupplierOrFarmName() { return supplierOrFarmName; }
    public void setSupplierOrFarmName(String supplierOrFarmName) { this.supplierOrFarmName = supplierOrFarmName; }
    public String getBirdType() { return birdType; }
    public void setBirdType(String birdType) { this.birdType = birdType; }
    public Integer getLiveBirdsPurchased() { return liveBirdsPurchased != null ? liveBirdsPurchased : 0; }
    public void setLiveBirdsPurchased(Integer liveBirdsPurchased) { this.liveBirdsPurchased = liveBirdsPurchased; }
    public Integer getBirdsProcessedCount() { return birdsProcessedCount; }
    public void setBirdsProcessedCount(Integer birdsProcessedCount) { this.birdsProcessedCount = birdsProcessedCount; }
    public Double getLiveWeightKg() { return liveWeightKg; }
    public void setLiveWeightKg(Double liveWeightKg) { this.liveWeightKg = liveWeightKg; }
    public Double getLivePurchaseRatePerKg() { return livePurchaseRatePerKg; }
    public void setLivePurchaseRatePerKg(Double livePurchaseRatePerKg) { this.livePurchaseRatePerKg = livePurchaseRatePerKg; }
    public Double getProcessingExpensesInr() { return processingExpensesInr != null ? processingExpensesInr : 0.0; }
    public void setProcessingExpensesInr(Double processingExpensesInr) { this.processingExpensesInr = processingExpensesInr; }
    public Double getOtherAllocatedExpensesInr() { return otherAllocatedExpensesInr != null ? otherAllocatedExpensesInr : 0.0; }
    public void setOtherAllocatedExpensesInr(Double otherAllocatedExpensesInr) { this.otherAllocatedExpensesInr = otherAllocatedExpensesInr; }
    public Double getActualDressedMeatWeightKg() { return actualDressedMeatWeightKg; }
    public void setActualDressedMeatWeightKg(Double actualDressedMeatWeightKg) { this.actualDressedMeatWeightKg = actualDressedMeatWeightKg; }
    public Double getMeatSoldImmediatelyKg() { return meatSoldImmediatelyKg != null ? meatSoldImmediatelyKg : 0.0; }
    public void setMeatSoldImmediatelyKg(Double meatSoldImmediatelyKg) { this.meatSoldImmediatelyKg = meatSoldImmediatelyKg; }
    public Double getSellingPricePerKg() { return sellingPricePerKg != null ? sellingPricePerKg : 220.0; }
    public void setSellingPricePerKg(Double sellingPricePerKg) { this.sellingPricePerKg = sellingPricePerKg; }
    public Double getWasteAndFeathersKg() { return wasteAndFeathersKg != null ? wasteAndFeathersKg : 0.0; }
    public void setWasteAndFeathersKg(Double wasteAndFeathersKg) { this.wasteAndFeathersKg = wasteAndFeathersKg; }
    public Double getByProductSalesInr() { return byProductSalesInr != null ? byProductSalesInr : 0.0; }
    public void setByProductSalesInr(Double byProductSalesInr) { this.byProductSalesInr = byProductSalesInr; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
