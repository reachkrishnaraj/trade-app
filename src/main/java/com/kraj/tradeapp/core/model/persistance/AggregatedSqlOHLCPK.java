package com.kraj.tradeapp.core.model.persistance;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class AggregatedSqlOHLCPK implements Serializable {

    private String symbol;
    private String timeframe;
    private Instant timestamp;

    public AggregatedSqlOHLCPK() {}

    public AggregatedSqlOHLCPK(String symbol, String timeframe, Instant timestamp) {
        this.symbol = symbol;
        this.timeframe = timeframe;
        this.timestamp = timestamp;
    }

    // Getters, Setters, equals(), hashCode()
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregatedSqlOHLCPK that = (AggregatedSqlOHLCPK) o;
        return (
            Objects.equals(symbol, that.symbol) && Objects.equals(timeframe, that.timeframe) && Objects.equals(timestamp, that.timestamp)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, timeframe, timestamp);
    }
}
