package com.kraj.tradeapp.core.model;

import org.apache.commons.lang3.StringUtils;

public enum AlertMessageMatchType {
    STARTS_WITH,
    FULL_MATCH,
    JAVA_REGEX,
    CONTAINS,
    UNKNOWN;

    public static AlertMessageMatchType fromString(String matchType) {
        if (StringUtils.isBlank(matchType)) {
            return UNKNOWN;
        }
        for (AlertMessageMatchType alertMessageMatchType : AlertMessageMatchType.values()) {
            if (alertMessageMatchType.name().equalsIgnoreCase(matchType.trim())) {
                return alertMessageMatchType;
            }
        }
        return UNKNOWN;
    }
}
