package com.poultry.smartfarm.farm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class FarmRequest {

    @NotBlank(message = "Unique Farm Code is required (e.g. FARM-MH-001)")
    private String farmCode;

    @NotBlank(message = "Farm Name is required")
    private String farmName;

    @NotBlank(message = "Owner Name is required")
    private String ownerName;

    @NotBlank(message = "Contact mobile number is required")
    @Pattern(regexp = "^(\\+91[-\\s]?)?[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String contactPhone;

    private String contactEmail;

    @NotBlank(message = "Village or City is required")
    private String villageOrCity;

    private String taluka;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "State is required")
    private String state;

    private String pincode;

    @NotBlank(message = "Farm Category is required (BROILER, LAYER, INDIGENOUS, MIXED)")
    private String farmCategory;

    private Double totalAreaSqFt;

    @NotNull(message = "Total Bird Capacity is required")
    @Min(value = 10, message = "Bird capacity must be at least 10")
    private Integer totalBirdCapacity;

    private String waterSource = "BOREWELL";
    private String electricityBackup = "SOLAR";
    private String registrationNumber;
    private Boolean active = true;

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
