package com.kraj.tradeapp.core.model.dto;

import com.kraj.tradeapp.core.model.EventInterval;
import com.kraj.tradeapp.core.model.Quadrant;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealingRangeHistoryDto {

    private String symbol;
    private BigDecimal currentPrice;
    private Quadrant currentQuadrant;
    private String quadrantDisplayName;
    private BigDecimal quadrantPercentage;

    private BigDecimal rangeHigh;
    private BigDecimal rangeLow;
    private BigDecimal rangeSize;

    private BigDecimal q1Level;
    private BigDecimal q2Level;
    private BigDecimal q3Level;

    private EventInterval interval;
    private ZonedDateTime timestamp;
    private String eventType;

    private boolean isInRange;
    private boolean isExtremePosition;

    private String alertMessage;
    private long minutesAgo;

    // Calculated field
    public long getMinutesAgo() {
        if (timestamp != null) {
            return java.time.Duration.between(timestamp, ZonedDateTime.now()).toMinutes();
        }
        return 0;
    }
}
