package com.kraj.tradeapp.core.model.persistance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "acc_pnl_daily")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPNL {

    @Id
    @Column(name = "account_id_date")
    private String accountIdDate;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "profit_loss", nullable = false)
    private BigDecimal profitLoss;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "created")
    private LocalDateTime createdTs;

    @Column(name = "lastupdated")
    private LocalDateTime lastUpdatedTs;
}
