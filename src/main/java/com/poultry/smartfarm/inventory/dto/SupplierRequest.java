package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class SupplierRequest {

    @NotBlank(message = "Farm ID is required")
    private String farmId;

    @NotBlank(message = "Supplier code is required")
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    private String contactPerson;

    @NotBlank(message = "Mobile/Phone number is required")
    private String phone;

    private String email;
    private String gstNumber;
    private String address;
    private String city;
    private String state;
    private List<String> suppliedCategories = new ArrayList<>();
    private String notes;

    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
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
    public List<String> getSuppliedCategories() { return suppliedCategories; }
    public void setSuppliedCategories(List<String> suppliedCategories) { this.suppliedCategories = suppliedCategories; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
