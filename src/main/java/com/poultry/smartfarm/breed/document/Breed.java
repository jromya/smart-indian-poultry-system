package com.poultry.smartfarm.breed.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "breeds")
public class Breed {

    @Id
    private String id;

    @Indexed(unique = true)
    private String breedCode;

    @Indexed(unique = true)
    private String breedName;

    private String localLanguageName;
    private Map<String, String> localizedNames = new HashMap<>();

    // INDIGENOUS, IMPROVED_RURAL, COMMERCIAL, CUSTOM
    @Indexed
    private String breedCategory;

    private String origin;

    // MEAT, EGG, DUAL_PURPOSE, GAME_ORNAMENTAL
    private String primaryPurpose;

    private Double purchasePricePerChick;

    private GrowthInfo expectedGrowth = new GrowthInfo();
    private EggInfo eggProduction = new EggInfo();
    private FeedInfo feedRequirements = new FeedInfo();
    private PerformanceInfo performanceTargets = new PerformanceInfo();

    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Breed() {
    }

    public static class GrowthInfo {
        private Integer standardMarketAgeDays;
        private Double expectedMarketWeightGrams;
        private Double expectedAdultMaleWeightGrams;
        private Double expectedAdultFemaleWeightGrams;
        private String growthSummary;

        public GrowthInfo() {
        }

        public GrowthInfo(Integer standardMarketAgeDays, Double expectedMarketWeightGrams,
                          Double expectedAdultMaleWeightGrams, Double expectedAdultFemaleWeightGrams,
                          String growthSummary) {
            this.standardMarketAgeDays = standardMarketAgeDays;
            this.expectedMarketWeightGrams = expectedMarketWeightGrams;
            this.expectedAdultMaleWeightGrams = expectedAdultMaleWeightGrams;
            this.expectedAdultFemaleWeightGrams = expectedAdultFemaleWeightGrams;
            this.growthSummary = growthSummary;
        }

        public Integer getStandardMarketAgeDays() { return standardMarketAgeDays; }
        public void setStandardMarketAgeDays(Integer standardMarketAgeDays) { this.standardMarketAgeDays = standardMarketAgeDays; }
        public Double getExpectedMarketWeightGrams() { return expectedMarketWeightGrams; }
        public void setExpectedMarketWeightGrams(Double expectedMarketWeightGrams) { this.expectedMarketWeightGrams = expectedMarketWeightGrams; }
        public Double getExpectedAdultMaleWeightGrams() { return expectedAdultMaleWeightGrams; }
        public void setExpectedAdultMaleWeightGrams(Double expectedAdultMaleWeightGrams) { this.expectedAdultMaleWeightGrams = expectedAdultMaleWeightGrams; }
        public Double getExpectedAdultFemaleWeightGrams() { return expectedAdultFemaleWeightGrams; }
        public void setExpectedAdultFemaleWeightGrams(Double expectedAdultFemaleWeightGrams) { this.expectedAdultFemaleWeightGrams = expectedAdultFemaleWeightGrams; }
        public String getGrowthSummary() { return growthSummary; }
        public void setGrowthSummary(String growthSummary) { this.growthSummary = growthSummary; }
    }

    public static class EggInfo {
        private boolean eggLayerRelevant;
        private Integer ageAtFirstEggDays;
        private Integer annualEggCountTarget;
        private Double peakHenDayProductionPct;
        private Double averageEggWeightGrams;
        private String eggShellColor;

        public EggInfo() {
        }

        public EggInfo(boolean eggLayerRelevant, Integer ageAtFirstEggDays, Integer annualEggCountTarget,
                       Double peakHenDayProductionPct, Double averageEggWeightGrams, String eggShellColor) {
            this.eggLayerRelevant = eggLayerRelevant;
            this.ageAtFirstEggDays = ageAtFirstEggDays;
            this.annualEggCountTarget = annualEggCountTarget;
            this.peakHenDayProductionPct = peakHenDayProductionPct;
            this.averageEggWeightGrams = averageEggWeightGrams;
            this.eggShellColor = eggShellColor;
        }

        public boolean isEggLayerRelevant() { return eggLayerRelevant; }
        public void setEggLayerRelevant(boolean eggLayerRelevant) { this.eggLayerRelevant = eggLayerRelevant; }
        public Integer getAgeAtFirstEggDays() { return ageAtFirstEggDays; }
        public void setAgeAtFirstEggDays(Integer ageAtFirstEggDays) { this.ageAtFirstEggDays = ageAtFirstEggDays; }
        public Integer getAnnualEggCountTarget() { return annualEggCountTarget; }
        public void setAnnualEggCountTarget(Integer annualEggCountTarget) { this.annualEggCountTarget = annualEggCountTarget; }
        public Double getPeakHenDayProductionPct() { return peakHenDayProductionPct; }
        public void setPeakHenDayProductionPct(Double peakHenDayProductionPct) { this.peakHenDayProductionPct = peakHenDayProductionPct; }
        public Double getAverageEggWeightGrams() { return averageEggWeightGrams; }
        public void setAverageEggWeightGrams(Double averageEggWeightGrams) { this.averageEggWeightGrams = averageEggWeightGrams; }
        public String getEggShellColor() { return eggShellColor; }
        public void setEggShellColor(String eggShellColor) { this.eggShellColor = eggShellColor; }
    }

    public static class FeedInfo {
        private Integer starterPhaseDaysEnd;
        private Integer growerPhaseDaysEnd;
        private Double dailyAdultFeedGrams;
        private Double cumulativeFeedToMarketKg;
        private boolean freeRangeForagingCapable;
        private String feedScheduleSummary;

