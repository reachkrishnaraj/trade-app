package com.kraj.tradeapp.core.model.dto;

import lombok.Data;

@Data
public class Account {

    private int id;
    private String name;
    private String displayName;
    private String primaryUser;
    private String provider;
    private String denomination;
    private String status;
    private double dailyLossLimit;
    private double minCashValue;
    private int maxOrderSize;
    private int maxPositionSize;
    private String connection;
    private String connectionStatus;
    private double lastTransactionSum;
    private AccountItem item;
    private boolean success;

    @Data
    public static class AccountItem {

        private double buyingPower;
        private double cashValue;
        private double commission;
        private double dailyLossLimit;
        private double dailyProfitTrigger;
        private double excessInitialMargin;
        private double excessIntradayMargin;
        private double excessMaintenanceMargin;
        private double excessPositionMargin;
        private double fee;
        private double grossRealizedProfitLoss;
        private double initialMargin;
        private double intradayMargin;
        private double longOptionValue;
        private double longStockValue;
        private double lookAheadMaintenanceMargin;
        private double maintenanceMargin;
        private double netLiquidation;
        private double netLiquidationByCurrency;
        private double positionMargin;
        private double realizedProfitLoss;
        private double shortOptionValue;
        private double shortStockValue;
        private double sodCashValue;
        private double sodLiquidatingValue;
        private double totalCashBalance;
        private double trailingMaxDrawdown;
        private double unrealizedProfitLoss;
        private double weeklyLossLimit;
        private double weeklyProfitLoss;
        private double weeklyProfitTrigger;
    }
}
