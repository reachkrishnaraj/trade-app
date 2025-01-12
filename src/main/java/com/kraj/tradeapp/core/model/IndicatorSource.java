package com.kraj.tradeapp.core.model;

public enum IndicatorSource {
    TRADING_VIEW("TV"),
    ATAS("ATAS"),
    QUANTVUE("QV"),
    UNKNOWN("UNKNOWN");

    private final String sourceName;

    IndicatorSource(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public static IndicatorSource fromString(String sourceName) {
        for (IndicatorSource source : IndicatorSource.values()) {
            if (source.sourceName.equalsIgnoreCase(sourceName) || source.name().equalsIgnoreCase(sourceName)) {
                return source;
            }
        }
        return UNKNOWN;
    }
}
