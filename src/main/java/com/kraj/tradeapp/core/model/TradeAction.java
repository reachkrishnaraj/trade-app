package com.kraj.tradeapp.core.model;

import org.apache.commons.lang3.StringUtils;

public enum TradeAction {
    ENTER_LONG,
    EXIT_LONG_TAKE_PROFIT,
    EXIT_LONG_STOP_LOSS,
    EXIT_LONG,
    ENTER_SHORT,
    EXIT_SHORT_TAKE_PROFIT,
    EXIT_SHORT_STOP_LOSS,
    EXIT_SHORT,
    NONE;

    public static TradeAction fromString(String action) {
        if (StringUtils.isBlank(action)) {
            return NONE;
        }
        for (TradeAction tradeAction : TradeAction.values()) {
            if (StringUtils.equalsAnyIgnoreCase(tradeAction.name(), action)) {
                return tradeAction;
            }
        }
        return NONE;
    }
}
