package com.poultry.smartfarm.commerce.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "retailers")
public class RetailerStore {

    @Id
    private String id;

    @Indexed(unique = true)
    private String ownerUserId;
    private String ownerUsername;

    @Indexed(unique = true)
    private String storeCode;
    private String storeName;
    private String phone;
    private String city;
    private String state;

    // Retail stock tracked separately for Live Birds and Eggs
    private Integer liveBirdStockCount = 0;
    private Double liveBirdAvgPurchaseCost = 0.0;

    private Integer eggStockPieces = 0;
    private Double eggAvgPurchaseCost = 0.0;

    private Double totalStockPurchaseCostInr = 0.0;
    private Double totalChickenSalesRevenueInr = 0.0;
    private Double totalEggSalesRevenueInr = 0.0;
    private Double totalOperatingExpensesInr = 0.0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public RetailerStore() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Integer getLiveBirdStockCount() { return liveBirdStockCount != null ? liveBirdStockCount : 0; }
    public void setLiveBirdStockCount(Integer liveBirdStockCount) { this.liveBirdStockCount = liveBirdStockCount; }
    public Double getLiveBirdAvgPurchaseCost() { return liveBirdAvgPurchaseCost != null ? liveBirdAvgPurchaseCost : 0.0; }
    public void setLiveBirdAvgPurchaseCost(Double liveBirdAvgPurchaseCost) { this.liveBirdAvgPurchaseCost = liveBirdAvgPurchaseCost; }
    public Integer getEggStockPieces() { return eggStockPieces != null ? eggStockPieces : 0; }
    public void setEggStockPieces(Integer eggStockPieces) { this.eggStockPieces = eggStockPieces; }
    public Double getEggAvgPurchaseCost() { return eggAvgPurchaseCost != null ? eggAvgPurchaseCost : 0.0; }
    public void setEggAvgPurchaseCost(Double eggAvgPurchaseCost) { this.eggAvgPurchaseCost = eggAvgPurchaseCost; }
    public Double getTotalStockPurchaseCostInr() { return totalStockPurchaseCostInr != null ? totalStockPurchaseCostInr : 0.0; }
    public void setTotalStockPurchaseCostInr(Double totalStockPurchaseCostInr) { this.totalStockPurchaseCostInr = totalStockPurchaseCostInr; }
    public Double getTotalChickenSalesRevenueInr() { return totalChickenSalesRevenueInr != null ? totalChickenSalesRevenueInr : 0.0; }
    public void setTotalChickenSalesRevenueInr(Double totalChickenSalesRevenueInr) { this.totalChickenSalesRevenueInr = totalChickenSalesRevenueInr; }
    public Double getTotalEggSalesRevenueInr() { return totalEggSalesRevenueInr != null ? totalEggSalesRevenueInr : 0.0; }
    public void setTotalEggSalesRevenueInr(Double totalEggSalesRevenueInr) { this.totalEggSalesRevenueInr = totalEggSalesRevenueInr; }
    public Double getTotalOperatingExpensesInr() { return totalOperatingExpensesInr != null ? totalOperatingExpensesInr : 0.0; }
    public void setTotalOperatingExpensesInr(Double totalOperatingExpensesInr) { this.totalOperatingExpensesInr = totalOperatingExpensesInr; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
