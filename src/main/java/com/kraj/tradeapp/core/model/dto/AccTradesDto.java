package com.kraj.tradeapp.core.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccTradesDto {

    private String accountName;
    private String symbol;
    private String direction;
    private String status;
    private String price;
    private String openedDatetime;
    private String closedDatetime;
}
