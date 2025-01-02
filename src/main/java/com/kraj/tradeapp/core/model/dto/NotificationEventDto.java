package com.kraj.tradeapp.core.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationEventDto {

    private Long id;

    private LocalDateTime datetime;

    private String symbol;

    private String source;

    private String indicator;

    private String derivedValue;

    private String direction;

    private String category;

    private String rawMsg;

    private BigDecimal price;

    private String interval;

    private LocalDateTime created;

    private LocalDateTime lastUpdated;

    private boolean strategy;

    private String importance;

    private String sinceCreatedStr;

    private String tradeAction;
}
