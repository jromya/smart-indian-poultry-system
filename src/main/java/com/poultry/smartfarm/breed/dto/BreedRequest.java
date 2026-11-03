package com.poultry.smartfarm.breed.dto;

import com.poultry.smartfarm.breed.document.Breed;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BreedRequest {

    @NotBlank(message = "Breed Code is required (e.g. IND-ASEEL)")
    private String breedCode;

    @NotBlank(message = "Breed Name is required")
    private String breedName;

    @NotBlank(message = "Local language name is required")
    private String localLanguageName;

    private Map<String, String> localizedNames = new HashMap<>();

    @NotBlank(message = "Breed Category is required (INDIGENOUS, IMPROVED_RURAL, COMMERCIAL)")
    private String breedCategory;

    @NotBlank(message = "Breed Origin is required")
    private String origin;

    @NotBlank(message = "Primary Purpose is required (MEAT, EGG, DUAL_PURPOSE)")
    private String primaryPurpose;

    @NotNull(message = "Purchase price per chick is required")
    @Min(value = 1, message = "Purchase price must be positive")
    private Double purchasePricePerChick;

    private Breed.GrowthInfo expectedGrowth = new Breed.GrowthInfo();
    private Breed.EggInfo eggProduction = new Breed.EggInfo();
    private Breed.FeedInfo feedRequirements = new Breed.FeedInfo();
    private Breed.PerformanceInfo performanceTargets = new Breed.PerformanceInfo();
    private Boolean active = true;

    public String getBreedCode() { return breedCode; }
    public void setBreedCode(String breedCode) { this.breedCode = breedCode; }
    public String getBreedName() { return breedName; }
    public void setBreedName(String breedName) { this.breedName = breedName; }
    public String getLocalLanguageName() { return localLanguageName; }
    public void setLocalLanguageName(String localLanguageName) { this.localLanguageName = localLanguageName; }
    public Map<String, String> getLocalizedNames() { return localizedNames; }
    public void setLocalizedNames(Map<String, String> localizedNames) { this.localizedNames = localizedNames; }
    public String getBreedCategory() { return breedCategory; }
    public void setBreedCategory(String breedCategory) { this.breedCategory = breedCategory; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getPrimaryPurpose() { return primaryPurpose; }
    public void setPrimaryPurpose(String primaryPurpose) { this.primaryPurpose = primaryPurpose; }
    public Double getPurchasePricePerChick() { return purchasePricePerChick; }
    public void setPurchasePricePerChick(Double purchasePricePerChick) { this.purchasePricePerChick = purchasePricePerChick; }
    public Breed.GrowthInfo getExpectedGrowth() { return expectedGrowth; }
    public void setExpectedGrowth(Breed.GrowthInfo expectedGrowth) { this.expectedGrowth = expectedGrowth; }
    public Breed.EggInfo getEggProduction() { return eggProduction; }
    public void setEggProduction(Breed.EggInfo eggProduction) { this.eggProduction = eggProduction; }
    public Breed.FeedInfo getFeedRequirements() { return feedRequirements; }
    public void setFeedRequirements(Breed.FeedInfo feedRequirements) { this.feedRequirements = feedRequirements; }
    public Breed.PerformanceInfo getPerformanceTargets() { return performanceTargets; }
    public void setPerformanceTargets(Breed.PerformanceInfo performanceTargets) { this.performanceTargets = performanceTargets; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
