package com.kraj.tradeapp.core.model.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TradeSignalDto {

    private String indicator;
    private String symbol;
    private String direction;
    private String signal;
    private String status;
    private String createdTs;
    private BigDecimal price;
    private String source;
    private String strategy;
    private String sinceCreatedStr;
}
