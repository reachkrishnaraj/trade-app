package com.kraj.tradeapp.core.model.dashboard.ui.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventsUI {

    private String indicatorName;
    private String rawMessage;
    private LocalDateTime dateTime;
    private String signal;
    private String symbol;
}
