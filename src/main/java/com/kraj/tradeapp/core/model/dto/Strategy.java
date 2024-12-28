package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class Strategy {

    private String id;
    private String account;
    private String strategyName;
    private String displayName;
    private String position;
    private int quantity;
    private double averagePrice;
    private double unrealizedPnl;
    private boolean success;
}
