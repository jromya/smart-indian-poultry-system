package com.poultry.smartfarm.commerce.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "customers")
public class Customer {

    @Id
    private String id;

    // FARM, RETAIL, CHICKEN_SHOP
    @Indexed
    private String businessContext = "FARM";

    @Indexed
    private String farmId;
    private String farmName;

    @Indexed
    private String ownerUserId;

    @Indexed(unique = true)
    private String customerCode;

    private String customerName;

    // WHOLESALER, CHICKEN_RETAILER, MEAT_SHOP, EGG_RETAILER, DIRECT_CUSTOMER
    @Indexed
    private String buyerCategory = "DIRECT_CUSTOMER";

    private String phone;
    private String email;
    private String gstNumber;
    private String address;
    private String city;
    private String state;

    private Double totalBilledAmount = 0.0;
    private Double totalReceivedAmount = 0.0;
    private Double outstandingBalance = 0.0;

    private boolean active = true;
    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Customer() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBusinessContext() { return businessContext; }
    public void setBusinessContext(String businessContext) { this.businessContext = businessContext; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getBuyerCategory() { return buyerCategory; }
    public void setBuyerCategory(String buyerCategory) { this.buyerCategory = buyerCategory; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getTotalBilledAmount() { return totalBilledAmount != null ? totalBilledAmount : 0.0; }
    public void setTotalBilledAmount(Double totalBilledAmount) { this.totalBilledAmount = totalBilledAmount; }
    public Double getTotalReceivedAmount() { return totalReceivedAmount != null ? totalReceivedAmount : 0.0; }
    public void setTotalReceivedAmount(Double totalReceivedAmount) { this.totalReceivedAmount = totalReceivedAmount; }
    public Double getOutstandingBalance() { return outstandingBalance != null ? outstandingBalance : 0.0; }
    public void setOutstandingBalance(Double outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
