package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import org.apache.commons.lang3.StringUtils;

public enum EventInterval {
    S1("1s"),
    S30("30s"),
    M1("1m"),
    M2("2m"),
    M3("3m"),
    M5("5m"),
    M15("15m"),
    M30("30m"),
    H1("1h"),
    H4("4h"),
    H7("7h"),
    D1("1d"),
    W1("1w"),
    NA("NA"),
    ANY("ANY");

    private final String value;

    EventInterval(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EventInterval getFromValue(@Nullable String value) {
        for (EventInterval interval : EventInterval.values()) {
            if (StringUtils.equalsAnyIgnoreCase(interval.getValue(), value)) {
                return interval;
            }
        }
        return EventInterval.NA;
    }
}
