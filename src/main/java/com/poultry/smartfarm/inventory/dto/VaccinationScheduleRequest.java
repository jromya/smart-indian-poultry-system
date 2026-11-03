package com.poultry.smartfarm.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class VaccinationScheduleRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "Vaccine name is required")
    private String vaccineName;

    @NotBlank(message = "Target disease is required")
    private String targetDisease;

    @PositiveOrZero
    private Integer recommendedAgeDays;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    private String route = "EYE_DROP";
    private String dosagePerBird = "1 drop / bird";
    private String veterinaryGuidanceNotes;

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
    public String getTargetDisease() { return targetDisease; }
    public void setTargetDisease(String targetDisease) { this.targetDisease = targetDisease; }
    public Integer getRecommendedAgeDays() { return recommendedAgeDays; }
    public void setRecommendedAgeDays(Integer recommendedAgeDays) { this.recommendedAgeDays = recommendedAgeDays; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getDosagePerBird() { return dosagePerBird; }
    public void setDosagePerBird(String dosagePerBird) { this.dosagePerBird = dosagePerBird; }
    public String getVeterinaryGuidanceNotes() { return veterinaryGuidanceNotes; }
    public void setVeterinaryGuidanceNotes(String veterinaryGuidanceNotes) { this.veterinaryGuidanceNotes = veterinaryGuidanceNotes; }
}
