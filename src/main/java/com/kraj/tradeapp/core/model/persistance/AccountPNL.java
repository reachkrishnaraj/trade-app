package com.kraj.tradeapp.core.model.persistance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "acc_pnl_daily",
    indexes = { @Index(name = "idx_account_id", columnList = "account_id"), @Index(name = "idx_date", columnList = "date") }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPNL {

    @Id
    @Column(name = "account_id_date", nullable = false)
    private String accountIdDate;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "profit_loss", nullable = false)
    private BigDecimal profitLoss;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "created_ts", nullable = false)
    private ZonedDateTime createdTs;

    @Column(name = "lastupdated_ts", nullable = false)
    private ZonedDateTime lastUpdatedTs;
}
