package com.kraj.tradeapp.core.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "computed_trade_signals")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComputedTradeSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(name = "signal_type", nullable = false, length = 20)
    private String signalType;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "lastupdated_ts")
    private LocalDateTime lastUpdated;
}
