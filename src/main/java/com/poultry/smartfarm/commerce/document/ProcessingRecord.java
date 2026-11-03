package com.poultry.smartfarm.commerce.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "processing_records")
public class ProcessingRecord {

    @Id
    private String id;

    @Indexed
    private String shopId;
    private String shopName;

    @Indexed
    private String ownerUserId;

    @Indexed
    private LocalDate recordDate;

    private String supplierOrFarmName;
    private String birdType = "Commercial Broiler";

    // Live purchase details
    private Integer liveBirdsPurchased = 0;
    private Integer birdsProcessedCount = 0;
    private Double liveWeightKg = 0.0;
    private Double livePurchaseRatePerKg = 0.0;

    // Exact Formula: Live purchase cost = Live weight × Purchase rate
    private Double livePurchaseCostInr = 0.0;

    private Double processingExpensesInr = 0.0;
    private Double otherAllocatedExpensesInr = 0.0;

    // NEVER assumed fixed yield — actual recorded dressed meat weight!
    private Double actualDressedMeatWeightKg = 0.0;
    private Double observedDressingPct = 0.0;

    private Double meatSoldImmediatelyKg = 0.0;
    private Double remainingMeatAddedToStockKg = 0.0;
    private Double sellingPricePerKg = 0.0;
    private Double meatSalesRevenueInr = 0.0;

    private Double wasteAndFeathersKg = 0.0;
    private Double byProductSalesInr = 0.0;

    // Total revenue = Meat sales + By-product sales
    private Double totalRevenueInr = 0.0;
    private Double totalCostInr = 0.0;
    // Net profit = Total revenue - Purchase cost - Processing expenses - Other allocated expenses
    private Double netProfitLossInr = 0.0;

    private String remarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public ProcessingRecord() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public String getSupplierOrFarmName() { return supplierOrFarmName; }
    public void setSupplierOrFarmName(String supplierOrFarmName) { this.supplierOrFarmName = supplierOrFarmName; }
    public String getBirdType() { return birdType; }
    public void setBirdType(String birdType) { this.birdType = birdType; }
    public Integer getLiveBirdsPurchased() { return liveBirdsPurchased != null ? liveBirdsPurchased : 0; }
    public void setLiveBirdsPurchased(Integer liveBirdsPurchased) { this.liveBirdsPurchased = liveBirdsPurchased; }
    public Integer getBirdsProcessedCount() { return birdsProcessedCount != null ? birdsProcessedCount : 0; }
    public void setBirdsProcessedCount(Integer birdsProcessedCount) { this.birdsProcessedCount = birdsProcessedCount; }
    public Double getLiveWeightKg() { return liveWeightKg != null ? liveWeightKg : 0.0; }
    public void setLiveWeightKg(Double liveWeightKg) { this.liveWeightKg = liveWeightKg; }
    public Double getLivePurchaseRatePerKg() { return livePurchaseRatePerKg != null ? livePurchaseRatePerKg : 0.0; }
    public void setLivePurchaseRatePerKg(Double livePurchaseRatePerKg) { this.livePurchaseRatePerKg = livePurchaseRatePerKg; }
    public Double getLivePurchaseCostInr() { return livePurchaseCostInr != null ? livePurchaseCostInr : 0.0; }
    public void setLivePurchaseCostInr(Double livePurchaseCostInr) { this.livePurchaseCostInr = livePurchaseCostInr; }
    public Double getProcessingExpensesInr() { return processingExpensesInr != null ? processingExpensesInr : 0.0; }
    public void setProcessingExpensesInr(Double processingExpensesInr) { this.processingExpensesInr = processingExpensesInr; }
    public Double getOtherAllocatedExpensesInr() { return otherAllocatedExpensesInr != null ? otherAllocatedExpensesInr : 0.0; }
    public void setOtherAllocatedExpensesInr(Double otherAllocatedExpensesInr) { this.otherAllocatedExpensesInr = otherAllocatedExpensesInr; }
    public Double getActualDressedMeatWeightKg() { return actualDressedMeatWeightKg != null ? actualDressedMeatWeightKg : 0.0; }
    public void setActualDressedMeatWeightKg(Double actualDressedMeatWeightKg) { this.actualDressedMeatWeightKg = actualDressedMeatWeightKg; }
    public Double getObservedDressingPct() { return observedDressingPct != null ? observedDressingPct : 0.0; }
    public void setObservedDressingPct(Double observedDressingPct) { this.observedDressingPct = observedDressingPct; }
    public Double getMeatSoldImmediatelyKg() { return meatSoldImmediatelyKg != null ? meatSoldImmediatelyKg : 0.0; }
    public void setMeatSoldImmediatelyKg(Double meatSoldImmediatelyKg) { this.meatSoldImmediatelyKg = meatSoldImmediatelyKg; }
    public Double getRemainingMeatAddedToStockKg() { return remainingMeatAddedToStockKg != null ? remainingMeatAddedToStockKg : 0.0; }
    public void setRemainingMeatAddedToStockKg(Double remainingMeatAddedToStockKg) { this.remainingMeatAddedToStockKg = remainingMeatAddedToStockKg; }
    public Double getSellingPricePerKg() { return sellingPricePerKg != null ? sellingPricePerKg : 0.0; }
    public void setSellingPricePerKg(Double sellingPricePerKg) { this.sellingPricePerKg = sellingPricePerKg; }
    public Double getMeatSalesRevenueInr() { return meatSalesRevenueInr != null ? meatSalesRevenueInr : 0.0; }
    public void setMeatSalesRevenueInr(Double meatSalesRevenueInr) { this.meatSalesRevenueInr = meatSalesRevenueInr; }
    public Double getWasteAndFeathersKg() { return wasteAndFeathersKg != null ? wasteAndFeathersKg : 0.0; }
    public void setWasteAndFeathersKg(Double wasteAndFeathersKg) { this.wasteAndFeathersKg = wasteAndFeathersKg; }
    public Double getByProductSalesInr() { return byProductSalesInr != null ? byProductSalesInr : 0.0; }
    public void setByProductSalesInr(Double byProductSalesInr) { this.byProductSalesInr = byProductSalesInr; }
    public Double getTotalRevenueInr() { return totalRevenueInr != null ? totalRevenueInr : 0.0; }
    public void setTotalRevenueInr(Double totalRevenueInr) { this.totalRevenueInr = totalRevenueInr; }
    public Double getTotalCostInr() { return totalCostInr != null ? totalCostInr : 0.0; }
    public void setTotalCostInr(Double totalCostInr) { this.totalCostInr = totalCostInr; }
    public Double getNetProfitLossInr() { return netProfitLossInr != null ? netProfitLossInr : 0.0; }
    public void setNetProfitLossInr(Double netProfitLossInr) { this.netProfitLossInr = netProfitLossInr; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
