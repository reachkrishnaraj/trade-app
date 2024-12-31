package com.kraj.tradeapp.core.model.dashboard.ui.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradingSignalUI {

    private String signal;
    private String source;
    private LocalDateTime dateTime;
}
