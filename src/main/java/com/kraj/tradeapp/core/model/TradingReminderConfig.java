package com.kraj.tradeapp.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingReminderConfig {

    private String frequency;
    private String cronExpr;
    private String message;
}
