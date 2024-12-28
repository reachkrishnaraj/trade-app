package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class Quote {

    private String instrument;
    private String description;
    private String type;
    private double bid;
    private double ask;
    private double last;
    private double open;
    private double high;
    private double low;
    private double close;
    private int volume;
    private double tickSize;
    private double pointValue;
    private String exchange;
    private String expiration;
    private String tradingHours;
    private String quoteTime;
    private boolean success;
}
