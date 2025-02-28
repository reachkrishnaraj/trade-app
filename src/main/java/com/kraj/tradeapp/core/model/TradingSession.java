package com.kraj.tradeapp.core.model;

public enum TradingSession {
    ASIA("Asia", 20, 0, 23, 59),
    LONDON("London", 2, 0, 4, 59),
    NEW_YORK_AM("New York AM", 9, 30, 11, 59),
    NEW_YORK_LUNCH("New York Lunch", 12, 0, 13, 59);

    private String name;
    private int startHr;
    private int startMin;
    private int endHr;
    private int endMin;

    TradingSession(String name, int startHr, int startMin, int endHr, int endMin) {
        this.name = name;
        this.startHr = startHr;
        this.startMin = startMin;
        this.endHr = endHr;
        this.endMin = endMin;
    }
}
