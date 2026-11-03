package com.poultry.smartfarm.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class WholesalerDealRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "Deal / Challan number is required")
    private String dealNumber;

    @NotNull(message = "Deal date is required")
    private LocalDate dealDate;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull
    @Positive(message = "Live bird quantity must be positive")
    private Integer liveBirdQuantity;

    @NotNull
    @Positive(message = "Total live weight (kg) must be positive")
    private Double totalLiveWeightKg;

    @PositiveOrZero(message = "Base cost / purchase price per kg cannot be negative")
    private Double purchasePricePerKg = 0.0;

    @PositiveOrZero(message = "Transport cost cannot be negative")
    private Double transportCostInr = 0.0;

    @NotNull
    @Positive(message = "Wholesale selling price per kg must be positive")
    private Double sellingPricePerKg;

    @PositiveOrZero(message = "Cash received cannot be negative")
    private Double cashReceivedInr = 0.0;

    private String vehicleNumber;
    private String remarks;

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getDealNumber() { return dealNumber; }
    public void setDealNumber(String dealNumber) { this.dealNumber = dealNumber; }
    public LocalDate getDealDate() { return dealDate; }
    public void setDealDate(LocalDate dealDate) { this.dealDate = dealDate; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public Integer getLiveBirdQuantity() { return liveBirdQuantity; }
    public void setLiveBirdQuantity(Integer liveBirdQuantity) { this.liveBirdQuantity = liveBirdQuantity; }
    public Double getTotalLiveWeightKg() { return totalLiveWeightKg; }
    public void setTotalLiveWeightKg(Double totalLiveWeightKg) { this.totalLiveWeightKg = totalLiveWeightKg; }
    public Double getPurchasePricePerKg() { return purchasePricePerKg != null ? purchasePricePerKg : 0.0; }
    public void setPurchasePricePerKg(Double purchasePricePerKg) { this.purchasePricePerKg = purchasePricePerKg; }
    public Double getTransportCostInr() { return transportCostInr != null ? transportCostInr : 0.0; }
    public void setTransportCostInr(Double transportCostInr) { this.transportCostInr = transportCostInr; }
    public Double getSellingPricePerKg() { return sellingPricePerKg; }
    public void setSellingPricePerKg(Double sellingPricePerKg) { this.sellingPricePerKg = sellingPricePerKg; }
    public Double getCashReceivedInr() { return cashReceivedInr != null ? cashReceivedInr : 0.0; }
    public void setCashReceivedInr(Double cashReceivedInr) { this.cashReceivedInr = cashReceivedInr; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
