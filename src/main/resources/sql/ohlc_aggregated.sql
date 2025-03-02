CREATE TABLE ohlc_aggregated (
    symbol TEXT NOT NULL,
    timeframe TEXT NOT NULL,  -- "5m", "15m", "30m"
    timestamp TIMESTAMPTZ NOT NULL, -- Interval start time
    ny_date_time_id BIGINT NOT NULL, -- YYYYMMDDHHmm format
    open NUMERIC(20,6) NOT NULL,
    high NUMERIC(20,6) NOT NULL,
    low NUMERIC(20,6) NOT NULL,
    close NUMERIC(20,6) NOT NULL,
    PRIMARY KEY (symbol, timeframe, timestamp) -- ✅ Composite primary key
);
CREATE INDEX idx_ohlc_aggregated_symbol_timeframe ON ohlc_aggregated(symbol, timeframe, timestamp);
CREATE INDEX idx_ohlc_aggregated_ny_datetime_id ON ohlc_aggregated(ny_date_time_id);
