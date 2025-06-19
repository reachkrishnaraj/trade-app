package com.kraj.tradeapp.core.model;

public enum Quadrant {
    Q1_75_100("Q1 (75-100%)", "Top quadrant"),
    Q2_50_75("Q2 (50-75%)", "Upper middle quadrant"),
    Q3_25_50("Q3 (25-50%)", "Lower middle quadrant"),
    Q4_0_25("Q4 (0-25%)", "Bottom quadrant"),
    BELOW_RANGE("Below Range", "Price below 0% range"),
    ABOVE_RANGE("Above Range", "Price above 100% range"),
    UNKNOWN("Unknown", "Could not determine quadrant");

    private final String displayName;
    private final String description;

    Quadrant(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static Quadrant fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNKNOWN;
        }

        String normalized = value.toUpperCase().trim();

        // Handle various formats from Pine Script alerts
        if (normalized.contains("Q1") || (normalized.contains("75") && normalized.contains("100"))) {
            return Q1_75_100;
        } else if (normalized.contains("Q2") || (normalized.contains("50") && normalized.contains("75"))) {
            return Q2_50_75;
        } else if (normalized.contains("Q3") || (normalized.contains("25") && normalized.contains("50"))) {
            return Q3_25_50;
        } else if (normalized.contains("Q4") || (normalized.contains("0") && normalized.contains("25"))) {
            return Q4_0_25;
        } else if (normalized.contains("BELOW") || normalized.contains("UNDER")) {
            return BELOW_RANGE;
        } else if (normalized.contains("ABOVE") || normalized.contains("OVER")) {
            return ABOVE_RANGE;
        }

        // Try exact enum name matching
        try {
            return Quadrant.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
