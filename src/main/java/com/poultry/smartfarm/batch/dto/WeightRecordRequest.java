package com.poultry.smartfarm.batch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class WeightRecordRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotNull(message = "Weighing date is required")
    private LocalDate recordDate;

    @Min(value = 1, message = "Sample bird count must be at least 1")
    private Integer sampleBirdCount = 50;

    @NotNull(message = "Average bird weight (grams) is required")
    @Min(value = 10, message = "Average weight must be at least 10 grams")
    private Double averageWeightGrams;

    private Double minWeightGrams;
    private Double maxWeightGrams;
    private Double uniformityPct;
    private String remarks;

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Integer getSampleBirdCount() { return sampleBirdCount != null ? sampleBirdCount : 50; }
    public void setSampleBirdCount(Integer sampleBirdCount) { this.sampleBirdCount = sampleBirdCount; }
    public Double getAverageWeightGrams() { return averageWeightGrams; }
    public void setAverageWeightGrams(Double averageWeightGrams) { this.averageWeightGrams = averageWeightGrams; }
    public Double getMinWeightGrams() { return minWeightGrams; }
    public void setMinWeightGrams(Double minWeightGrams) { this.minWeightGrams = minWeightGrams; }
    public Double getMaxWeightGrams() { return maxWeightGrams; }
    public void setMaxWeightGrams(Double maxWeightGrams) { this.maxWeightGrams = maxWeightGrams; }
    public Double getUniformityPct() { return uniformityPct; }
    public void setUniformityPct(Double uniformityPct) { this.uniformityPct = uniformityPct; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
