package com.poultry.smartfarm.batch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class EggRecordRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotNull(message = "Collection date is required")
    private LocalDate recordDate;

    @NotNull(message = "Total eggs collected is required")
    @Min(value = 0)
    private Integer totalEggs;

    @Min(value = 0)
    private Integer damagedEggs = 0;

    @Min(value = 0)
    private Integer brokenEggs = 0;

    @Min(value = 0)
    private Integer dirtyEggs = 0;

    @Min(value = 0)
    private Integer gradeSmall = 0;

    @Min(value = 0)
    private Integer gradeMedium = 0;

    @Min(value = 0)
    private Integer gradeLarge = 0;

    @Min(value = 0)
    private Integer gradeExtraLarge = 0;

    private Double feedConsumedKg = 0.0;
    private Double averageBirdWeightGrams;

    @Min(value = 0)
    private Integer eggsSoldCount = 0;

    @Min(value = 0)
    private Double pricePerEgg = 0.0;

    private String remarks;

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Integer getTotalEggs() { return totalEggs != null ? totalEggs : 0; }
    public void setTotalEggs(Integer totalEggs) { this.totalEggs = totalEggs; }
    public Integer getDamagedEggs() { return damagedEggs != null ? damagedEggs : 0; }
    public void setDamagedEggs(Integer damagedEggs) { this.damagedEggs = damagedEggs; }
    public Integer getBrokenEggs() { return brokenEggs != null ? brokenEggs : 0; }
    public void setBrokenEggs(Integer brokenEggs) { this.brokenEggs = brokenEggs; }
    public Integer getDirtyEggs() { return dirtyEggs != null ? dirtyEggs : 0; }
    public void setDirtyEggs(Integer dirtyEggs) { this.dirtyEggs = dirtyEggs; }
    public Integer getGradeSmall() { return gradeSmall != null ? gradeSmall : 0; }
    public void setGradeSmall(Integer gradeSmall) { this.gradeSmall = gradeSmall; }
    public Integer getGradeMedium() { return gradeMedium != null ? gradeMedium : 0; }
    public void setGradeMedium(Integer gradeMedium) { this.gradeMedium = gradeMedium; }
    public Integer getGradeLarge() { return gradeLarge != null ? gradeLarge : 0; }
    public void setGradeLarge(Integer gradeLarge) { this.gradeLarge = gradeLarge; }
    public Integer getGradeExtraLarge() { return gradeExtraLarge != null ? gradeExtraLarge : 0; }
    public void setGradeExtraLarge(Integer gradeExtraLarge) { this.gradeExtraLarge = gradeExtraLarge; }
    public Double getFeedConsumedKg() { return feedConsumedKg != null ? feedConsumedKg : 0.0; }
    public void setFeedConsumedKg(Double feedConsumedKg) { this.feedConsumedKg = feedConsumedKg; }
    public Double getAverageBirdWeightGrams() { return averageBirdWeightGrams; }
    public void setAverageBirdWeightGrams(Double averageBirdWeightGrams) { this.averageBirdWeightGrams = averageBirdWeightGrams; }
    public Integer getEggsSoldCount() { return eggsSoldCount != null ? eggsSoldCount : 0; }
    public void setEggsSoldCount(Integer eggsSoldCount) { this.eggsSoldCount = eggsSoldCount; }
    public Double getPricePerEgg() { return pricePerEgg != null ? pricePerEgg : 0.0; }
    public void setPricePerEgg(Double pricePerEgg) { this.pricePerEgg = pricePerEgg; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
