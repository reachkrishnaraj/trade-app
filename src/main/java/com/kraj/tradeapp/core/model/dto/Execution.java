package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class Execution {

    private String id;
    private String type;
    private String time;
    private long epoch;
    private String name;
    private String orderId;
    private String account;
    private String serverName;
    private String instrument;
    private String instrumentType;
    private int position;
    private String marketPosition;
    private int positionStrategy;
    private double price;
    private int quantity;
    private double rate;
    private String commission;
    private double slippage;
    private double lotSize;
    private boolean isEntry;
    private boolean isEntryStrategy;
    private boolean isExit;
    private boolean isExitStrategy;
    private boolean isInitialEntry;
    private boolean isLastExit;
    private boolean isSod;
    private int barsInProgress;
    private String exchange;
    private boolean success;
}
