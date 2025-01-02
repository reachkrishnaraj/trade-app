package com.kraj.tradeapp.core.model;

import org.apache.commons.lang3.StringUtils;

public enum Strategy {
    LILY,
    LOTUS,
    NONE;

    public static Strategy fromString(String strategy) {
        if (strategy == null) {
            return NONE;
        }
        for (Strategy s : Strategy.values()) {
            if (StringUtils.equalsAnyIgnoreCase(s.name(), strategy)) {
                return s;
            }
        }
        return NONE;
    }
}
