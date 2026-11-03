package com.poultry.smartfarm.batch.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "daily_records")
@CompoundIndex(name = "idx_batch_record_date_unique", def = "{'batchId': 1, 'recordDate': 1}", unique = true)
public class DailyRecord {

    @Id
    private String id;

    @Indexed
    private String farmId;

    @Indexed
    private String batchId;
    private String batchCode;

    private LocalDate recordDate;
    private Integer dayNumber;

    private Integer openingBirds;
    private Integer mortality = 0;
    private Integer culls = 0;
    private Integer additions = 0;
    private Integer transfersOut = 0;
    private Integer closingBirds;

    private String mortalityCause;

    // STARTER, GROWER, FINISHER, LAYER_MASH, DESI_FORAGE
    private String feedType = "STARTER";
    private Double feedConsumptionKg = 0.0;
    private Double averageBirdWeightGrams;
    private Double waterConsumptionLiters = 0.0;

    private Double temperatureCelsius;
    private Double humidityPct;

    private String medicineGiven;
    private String vaccinationGiven;

    private Double labourCost = 0.0;
    private Double electricityCost = 0.0;
    private Double otherExpenses = 0.0;

    private String farmerRemarks;
    private String recordedBy;
    private String correctionReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public DailyRecord() {
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
    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
    public Integer getOpeningBirds() { return openingBirds; }
    public void setOpeningBirds(Integer openingBirds) { this.openingBirds = openingBirds; }
    public Integer getMortality() { return mortality != null ? mortality : 0; }
    public void setMortality(Integer mortality) { this.mortality = mortality; }
    public Integer getCulls() { return culls != null ? culls : 0; }
    public void setCulls(Integer culls) { this.culls = culls; }
    public Integer getAdditions() { return additions != null ? additions : 0; }
    public void setAdditions(Integer additions) { this.additions = additions; }
    public Integer getTransfersOut() { return transfersOut != null ? transfersOut : 0; }
    public void setTransfersOut(Integer transfersOut) { this.transfersOut = transfersOut; }
    public Integer getClosingBirds() { return closingBirds; }
    public void setClosingBirds(Integer closingBirds) { this.closingBirds = closingBirds; }
    public String getMortalityCause() { return mortalityCause; }
    public void setMortalityCause(String mortalityCause) { this.mortalityCause = mortalityCause; }
    public String getFeedType() { return feedType; }
    public void setFeedType(String feedType) { this.feedType = feedType; }
    public Double getFeedConsumptionKg() { return feedConsumptionKg != null ? feedConsumptionKg : 0.0; }
    public void setFeedConsumptionKg(Double feedConsumptionKg) { this.feedConsumptionKg = feedConsumptionKg; }
    public Double getAverageBirdWeightGrams() { return averageBirdWeightGrams; }
    public void setAverageBirdWeightGrams(Double averageBirdWeightGrams) { this.averageBirdWeightGrams = averageBirdWeightGrams; }
    public Double getWaterConsumptionLiters() { return waterConsumptionLiters != null ? waterConsumptionLiters : 0.0; }
    public void setWaterConsumptionLiters(Double waterConsumptionLiters) { this.waterConsumptionLiters = waterConsumptionLiters; }
    public Double getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(Double temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }
    public Double getHumidityPct() { return humidityPct; }
    public void setHumidityPct(Double humidityPct) { this.humidityPct = humidityPct; }
    public String getMedicineGiven() { return medicineGiven; }
    public void setMedicineGiven(String medicineGiven) { this.medicineGiven = medicineGiven; }
    public String getVaccinationGiven() { return vaccinationGiven; }
    public void setVaccinationGiven(String vaccinationGiven) { this.vaccinationGiven = vaccinationGiven; }
    public Double getLabourCost() { return labourCost != null ? labourCost : 0.0; }
    public void setLabourCost(Double labourCost) { this.labourCost = labourCost; }
    public Double getElectricityCost() { return electricityCost != null ? electricityCost : 0.0; }
    public void setElectricityCost(Double electricityCost) { this.electricityCost = electricityCost; }
    public Double getOtherExpenses() { return otherExpenses != null ? otherExpenses : 0.0; }
    public void setOtherExpenses(Double otherExpenses) { this.otherExpenses = otherExpenses; }
    public String getFarmerRemarks() { return farmerRemarks; }
    public void setFarmerRemarks(String farmerRemarks) { this.farmerRemarks = farmerRemarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public String getCorrectionReason() { return correctionReason; }
    public void setCorrectionReason(String correctionReason) { this.correctionReason = correctionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
