package com.kraj.tradeapp.core.model.dashboard.ui.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentTradeUI {

    private String accountName;
    private Long id;
    private BigDecimal openPnL;
    private LocalDateTime tradeOpenTime;
    private String status;
}
