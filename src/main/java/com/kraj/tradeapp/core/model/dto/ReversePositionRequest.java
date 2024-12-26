package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class ReversePositionRequest {

    private String instrument;
    private String action; // BUY, SELL
    private int quantity;
    private String orderType; // MARKET, LIMIT, STOPMARKET, STOPLIMIT
    private String timeInForce; // DAY, GTC
    private Float limitPrice; // Optional
    private Float stopPrice; // Optional
    private String ocoId; // Optional
    private String strategy; // Optional
}
