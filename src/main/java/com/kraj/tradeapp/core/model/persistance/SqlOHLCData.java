package com.kraj.tradeapp.core.model.persistance;

import com.kraj.tradeapp.core.model.TimeFrame;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "ohlc_data",
    indexes = {
        @Index(name = "idx_ohlc_symbol_timestamp", columnList = "symbol, timestamp"),
        @Index(name = "idx_ohlc_ny_datetime_id", columnList = "ny_date_time_id"),
    }
)
@IdClass(SqlOHLCPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SqlOHLCData {

    @Id
    private String symbol;

    @Id
    @Column(nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant timestamp;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal open;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal high;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal low;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal close;

    @Column(nullable = false, name = "ny_date_time_id")
    private Long nyDateTimeId;
}
