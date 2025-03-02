package com.kraj.tradeapp.core.model.persistance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "ohlc_aggregated",
    indexes = {
        @Index(name = "idx_ohlc_aggregated_symbol_timeframe", columnList = "symbol, timeframe, timestamp"),
        @Index(name = "idx_ohlc_aggregated_ny_datetime_id", columnList = "ny_date_time_id"),
    }
)
@IdClass(AggregatedSqlOHLCPK.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AggregatedSqlOHLC {

    @Id
    private String symbol;

    @Id
    private String timeframe;

    @Id
    private Instant timestamp;

    @Column(nullable = false, name = "ny_date_time_id")
    private Long nyDateTimeId;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal open;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal high;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal low;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal close;
}
