package com.poultry.smartfarm.commerce.dto;

import jakarta.validation.constraints.NotBlank;

public class CustomerRequest {

    private String farmId;

    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    // WHOLESALER, CHICKEN_RETAILER, MEAT_SHOP, EGG_RETAILER, DIRECT_CUSTOMER
    @NotBlank(message = "Buyer category is required")
    private String buyerCategory = "DIRECT_CUSTOMER";

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
    private String gstNumber;
    private String address;
    private String city;
    private String state;
    private String notes;

    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
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
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
