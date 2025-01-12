package com.kraj.tradeapp.core.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationEventDto {

    private Long id;
    private String datetime;
    private String symbol;
    private String source;
    private String indicator;
    private String direction;
    private String indicatorSubCategory;
    private String rawAlertMsg;
    private String rawPayload;
    private BigDecimal price;
    private String interval;
    private String candleType;
    private String created;
    private String lastUpdated;
    private BigDecimal score;
    private boolean isStrategy;
    private String strategyName;
    private String strategyProcessStatus;
    private String strategyProcessedAt;
    private String strategyProcessMsg;
    private Map<String, Object> additionalData;
    private String sinceCreatedStr;
}
