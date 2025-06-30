package com.kraj.tradeapp.core.model.dto;

import com.kraj.tradeapp.core.model.EventInterval;
import com.kraj.tradeapp.core.model.Quadrant;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DealingRangeDto {

    private String symbol;
    private BigDecimal currentPrice;
    private Quadrant currentQuadrant;
    private String quadrantDisplayName;
    private BigDecimal quadrantPercentage;

    // Range levels
    private BigDecimal rangeHigh;
    private BigDecimal rangeLow;
    private BigDecimal q1Level;
    private BigDecimal q2Level;
    private BigDecimal q3Level;

    private EventInterval interval;

    // Metadata
    private String chartTimeframe;
    private ZonedDateTime lastUpdated;
    private boolean isInRange;
    private boolean isExtremePosition;
    private BigDecimal rangeSize;
    private String alertMessage;

    // New fields for enhanced tracking
    private boolean quadrantChanged;
    private int minutesInCurrentQuadrant;
    private Quadrant previousQuadrant;
    private ZonedDateTime lastQuadrantChange;

    // Helper methods
    public String getQuadrantDisplayText() {
        if (currentQuadrant != null) {
            return currentQuadrant.getDisplayName() + " (" + currentQuadrant.name() + ")";
        }
        return "Unknown";
    }

    public String getRangeText() {
        if (rangeLow != null && rangeHigh != null) {
            return rangeLow.toString() + " - " + rangeHigh.toString();
        }
        return "Unknown Range";
    }

    public boolean isBreached() {
        return currentQuadrant == Quadrant.BREACH_ABOVE_RANGE || currentQuadrant == Quadrant.BREACH_BELOW_RANGE;
    }
}
