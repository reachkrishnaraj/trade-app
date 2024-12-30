package com.kraj.tradeapp.core.model.persistance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "acc_trades",
    indexes = {
        @Index(name = "idx_signal_id", columnList = "signal_id"),
        @Index(name = "idx_datetime", columnList = "datetime"),
        @Index(name = "idx_account_id", columnList = "account_id"),
        @Index(name = "idx_symbol", columnList = "symbol"),
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_app_default_seq_gen")
    @SequenceGenerator(name = "trade_app_default_seq_gen", sequenceName = "trade_app_def_seq")
    private Long id;

    @Column(name = "signal_id", nullable = false, length = 100)
    private String signalId;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(name = "account_id", nullable = false, length = 100)
    private String accountId;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "trade_type", length = 10, nullable = false)
    private String tradeType;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "lastupdated_ts", nullable = false)
    private LocalDateTime lastUpdatedTs;
}
