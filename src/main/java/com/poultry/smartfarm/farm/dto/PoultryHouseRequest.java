package com.poultry.smartfarm.farm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PoultryHouseRequest {

    @NotBlank(message = "Farm ID is required")
    private String farmId;

    @NotBlank(message = "House / Shed Code is required (e.g. SHED-01)")
    private String houseCode;

    @NotBlank(message = "House / Shed Name is required")
    private String houseName;

    @NotBlank(message = "House Type is required")
    private String houseType;

    @NotBlank(message = "Bird Purpose is required")
    private String birdPurpose;

    @NotNull(message = "Maximum Bird Capacity is required")
    @Min(value = 10, message = "Bird capacity must be at least 10")
    private Integer maxBirdCapacity;

    private Double lengthFeet;
    private Double widthFeet;
    private String ventilationType = "NATURAL_OPEN";
    private String status = "AVAILABLE";
    private String notes;

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getHouseCode() {
        return houseCode;
    }

    public void setHouseCode(String houseCode) {
        this.houseCode = houseCode;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public String getHouseType() {
        return houseType;
    }

    public void setHouseType(String houseType) {
        this.houseType = houseType;
    }

    public String getBirdPurpose() {
        return birdPurpose;
    }

    public void setBirdPurpose(String birdPurpose) {
        this.birdPurpose = birdPurpose;
    }

    public Integer getMaxBirdCapacity() {
        return maxBirdCapacity;
    }

    public void setMaxBirdCapacity(Integer maxBirdCapacity) {
        this.maxBirdCapacity = maxBirdCapacity;
    }

    public Double getLengthFeet() {
        return lengthFeet;
    }

    public void setLengthFeet(Double lengthFeet) {
        this.lengthFeet = lengthFeet;
    }

    public Double getWidthFeet() {
        return widthFeet;
    }

    public void setWidthFeet(Double widthFeet) {
        this.widthFeet = widthFeet;
    }

    public String getVentilationType() {
        return ventilationType;
    }

    public void setVentilationType(String ventilationType) {
        this.ventilationType = ventilationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
