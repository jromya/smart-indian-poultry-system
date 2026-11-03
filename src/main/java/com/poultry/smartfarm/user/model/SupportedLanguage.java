package com.poultry.smartfarm.user.model;

import java.util.Map;
import java.util.Set;

public final class SupportedLanguage {

    public static final Map<String, String> LANGUAGES = Map.ofEntries(
            Map.entry("en", "English"),
            Map.entry("mr", "मराठी (Marathi)"),
            Map.entry("hi", "हिन्दी (Hindi)"),
            Map.entry("gu", "ગુજરાતી (Gujarati)"),
            Map.entry("pa", "ਪੰਜਾਬੀ (Punjabi)"),
            Map.entry("bn", "বাংলা (Bengali)"),
            Map.entry("te", "తెలుగు (Telugu)"),
            Map.entry("kn", "ಕನ್ನಡ (Kannada)"),
            Map.entry("ta", "தமிழ் (Tamil)"),
            Map.entry("ml", "മലയാളം (Malayalam)"),
            Map.entry("or", "ଓଡ଼ିଆ (Odia)")
    );

    public static final Set<String> CODES = LANGUAGES.keySet();

    private SupportedLanguage() {
    }

    public static String normalizeOrDefault(String langCode) {
        if (langCode == null || langCode.isBlank()) {
            return "en";
        }
        String normalized = langCode.trim().toLowerCase();
        return CODES.contains(normalized) ? normalized : "en";
    }
}
