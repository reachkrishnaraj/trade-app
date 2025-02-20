package com.kraj.tradeapp.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

public enum TradeDirection {
    LONG(new String[] { "LONG_ENTRY" }),
    SHORT(new String[] { "SHORT_ENTRY" });

    private String[] possibleValues;

    TradeDirection(String[] possibleValues) {
        this.possibleValues = possibleValues;
    }

    public String[] getPossibleValues() {
        return possibleValues;
    }

    @JsonCreator
    public static TradeDirection fromString(String value) {
        for (TradeDirection direction : TradeDirection.values()) {
            for (String possibleValue : direction.getPossibleValues()) {
                if (possibleValue.equals(value)) {
                    return direction;
                }
            }
        }
        return null;
    }
}