        public FeedInfo() {
        }

        public FeedInfo(Integer starterPhaseDaysEnd, Integer growerPhaseDaysEnd, Double dailyAdultFeedGrams,
                        Double cumulativeFeedToMarketKg, boolean freeRangeForagingCapable, String feedScheduleSummary) {
            this.starterPhaseDaysEnd = starterPhaseDaysEnd;
            this.growerPhaseDaysEnd = growerPhaseDaysEnd;
            this.dailyAdultFeedGrams = dailyAdultFeedGrams;
            this.cumulativeFeedToMarketKg = cumulativeFeedToMarketKg;
            this.freeRangeForagingCapable = freeRangeForagingCapable;
            this.feedScheduleSummary = feedScheduleSummary;
        }

        public Integer getStarterPhaseDaysEnd() { return starterPhaseDaysEnd; }
        public void setStarterPhaseDaysEnd(Integer starterPhaseDaysEnd) { this.starterPhaseDaysEnd = starterPhaseDaysEnd; }
        public Integer getGrowerPhaseDaysEnd() { return growerPhaseDaysEnd; }
        public void setGrowerPhaseDaysEnd(Integer growerPhaseDaysEnd) { this.growerPhaseDaysEnd = growerPhaseDaysEnd; }
        public Double getDailyAdultFeedGrams() { return dailyAdultFeedGrams; }
        public void setDailyAdultFeedGrams(Double dailyAdultFeedGrams) { this.dailyAdultFeedGrams = dailyAdultFeedGrams; }
        public Double getCumulativeFeedToMarketKg() { return cumulativeFeedToMarketKg; }
        public void setCumulativeFeedToMarketKg(Double cumulativeFeedToMarketKg) { this.cumulativeFeedToMarketKg = cumulativeFeedToMarketKg; }
        public boolean isFreeRangeForagingCapable() { return freeRangeForagingCapable; }
        public void setFreeRangeForagingCapable(boolean freeRangeForagingCapable) { this.freeRangeForagingCapable = freeRangeForagingCapable; }
        public String getFeedScheduleSummary() { return feedScheduleSummary; }
        public void setFeedScheduleSummary(String feedScheduleSummary) { this.feedScheduleSummary = feedScheduleSummary; }
    }

    public static class PerformanceInfo {
        private Double targetFcr;
        private Double acceptableMortalityPct;
        private String idealTemperatureRangeC;
        private String breedSpecificNotes;

        public PerformanceInfo() {
        }

        public PerformanceInfo(Double targetFcr, Double acceptableMortalityPct,
                               String idealTemperatureRangeC, String breedSpecificNotes) {
            this.targetFcr = targetFcr;
            this.acceptableMortalityPct = acceptableMortalityPct;
            this.idealTemperatureRangeC = idealTemperatureRangeC;
            this.breedSpecificNotes = breedSpecificNotes;
        }

        public Double getTargetFcr() { return targetFcr; }
        public void setTargetFcr(Double targetFcr) { this.targetFcr = targetFcr; }
        public Double getAcceptableMortalityPct() { return acceptableMortalityPct; }
        public void setAcceptableMortalityPct(Double acceptableMortalityPct) { this.acceptableMortalityPct = acceptableMortalityPct; }
        public String getIdealTemperatureRangeC() { return idealTemperatureRangeC; }
        public void setIdealTemperatureRangeC(String idealTemperatureRangeC) { this.idealTemperatureRangeC = idealTemperatureRangeC; }
        public String getBreedSpecificNotes() { return breedSpecificNotes; }
        public void setBreedSpecificNotes(String breedSpecificNotes) { this.breedSpecificNotes = breedSpecificNotes; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBreedCode() { return breedCode; }
    public void setBreedCode(String breedCode) { this.breedCode = breedCode; }
    public String getBreedName() { return breedName; }
    public void setBreedName(String breedName) { this.breedName = breedName; }
    public String getLocalLanguageName() { return localLanguageName; }
    public void setLocalLanguageName(String localLanguageName) { this.localLanguageName = localLanguageName; }
    public Map<String, String> getLocalizedNames() { return localizedNames; }
    public void setLocalizedNames(Map<String, String> localizedNames) { this.localizedNames = localizedNames != null ? localizedNames : new HashMap<>(); }
    public String getBreedCategory() { return breedCategory; }
    public void setBreedCategory(String breedCategory) { this.breedCategory = breedCategory; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getPrimaryPurpose() { return primaryPurpose; }
    public void setPrimaryPurpose(String primaryPurpose) { this.primaryPurpose = primaryPurpose; }
    public Double getPurchasePricePerChick() { return purchasePricePerChick; }
    public void setPurchasePricePerChick(Double purchasePricePerChick) { this.purchasePricePerChick = purchasePricePerChick; }
    public GrowthInfo getExpectedGrowth() { return expectedGrowth; }
    public void setExpectedGrowth(GrowthInfo expectedGrowth) { this.expectedGrowth = expectedGrowth; }
    public EggInfo getEggProduction() { return eggProduction; }
    public void setEggProduction(EggInfo eggProduction) { this.eggProduction = eggProduction; }
    public FeedInfo getFeedRequirements() { return feedRequirements; }
    public void setFeedRequirements(FeedInfo feedRequirements) { this.feedRequirements = feedRequirements; }
    public PerformanceInfo getPerformanceTargets() { return performanceTargets; }
    public void setPerformanceTargets(PerformanceInfo performanceTargets) { this.performanceTargets = performanceTargets; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
