package com.poultry.smartfarm.batch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BirdHarvestSaleRequest {

    @NotNull(message = "Number of birds sold is required")
    @Min(value = 1, message = "Birds sold must be at least 1")
    private Integer birdsSold;

    @NotNull(message = "Total live weight (kg) is required")
    @Min(value = 1, message = "Live weight must be positive")
    private Double totalLiveWeightKg;

    @NotNull(message = "Live selling price per kg is required")
    @Min(value = 1, message = "Selling price must be positive")
    private Double sellingPricePerKg;

    private String buyerName;
    private String remarks;

    public Integer getBirdsSold() { return birdsSold; }
    public void setBirdsSold(Integer birdsSold) { this.birdsSold = birdsSold; }
    public Double getTotalLiveWeightKg() { return totalLiveWeightKg; }
    public void setTotalLiveWeightKg(Double totalLiveWeightKg) { this.totalLiveWeightKg = totalLiveWeightKg; }
    public Double getSellingPricePerKg() { return sellingPricePerKg; }
    public void setSellingPricePerKg(Double sellingPricePerKg) { this.sellingPricePerKg = sellingPricePerKg; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
