package com.poultry.smartfarm.farm.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "poultry_houses")
@CompoundIndex(name = "idx_farm_house_code", def = "{'farmId': 1, 'houseCode': 1}", unique = true)
public class PoultryHouse {

    @Id
    private String id;

    @Indexed
    private String farmId;

    private String farmName;
    private String houseCode;
    private String houseName;

    // DEEP_LITTER, CAGE_SYSTEM, EC_SHED, FREE_RANGE_DESI, BROODER_HOUSE
    private String houseType;

    // BROILER, LAYER, INDIGENOUS, BREEDER
    private String birdPurpose;

    private Integer maxBirdCapacity;
    private Integer currentBirdOccupancy = 0;

    private Double lengthFeet;
    private Double widthFeet;

    // NATURAL_OPEN, TUNNEL_FAN_PAD, EXHAUST_FAN
    private String ventilationType;

    // AVAILABLE, OCCUPIED, CLEANING_REST, MAINTENANCE
    private String status = "AVAILABLE";

    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public PoultryHouse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
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

    public Integer getCurrentBirdOccupancy() {
        return currentBirdOccupancy;
    }

    public void setCurrentBirdOccupancy(Integer currentBirdOccupancy) {
        this.currentBirdOccupancy = currentBirdOccupancy;
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
