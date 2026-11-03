package com.poultry.smartfarm.inventory.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "suppliers")
public class Supplier {

    @Id
    private String id;

    @Indexed
    private String farmId;
    private String farmName;

    @Indexed(unique = true)
    private String supplierCode;

    private String supplierName;
    private String contactPerson;
    private String phone;
    private String email;
    private String gstNumber;
    private String address;
    private String city;
    private String state;

    // FEED, MEDICINE, VACCINE, CHICKS, EQUIPMENT, PACKAGING, OTHER
    private List<String> suppliedCategories = new ArrayList<>();

    private Double totalPurchasesAmount = 0.0;
    private Double totalPaidAmount = 0.0;
    private Double outstandingPayable = 0.0;

    private boolean active = true;
    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Supplier() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
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
    public Double getTotalPurchasesAmount() { return totalPurchasesAmount != null ? totalPurchasesAmount : 0.0; }
    public void setTotalPurchasesAmount(Double totalPurchasesAmount) { this.totalPurchasesAmount = totalPurchasesAmount; }
    public Double getTotalPaidAmount() { return totalPaidAmount != null ? totalPaidAmount : 0.0; }
    public void setTotalPaidAmount(Double totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }
    public Double getOutstandingPayable() { return outstandingPayable != null ? outstandingPayable : 0.0; }
    public void setOutstandingPayable(Double outstandingPayable) { this.outstandingPayable = outstandingPayable; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
