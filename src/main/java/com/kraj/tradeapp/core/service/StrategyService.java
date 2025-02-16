package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.Indicator;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final PickMyTradeService pickMyTradeService;
    private final AlertService alertService;

    public void handleStrategyEvent(NotificationEvent event) {
        Indicator indicator = Indicator.fromString(event.getIndicator());
        switch (indicator) {
            case QUANTVUE_QKRONOS -> handle_QKronosStrategyEvent(event);
            default -> {
                alertService.sendTelegramMessage("Invalid strategy:%s, cannot handle".formatted(event.getIndicator()));
                throw new IllegalArgumentException("Invalid indicator: %s".formatted(event.getIndicator()));
            }
        }
    }

    public void handle_QKronosStrategyEvent(NotificationEvent event) {
        if (!StringUtils.equalsIgnoreCase(event.getIndicator(), Indicator.QUANTVUE_QKRONOS.name())) {
            alertService.sendTelegramMessage("Invalid indicator for QKronos strategy: %s".formatted(event.getIndicator()));
            throw new IllegalArgumentException("Invalid indicator for QKronos strategy: %s".formatted(event.getIndicator()));
        }
        String symbol = event.getSymbol();
        String rawMsg = event.getRawAlertMsg();
        String price = event.getPrice().toString();
        List<String> closeConditions = List.of(
            "REGULAR_LONG_EXIT",
            "REGULAR_SHORT_EXIT",
            "END_OF_DAY_EXIT",
            "PROFIT_LOSS_TRADE_LIMIT_CLOSE"
        );
        if (closeConditions.contains(rawMsg.trim().toUpperCase())) {
            pickMyTradeService.placeCloseOrders(symbol, price);
            alertService.sendTelegramMessage("Close order placed for symbol: %s at price: %s".formatted(symbol, price));
            return;
        }

        if (StringUtils.equalsIgnoreCase(rawMsg, "LONG_ENTRY")) {
            pickMyTradeService.placeBuyOrders(symbol, price);
            alertService.sendTelegramMessage("Buy order placed for symbol: %s at price: %s".formatted(symbol, price));
            return;
        }

        if (StringUtils.equalsIgnoreCase(rawMsg, "SHORT_ENTRY")) {
            pickMyTradeService.placeSellOrders(symbol, price);
            alertService.sendTelegramMessage("Sell order placed for symbol: %s at price: %s".formatted(symbol, price));
            return;
        }
        alertService.sendTelegramMessage("Invalid message for QKronos strategy: %s".formatted(rawMsg));
        throw new IllegalArgumentException("Invalid message for QKronos strategy: %s".formatted(rawMsg));
    }
    //ICEBERG_QUANTVUE
    /*
    Parameters: Main Chart
    - QLINE
    - QGRID
    - QCLOUD
    - QWAVE
    - QMoneyball
    - QMomentum
    - QBands
    - SUPERTREND
    - ALGO Alpha Trend
     - Pivot point ST
    - Luxalgo S & O Confirmation

    Support chart:
    - 3M Oscillator
    - 3M Swing BoS/ChoCh
    - 3M pivot point ST
    - 3M Luxalgo S & O Confirmation
    - Bj key levels
     Long conditions:
        1

     */
}
