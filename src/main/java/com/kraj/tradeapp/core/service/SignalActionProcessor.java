package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface SignalActionProcessor {
    /**
     * Create a signal action DTO from external event data
     * PURE FUNCTION - No side effects, just returns DTO
     */
    SignalActionDTO createSignalActionDTO(
        String symbol,
        BigDecimal price,
        String indicator,
        String indicatorDisplayName,
        String interval,
        String alertMessage,
        String direction,
        ZonedDateTime eventTime,
        BigDecimal score,
        boolean isStrategy,
        boolean isAlertable,
        boolean isAnnounce
    );

    /**
     * Get processor type for identification
     */
    String getProcessorType();

    /**
     * Check if processor is enabled
     */
    boolean isEnabled();
}
