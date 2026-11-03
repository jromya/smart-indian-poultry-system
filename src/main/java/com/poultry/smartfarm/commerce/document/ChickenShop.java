package com.poultry.smartfarm.commerce.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chicken_shops")
public class ChickenShop {

    @Id
    private String id;

    @Indexed(unique = true)
    private String ownerUserId;
    private String ownerUsername;

    @Indexed(unique = true)
    private String shopCode;
    private String shopName;
    private String phone;
    private String city;
    private String state;

    // SEPARATE STOCK: Live Birds vs Processed Dressed Meat (Never conflated!)
    private Integer liveBirdsStockCount = 0;
    private Double liveBirdsStockWeightKg = 0.0;
    private Double defaultMeatSellingPricePerKg = 220.0;

    private Double dressedMeatStockKg = 0.0;
    private Double totalDressedMeatProducedKg = 0.0;
    private Double totalDressedMeatSoldKg = 0.0;

    // Cumulative Shop Financials
    private Double totalLivePurchaseCostInr = 0.0;
    private Double totalProcessingExpensesInr = 0.0;
    private Double totalOtherAllocatedExpensesInr = 0.0;
    private Double totalMeatSalesRevenueInr = 0.0;
    private Double totalByProductSalesRevenueInr = 0.0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public ChickenShop() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public String getShopCode() { return shopCode; }
    public void setShopCode(String shopCode) { this.shopCode = shopCode; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Integer getLiveBirdsStockCount() { return liveBirdsStockCount != null ? liveBirdsStockCount : 0; }
    public void setLiveBirdsStockCount(Integer liveBirdsStockCount) { this.liveBirdsStockCount = liveBirdsStockCount; }
    public Double getLiveBirdsStockWeightKg() { return liveBirdsStockWeightKg != null ? liveBirdsStockWeightKg : 0.0; }
    public void setLiveBirdsStockWeightKg(Double liveBirdsStockWeightKg) { this.liveBirdsStockWeightKg = liveBirdsStockWeightKg; }
    public Double getDefaultMeatSellingPricePerKg() { return defaultMeatSellingPricePerKg != null ? defaultMeatSellingPricePerKg : 220.0; }
    public void setDefaultMeatSellingPricePerKg(Double defaultMeatSellingPricePerKg) { this.defaultMeatSellingPricePerKg = defaultMeatSellingPricePerKg; }
    public Double getDressedMeatStockKg() { return dressedMeatStockKg != null ? dressedMeatStockKg : 0.0; }
    public void setDressedMeatStockKg(Double dressedMeatStockKg) { this.dressedMeatStockKg = dressedMeatStockKg; }
    public Double getTotalDressedMeatProducedKg() { return totalDressedMeatProducedKg != null ? totalDressedMeatProducedKg : 0.0; }
    public void setTotalDressedMeatProducedKg(Double totalDressedMeatProducedKg) { this.totalDressedMeatProducedKg = totalDressedMeatProducedKg; }
    public Double getTotalDressedMeatSoldKg() { return totalDressedMeatSoldKg != null ? totalDressedMeatSoldKg : 0.0; }
    public void setTotalDressedMeatSoldKg(Double totalDressedMeatSoldKg) { this.totalDressedMeatSoldKg = totalDressedMeatSoldKg; }
    public Double getTotalLivePurchaseCostInr() { return totalLivePurchaseCostInr != null ? totalLivePurchaseCostInr : 0.0; }
    public void setTotalLivePurchaseCostInr(Double totalLivePurchaseCostInr) { this.totalLivePurchaseCostInr = totalLivePurchaseCostInr; }
    public Double getTotalProcessingExpensesInr() { return totalProcessingExpensesInr != null ? totalProcessingExpensesInr : 0.0; }
    public void setTotalProcessingExpensesInr(Double totalProcessingExpensesInr) { this.totalProcessingExpensesInr = totalProcessingExpensesInr; }
    public Double getTotalOtherAllocatedExpensesInr() { return totalOtherAllocatedExpensesInr != null ? totalOtherAllocatedExpensesInr : 0.0; }
    public void setTotalOtherAllocatedExpensesInr(Double totalOtherAllocatedExpensesInr) { this.totalOtherAllocatedExpensesInr = totalOtherAllocatedExpensesInr; }
    public Double getTotalMeatSalesRevenueInr() { return totalMeatSalesRevenueInr != null ? totalMeatSalesRevenueInr : 0.0; }
    public void setTotalMeatSalesRevenueInr(Double totalMeatSalesRevenueInr) { this.totalMeatSalesRevenueInr = totalMeatSalesRevenueInr; }
    public Double getTotalByProductSalesRevenueInr() { return totalByProductSalesRevenueInr != null ? totalByProductSalesRevenueInr : 0.0; }
    public void setTotalByProductSalesRevenueInr(Double totalByProductSalesRevenueInr) { this.totalByProductSalesRevenueInr = totalByProductSalesRevenueInr; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
