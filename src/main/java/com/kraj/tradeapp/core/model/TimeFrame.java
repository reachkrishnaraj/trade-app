package com.kraj.tradeapp.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;

public enum TimeFrame {
    S30("30_SECOND"),
    M1("1_MINUTE"),
    M5("5_MINUTE"),
    M15("15_MINUTE"),
    M30("30_MINUTE"),
    M90("90_MINUTE"),
    H1("1_HOUR"),
    H4("4_HOUR"),
    D1("1_DAY"),
    W1("1_WEEK");

    private String name;

    TimeFrame(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @JsonCreator
    @Nullable
    public static TimeFrame fromString(String name) {
        for (TimeFrame timeFrame : TimeFrame.values()) {
            if (timeFrame.name.equalsIgnoreCase(name)) {
                return timeFrame;
            }
        }
        return null;
    }
}
