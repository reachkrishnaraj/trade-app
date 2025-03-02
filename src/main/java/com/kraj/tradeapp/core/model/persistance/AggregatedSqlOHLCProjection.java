package com.kraj.tradeapp.core.model.persistance;

import java.math.BigDecimal;
import java.time.Instant;

public interface AggregatedSqlOHLCProjection {
    Instant getTimestamp();
    String getSymbol();
    String getTimeframe();
    Long getNyDateTimeId();
    BigDecimal getOpen();
    BigDecimal getHigh();
    BigDecimal getLow();
    BigDecimal getClose();
}
