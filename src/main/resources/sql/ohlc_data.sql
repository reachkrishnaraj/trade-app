CREATE TABLE ohlc_data (
    symbol TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    ny_date_time_id BIGINT NOT NULL,
    open NUMERIC(20,6) NOT NULL,
    high NUMERIC(20,6) NOT NULL,
    low NUMERIC(20,6) NOT NULL,
    close NUMERIC(20,6) NOT NULL,
    PRIMARY KEY (symbol, timestamp)  -- ✅ Composite Primary Key
);
CREATE INDEX idx_ohlc_symbol_timestamp ON ohlc_data(symbol, timestamp);
CREATE INDEX idx_ohlc_ny_datetime_id ON ohlc_data(ny_date_time_id);
