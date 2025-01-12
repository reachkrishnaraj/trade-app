package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public enum CandleType {
    CLASSIC,
    HEIKIN_ASHI,
    RENKO_8B,
    RENKO_5B,
    RENKO_4B,
    RENKO_2B,
    RENKO_1B;

    public static CandleType getFromValue(@Nullable String value) {
        if (StringUtils.isBlank(value)) {
            return CLASSIC;
        }
        for (CandleType candleType : CandleType.values()) {
            if (candleType.name().equalsIgnoreCase(value.trim())) {
                return candleType;
            }
        }
        return CandleType.CLASSIC;
    }
}
