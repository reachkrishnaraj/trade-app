package com.kraj.tradeapp.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;

public enum TradeStatus {
    OPEN,
    CLOSED;

    @JsonCreator
    @Nullable
    public static TradeStatus fromString(String value) {
        for (TradeStatus status : TradeStatus.values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
