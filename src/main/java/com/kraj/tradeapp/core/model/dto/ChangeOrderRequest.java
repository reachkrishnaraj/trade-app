package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class ChangeOrderRequest {

    private Integer quantity; // Optional
    private Float limitPrice; // Optional
    private Float stopPrice; // Optional
    private String strategyId; // Optional
}
