package com.poultry.smartfarm.batch.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "weight_records")
public class WeightRecord {

    @Id
    private String id;

    @Indexed
    private String farmId;

    @Indexed
    private String batchId;
    private String batchCode;

    private LocalDate recordDate;
    private Integer ageDays;
    private Integer weekNumber;

    private Integer sampleBirdCount = 50;
    private Double averageWeightGrams;
    private Double minWeightGrams;
    private Double maxWeightGrams;
    private Double uniformityPct;
    private Double expectedBreedWeightGrams;

    private String remarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public WeightRecord() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Integer getAgeDays() { return ageDays; }
    public void setAgeDays(Integer ageDays) { this.ageDays = ageDays; }
    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }
    public Integer getSampleBirdCount() { return sampleBirdCount; }
    public void setSampleBirdCount(Integer sampleBirdCount) { this.sampleBirdCount = sampleBirdCount; }
    public Double getAverageWeightGrams() { return averageWeightGrams; }
    public void setAverageWeightGrams(Double averageWeightGrams) { this.averageWeightGrams = averageWeightGrams; }
    public Double getMinWeightGrams() { return minWeightGrams; }
    public void setMinWeightGrams(Double minWeightGrams) { this.minWeightGrams = minWeightGrams; }
    public Double getMaxWeightGrams() { return maxWeightGrams; }
    public void setMaxWeightGrams(Double maxWeightGrams) { this.maxWeightGrams = maxWeightGrams; }
    public Double getUniformityPct() { return uniformityPct; }
    public void setUniformityPct(Double uniformityPct) { this.uniformityPct = uniformityPct; }
    public Double getExpectedBreedWeightGrams() { return expectedBreedWeightGrams; }
    public void setExpectedBreedWeightGrams(Double expectedBreedWeightGrams) { this.expectedBreedWeightGrams = expectedBreedWeightGrams; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
