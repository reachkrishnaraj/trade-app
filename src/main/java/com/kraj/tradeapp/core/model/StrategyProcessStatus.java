package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public enum StrategyProcessStatus {
    PENDING,
    PROCESSED,
    FAILED,
    IGNORED,
    NA;

    public static StrategyProcessStatus fromString(@Nullable String status) {
        if (StringUtils.isBlank(status)) {
            return NA;
        }
        for (StrategyProcessStatus s : StrategyProcessStatus.values()) {
            if (StringUtils.equalsAnyIgnoreCase(s.name(), status)) {
                return s;
            }
        }
        return NA;
    }
}
