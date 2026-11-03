package com.poultry.smartfarm.commerce.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "wholesalers")
public class WholesalerDeal {

    @Id
    private String id;

    @Indexed
    private String farmId;
    private String farmName;

    @Indexed
    private String batchId;
    private String batchCode;

    @Indexed(unique = true)
    private String dealNumber;

    @Indexed
    private LocalDate dealDate;

    @Indexed
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String buyerCategory = "WHOLESALER";

    private Integer liveBirdQuantity;
    private Double averageWeightKg;
    private Double totalLiveWeightKg;

    private Double purchasePricePerKg;
    private Double transportCostInr = 0.0;
    private Double totalPurchaseAmountInr;

    private Double sellingPricePerKg;
    private Double totalSaleAmountInr;
    private Double cashReceivedInr = 0.0;
    private Double outstandingPaymentInr = 0.0;
    private String paymentStatus = "PAID";

    private String vehicleNumber;
    private String linkedSaleId;
    private String remarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public WholesalerDeal() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getDealNumber() { return dealNumber; }
    public void setDealNumber(String dealNumber) { this.dealNumber = dealNumber; }
    public LocalDate getDealDate() { return dealDate; }
    public void setDealDate(LocalDate dealDate) { this.dealDate = dealDate; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getBuyerCategory() { return buyerCategory; }
    public void setBuyerCategory(String buyerCategory) { this.buyerCategory = buyerCategory; }
    public Integer getLiveBirdQuantity() { return liveBirdQuantity != null ? liveBirdQuantity : 0; }
    public void setLiveBirdQuantity(Integer liveBirdQuantity) { this.liveBirdQuantity = liveBirdQuantity; }
    public Double getAverageWeightKg() { return averageWeightKg != null ? averageWeightKg : 0.0; }
    public void setAverageWeightKg(Double averageWeightKg) { this.averageWeightKg = averageWeightKg; }
    public Double getTotalLiveWeightKg() { return totalLiveWeightKg != null ? totalLiveWeightKg : 0.0; }
    public void setTotalLiveWeightKg(Double totalLiveWeightKg) { this.totalLiveWeightKg = totalLiveWeightKg; }
    public Double getPurchasePricePerKg() { return purchasePricePerKg != null ? purchasePricePerKg : 0.0; }
    public void setPurchasePricePerKg(Double purchasePricePerKg) { this.purchasePricePerKg = purchasePricePerKg; }
    public Double getTransportCostInr() { return transportCostInr != null ? transportCostInr : 0.0; }
    public void setTransportCostInr(Double transportCostInr) { this.transportCostInr = transportCostInr; }
    public Double getTotalPurchaseAmountInr() { return totalPurchaseAmountInr != null ? totalPurchaseAmountInr : 0.0; }
    public void setTotalPurchaseAmountInr(Double totalPurchaseAmountInr) { this.totalPurchaseAmountInr = totalPurchaseAmountInr; }
    public Double getSellingPricePerKg() { return sellingPricePerKg != null ? sellingPricePerKg : 0.0; }
    public void setSellingPricePerKg(Double sellingPricePerKg) { this.sellingPricePerKg = sellingPricePerKg; }
    public Double getTotalSaleAmountInr() { return totalSaleAmountInr != null ? totalSaleAmountInr : 0.0; }
    public void setTotalSaleAmountInr(Double totalSaleAmountInr) { this.totalSaleAmountInr = totalSaleAmountInr; }
    public Double getCashReceivedInr() { return cashReceivedInr != null ? cashReceivedInr : 0.0; }
    public void setCashReceivedInr(Double cashReceivedInr) { this.cashReceivedInr = cashReceivedInr; }
    public Double getOutstandingPaymentInr() { return outstandingPaymentInr != null ? outstandingPaymentInr : 0.0; }
    public void setOutstandingPaymentInr(Double outstandingPaymentInr) { this.outstandingPaymentInr = outstandingPaymentInr; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getLinkedSaleId() { return linkedSaleId; }
    public void setLinkedSaleId(String linkedSaleId) { this.linkedSaleId = linkedSaleId; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
