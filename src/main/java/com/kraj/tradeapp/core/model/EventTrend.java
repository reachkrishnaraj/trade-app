package com.kraj.tradeapp.core.model;

import org.apache.commons.lang3.StringUtils;

public enum EventTrend {
    UP,
    DOWN,
    UP_TO_DOWN_DIVERGENCE,
    DOWN_TO_UP_DIVERGENCE,
    NEUTRAL;

    public static EventTrend getFromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return EventTrend.NEUTRAL;
        }

        for (EventTrend trend : EventTrend.values()) {
            if (trend.name().equalsIgnoreCase(value)) {
                return trend;
            }
        }
        return EventTrend.NEUTRAL;
    }
}
