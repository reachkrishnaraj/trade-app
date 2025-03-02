package com.kraj.tradeapp.core.model.persistance;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class SqlOHLCPK implements Serializable {

    private String symbol;
    private Instant timestamp;

    public SqlOHLCPK() {}

    public SqlOHLCPK(String symbol, Instant timestamp) {
        this.symbol = symbol;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlOHLCPK that = (SqlOHLCPK) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, timestamp);
    }
}
