package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public enum PayloadKey {
    INDICATOR_NAME("indicator"),
    PRICE("price"),
    TIME("time"),
    STRATEGY("strategyName"),
    SYMBOL("symbol"),
    SOURCE("source"),
    ALERT_MESSAGE("msg"),
    INTERVAL("interval"),
    CANDLE_TYPE("candleType"),
    IS_STRATEGY("isStrategy"),
    UNKNOWN("unknown");

    private final String keyName;

    PayloadKey(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public static PayloadKey fromString(String key) {
        if (StringUtils.isBlank(key)) {
            return UNKNOWN;
        }

        for (PayloadKey payloadKey : PayloadKey.values()) {
            if (payloadKey.getKeyName().equalsIgnoreCase(key)) {
                return payloadKey;
            }
        }
        return UNKNOWN;
    }
}
