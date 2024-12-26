package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class Position {

    private String type;
    private String account;
    private String instrument;
    private String instrumentType;
    private String marketPosition;
    private int quantity;
    private double averagePrice;
    private double marketPrice;
    private double unrealizedProfitLoss;
    private boolean success;
}
