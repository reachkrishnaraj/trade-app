package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class PlaceOrderRequest {

    private String instrument; // Required
    private String action; // Required: BUY, SELL
    private int quantity; // Required
    private String orderType; // Required: MARKET, LIMIT, STOPMARKET, STOPLIMIT
    private String timeInForce; // Required: DAY, GTC
    private Float limitPrice; // Optional
    private Float stopPrice; // Optional
    private String ocoId; // Optional
    private String strategy; // Optional
}
