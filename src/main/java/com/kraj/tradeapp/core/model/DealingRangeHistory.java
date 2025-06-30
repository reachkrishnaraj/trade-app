// DealingRangeHistory.java
package com.kraj.tradeapp.core.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dealing_range_history")
@CompoundIndexes(
    {
        @CompoundIndex(name = "symbol_interval_timestamp", def = "{'symbol': 1, 'interval': 1, 'timestamp': -1}"),
        @CompoundIndex(name = "symbol_timestamp", def = "{'symbol': 1, 'timestamp': -1}"),
        @CompoundIndex(name = "interval_eventType_timestamp", def = "{'interval': 1, 'eventType': 1, 'timestamp': -1}"),
        @CompoundIndex(name = "currentQuadrant_interval_timestamp", def = "{'currentQuadrant': 1, 'interval': 1, 'timestamp': -1}"),
    }
)
public class DealingRangeHistory {

    @Id
    private String id;

    @Indexed
    private String symbol;

    private BigDecimal currentPrice;

    @Indexed
    private Quadrant currentQuadrant;

    private BigDecimal rangeHigh;
    private BigDecimal rangeLow;
    private BigDecimal rangeSize;

    // Quadrant boundary levels
    private BigDecimal q1Level; // 75% level
    private BigDecimal q2Level; // 50% level
    private BigDecimal q3Level; // 25% level

    private String chartTimeframe;

    @Indexed
    private EventInterval interval;

    @Indexed
    private ZonedDateTime timestamp;

    @Indexed
    private String eventType; // QUADRANT_CHANGE, PRICE_UPDATE, RANGE_UPDATE

    private String alertMessage;
    private String source;
    private Integer lookbackBars;

    // Calculated fields
    public BigDecimal getQuadrantPercentage() {
        if (rangeSize == null || rangeSize.compareTo(BigDecimal.ZERO) == 0 || currentPrice == null || rangeLow == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(rangeLow).divide(rangeSize, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public boolean isInRange() {
        return currentQuadrant != Quadrant.BREACH_ABOVE_RANGE && currentQuadrant != Quadrant.BREACH_BELOW_RANGE;
    }

    public boolean isExtremePosition() {
        return (
            currentQuadrant == Quadrant.BREACH_ABOVE_RANGE ||
            currentQuadrant == Quadrant.BREACH_BELOW_RANGE ||
            currentQuadrant == Quadrant.Q1_75_100 ||
            currentQuadrant == Quadrant.Q4_0_25
        );
    }
}
