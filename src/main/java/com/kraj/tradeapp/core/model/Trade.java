package com.kraj.tradeapp.core.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "acc_trades")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(name = "account_id", nullable = false, length = 20)
    private String accountId;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "trade_type", length = 10)
    private String tradeType;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "lastupdated_ts")
    private LocalDateTime lastUpdatedTs;
}
