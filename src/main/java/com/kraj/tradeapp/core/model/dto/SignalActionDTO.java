package com.kraj.tradeapp.core.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalActionDTO {

    private Long id;
    private String symbol;
    private BigDecimal price;
    private String signalName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    private SignalStatus status;

    // New fields for enhanced functionality
    private String indicatorName;
    private String interval;
    private String message;
    private SignalDirection direction;
    private boolean isAnnounce;

    public enum SignalStatus {
        PENDING,
        EXECUTED,
        CANCELLED,
    }

    public enum SignalDirection {
        BUY,
        SELL,
        HOLD,
    }
}
