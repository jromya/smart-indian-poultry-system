package com.poultry.smartfarm.batch.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "egg_records")
@CompoundIndex(name = "idx_egg_batch_date", def = "{'batchId': 1, 'recordDate': 1}", unique = true)
public class EggRecord {

    @Id
    private String id;

    @Indexed
    private String farmId;

    @Indexed
    private String batchId;
    private String batchCode;

    private LocalDate recordDate;
    private Integer liveHenCount;

    private Integer totalEggs = 0;
    private Integer goodEggs = 0;
    private Integer damagedEggs = 0;
    private Integer brokenEggs = 0;
    private Integer dirtyEggs = 0;

    // Egg Grading (counts of good eggs)
    private Integer gradeSmall = 0;
    private Integer gradeMedium = 0;
    private Integer gradeLarge = 0;
    private Integer gradeExtraLarge = 0;

    // Hen-Day Egg Production % = (totalEggs / liveHenCount) * 100
    private Double henDayProductionPct = 0.0;

    private Double feedConsumedKg = 0.0;
    private Double averageBirdWeightGrams;

    // Daily Egg Sales & Revenue
    private Integer eggsSoldCount = 0;
    private Double pricePerEgg = 0.0;
    private Double eggSaleRevenue = 0.0;

    private String remarks;
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public EggRecord() {
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
    public Integer getLiveHenCount() { return liveHenCount; }
    public void setLiveHenCount(Integer liveHenCount) { this.liveHenCount = liveHenCount; }
    public Integer getTotalEggs() { return totalEggs != null ? totalEggs : 0; }
    public void setTotalEggs(Integer totalEggs) { this.totalEggs = totalEggs; }
    public Integer getGoodEggs() { return goodEggs != null ? goodEggs : 0; }
    public void setGoodEggs(Integer goodEggs) { this.goodEggs = goodEggs; }
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
    public Double getHenDayProductionPct() { return henDayProductionPct != null ? henDayProductionPct : 0.0; }
    public void setHenDayProductionPct(Double henDayProductionPct) { this.henDayProductionPct = henDayProductionPct; }
    public Double getFeedConsumedKg() { return feedConsumedKg != null ? feedConsumedKg : 0.0; }
    public void setFeedConsumedKg(Double feedConsumedKg) { this.feedConsumedKg = feedConsumedKg; }
    public Double getAverageBirdWeightGrams() { return averageBirdWeightGrams; }
    public void setAverageBirdWeightGrams(Double averageBirdWeightGrams) { this.averageBirdWeightGrams = averageBirdWeightGrams; }
    public Integer getEggsSoldCount() { return eggsSoldCount != null ? eggsSoldCount : 0; }
    public void setEggsSoldCount(Integer eggsSoldCount) { this.eggsSoldCount = eggsSoldCount; }
    public Double getPricePerEgg() { return pricePerEgg != null ? pricePerEgg : 0.0; }
    public void setPricePerEgg(Double pricePerEgg) { this.pricePerEgg = pricePerEgg; }
    public Double getEggSaleRevenue() { return eggSaleRevenue != null ? eggSaleRevenue : 0.0; }
    public void setEggSaleRevenue(Double eggSaleRevenue) { this.eggSaleRevenue = eggSaleRevenue; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
