package com.poultry.smartfarm.user.dto;

import jakarta.validation.constraints.NotBlank;

public class LanguageUpdateRequest {

    @NotBlank(message = "Language code is required")
    private String preferredLanguage;

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
}
