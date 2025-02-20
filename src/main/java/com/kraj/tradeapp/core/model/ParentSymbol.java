package com.kraj.tradeapp.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;

public enum ParentSymbol {
    NQ,
    ES,
    YM,
    RTY,
    GC,
    CL;

    @JsonCreator
    @Nullable
    public static ParentSymbol fromString(String value) {
        for (ParentSymbol symbol : ParentSymbol.values()) {
            if (symbol.name().equals(value)) {
                return symbol;
            }
        }
        return null;
    }
}
