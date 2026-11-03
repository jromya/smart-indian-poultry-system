package com.poultry.smartfarm.batch.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "mortality_records")
public class MortalityRecord {

    @Id
    private String id;

    @Indexed
    private String farmId;

    @Indexed
    private String batchId;
    private String batchCode;

    private String dailyRecordId;
    private LocalDate recordDate;
    private Integer ageDays;

    private Integer mortalityCount = 0;
    private Integer cullCount = 0;
    private String cause;
    private String disposalMethod = "COMPOST_PIT";
    private String recordedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    public MortalityRecord() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getDailyRecordId() { return dailyRecordId; }
    public void setDailyRecordId(String dailyRecordId) { this.dailyRecordId = dailyRecordId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Integer getAgeDays() { return ageDays; }
    public void setAgeDays(Integer ageDays) { this.ageDays = ageDays; }
    public Integer getMortalityCount() { return mortalityCount != null ? mortalityCount : 0; }
    public void setMortalityCount(Integer mortalityCount) { this.mortalityCount = mortalityCount; }
    public Integer getCullCount() { return cullCount != null ? cullCount : 0; }
    public void setCullCount(Integer cullCount) { this.cullCount = cullCount; }
    public String getCause() { return cause; }
    public void setCause(String cause) { this.cause = cause; }
    public String getDisposalMethod() { return disposalMethod; }
    public void setDisposalMethod(String disposalMethod) { this.disposalMethod = disposalMethod; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
