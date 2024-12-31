package com.kraj.tradeapp.core.model.persistance;

import com.kraj.tradeapp.core.model.TradeAction;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "trade_signals",
    indexes = { @Index(name = "idx_datetime", columnList = "datetime"), @Index(name = "idx_symbol", columnList = "symbol") }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_app_default_seq_gen")
    @SequenceGenerator(name = "trade_app_default_seq_gen", sequenceName = "trade_app_def_seq")
    private Long id;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(nullable = false, length = 50)
    private String direction;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "lastupdated_ts", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(nullable = false, name = "trade_action")
    private String tradeAction;

    @Column(nullable = false)
    private String status;

    @Column(name = "processed_at_ts", nullable = false)
    private LocalDateTime processedAt;
}
