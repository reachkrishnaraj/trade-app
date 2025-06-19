package com.kraj.tradeapp.core.model;

import com.kraj.tradeapp.core.model.Quadrant;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "dealing_range_snapshot")
@NoArgsConstructor
@AllArgsConstructor
public class DealingRangeSnapshot {

    @Id
    private String id;

    @Indexed(unique = true)
    private String symbol;

    // Current price information
    private BigDecimal currentPrice;
    private Quadrant currentQuadrant;
    private EventInterval interval;

    // Range boundaries
    private BigDecimal rangeHigh; // 100% level (q5)
    private BigDecimal rangeLow; // 0% level (q1)
    private BigDecimal q1Level; // 75% level (q4)
    private BigDecimal q2Level; // 50% level (q3)
    private BigDecimal q3Level; // 25% level (q2)

    // Metadata
    private String chartTimeframe; // The timeframe used for range calculation
    private ZonedDateTime lastUpdated;
    private ZonedDateTime rangeCalculatedAt;

    // Source information
    private String alertMessage;
    private String source; // e.g., "TV" (TradingView), "WEBHOOK"

    // Additional context
    private Integer lookbackBars; // Number of bars used for range calculation
    private BigDecimal rangeSize; // rangeHigh - rangeLow

    // Quick access methods
    public boolean isInRange() {
        return currentQuadrant != Quadrant.BREACH_BELOW_RANGE && currentQuadrant != Quadrant.BREACH_ABOVE_RANGE;
    }

    public boolean isExtremePosition() {
        return (
            currentQuadrant == Quadrant.Q1_75_100 ||
            currentQuadrant == Quadrant.Q4_0_25 ||
            currentQuadrant == Quadrant.BREACH_ABOVE_RANGE ||
            currentQuadrant == Quadrant.BREACH_ABOVE_RANGE
        );
    }

    public BigDecimal getQuadrantPercentage() {
        if (rangeSize == null || rangeSize.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (currentPrice == null || rangeLow == null) {
            return BigDecimal.ZERO;
        }

        return currentPrice.subtract(rangeLow).divide(rangeSize, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}
