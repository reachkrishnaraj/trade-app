package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class Order {

    private String account;
    private String orderId;
    private String instrument;
    private String instrumentType;
    private String orderType;
    private String orderState;
    private int quantity;
    private double averageFillPrice;
    private int filled;
    private String fromEntrySignal;
    private String gtd;
    private boolean hasOverfill;
    private boolean isBacktestOrder;
    private double limitPrice;
    private double limitPriceChanged;
    private String ocoId;
    private String orderAction;
    private int quantityChanged;
    private double stopPrice;
    private double stopPriceChanged;
    private String time;
    private long epoch;
    private String timeInForce;
    private String name;
    private boolean isLimit;
    private boolean isLiveUntilCancelled;
    private boolean isLong;
    private boolean isMarket;
    private boolean isMarketIfTouched;
    private boolean isShort;
    private boolean isSimulatedStop;
    private boolean isStopLimit;
    private boolean isStopMarket;
    private boolean isTrackingConfigured;
    private boolean isTrackingEnabled;
    private String userData;
    private OwnerStrategy ownerStrategy;
    private boolean success;

    @Data
    public static class OwnerStrategy {

        private String id;
        private String name;
        private String displayName;
    }
}
