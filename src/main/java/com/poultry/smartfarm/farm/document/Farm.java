package com.poultry.smartfarm.farm.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "farms")
public class Farm {

    @Id
    private String id;

    @Indexed(unique = true)
    private String farmCode;

    private String farmName;
    private String ownerName;
    private String contactPhone;
    private String contactEmail;

    private String villageOrCity;
    private String taluka;
    private String district;
    private String state;
    private String pincode;

    // BROILER, LAYER, INDIGENOUS, MIXED
    private String farmCategory;

    private Double totalAreaSqFt;
    private Integer totalBirdCapacity;

    // BOREWELL, WELL, MUNICIPAL, TANKER
    private String waterSource;

    // SOLAR, DIESEL_GENSET, INVERTER, NONE
    private String electricityBackup;

    private String registrationNumber;
    private String ownerUserId;
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Farm() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmCode() {
        return farmCode;
    }

    public void setFarmCode(String farmCode) {
        this.farmCode = farmCode;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getVillageOrCity() {
        return villageOrCity;
    }

    public void setVillageOrCity(String villageOrCity) {
        this.villageOrCity = villageOrCity;
    }

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getFarmCategory() {
        return farmCategory;
    }

    public void setFarmCategory(String farmCategory) {
        this.farmCategory = farmCategory;
    }

    public Double getTotalAreaSqFt() {
        return totalAreaSqFt;
    }

    public void setTotalAreaSqFt(Double totalAreaSqFt) {
        this.totalAreaSqFt = totalAreaSqFt;
    }

    public Integer getTotalBirdCapacity() {
        return totalBirdCapacity;
    }

    public void setTotalBirdCapacity(Integer totalBirdCapacity) {
        this.totalBirdCapacity = totalBirdCapacity;
    }

    public String getWaterSource() {
        return waterSource;
    }

    public void setWaterSource(String waterSource) {
        this.waterSource = waterSource;
    }

    public String getElectricityBackup() {
        return electricityBackup;
    }

    public void setElectricityBackup(String electricityBackup) {
        this.electricityBackup = electricityBackup;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
