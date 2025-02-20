package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.Indicator;
import com.kraj.tradeapp.core.model.TradeDirection;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeAccountConfig;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeGroupLedger;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final PickMyTradeService pickMyTradeService;
    private final AlertService alertService;
    private final TradeGroupLedgerService tradeGroupLedgerService;
    private final TradeAccountConfigService tradeAccountConfigService;
    private final MasterConfigService masterConfigService;

    private final Map<String, List<String>> CORRELATED_ASSETS_SYMBOL_MAP = new HashMap<String, List<String>>() {
        {
            put("NQ", List.of("ES", "YM", "RTY"));
            put("ES", List.of("NQ", "YM", "RTY"));
            put("YM", List.of("ES", "NQ", "RTY"));
            put("RTY", List.of("ES", "NQ", "YM"));
        }
    };

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

        Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap = tradeAccountConfigService.getTradeAccountConfigGroupedByTradeGroup(
            symbol
        );
        if (closeConditions.contains(rawMsg.trim().toUpperCase())) {
            pickMyTradeService.placeCloseOrders(symbol, price, trdGrpAndAccountsMap);
            tradeGroupLedgerService.closeOpenTradesForTradeGrpAndParentSymbol(trdGrpAndAccountsMap.keySet(), symbol);
            alertService.sendTelegramMessage("Close order placed for symbol: %s at price: %s".formatted(symbol, price));
            return;
        }

        if (!masterConfigService.isAllAutomationEnabled()) {
            alertService.sendTelegramMessage("All automation is disabled, cannot place trades");
            return;
        }

        if (StringUtils.equalsIgnoreCase(rawMsg, "LONG_ENTRY")) {
            Map<String, List<TradeAccountConfig>> longFilteredAccConfig = filterTradeAccConfigForOpenTrades(
                symbol,
                trdGrpAndAccountsMap,
                TradeDirection.LONG,
                TradeDirection.SHORT
            );
            if (longFilteredAccConfig.isEmpty()) {
                alertService.sendTelegramMessage(
                    "Long entry received for symbol: %s but opposite trades are open for other symbol(s)".formatted(symbol)
                );
                return;
            }
            pickMyTradeService.placeBuyOrders(symbol, price, longFilteredAccConfig);
            tradeGroupLedgerService.createTradeGrpLedgerRecord(longFilteredAccConfig, TradeDirection.LONG, event.getPrice());
            alertService.sendTelegramMessage("Buy order placed for symbol: %s at price: %s".formatted(symbol, price));
            return;
        }

        if (StringUtils.equalsIgnoreCase(rawMsg, "SHORT_ENTRY")) {
            Map<String, List<TradeAccountConfig>> shortFilteredAccConfig = filterTradeAccConfigForOpenTrades(
                symbol,
                trdGrpAndAccountsMap,
                TradeDirection.SHORT,
                TradeDirection.LONG
            );
            if (shortFilteredAccConfig.isEmpty()) {
                alertService.sendTelegramMessage(
                    "Short entry received for symbol: %s but opposite trades are open for other symbol(s)".formatted(symbol)
                );
                return;
            }
            pickMyTradeService.placeSellOrders(symbol, price, shortFilteredAccConfig);
            tradeGroupLedgerService.createTradeGrpLedgerRecord(shortFilteredAccConfig, TradeDirection.SHORT, event.getPrice());
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

    public void byPassAndDoLongTrade(String symbol, String price) {
        Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap = tradeAccountConfigService.getTradeAccountConfigGroupedByTradeGroup(
            symbol
        );
        Map<String, List<TradeAccountConfig>> longFilteredAccConfig = filterTradeAccConfigForOpenTrades(
            symbol,
            trdGrpAndAccountsMap,
            TradeDirection.LONG,
            TradeDirection.SHORT
        );
        if (longFilteredAccConfig.isEmpty()) {
            alertService.sendTelegramMessage(
                "Long entry received for symbol: %s but opposite trades are open for other symbol(s)".formatted(symbol)
            );
            return;
        }
        pickMyTradeService.placeBuyOrders(symbol, price, longFilteredAccConfig);
        tradeGroupLedgerService.createTradeGrpLedgerRecord(longFilteredAccConfig, TradeDirection.LONG, new BigDecimal(price));
        alertService.sendTelegramMessage("Buy order placed for symbol: %s at price: %s".formatted(symbol, price));
    }

    public void byPassAndDoShortTrade(String symbol, String price) {
        Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap = tradeAccountConfigService.getTradeAccountConfigGroupedByTradeGroup(
            symbol
        );
        Map<String, List<TradeAccountConfig>> longFilteredAccConfig = filterTradeAccConfigForOpenTrades(
            symbol,
            trdGrpAndAccountsMap,
            TradeDirection.SHORT,
            TradeDirection.LONG
        );
        if (longFilteredAccConfig.isEmpty()) {
            alertService.sendTelegramMessage(
                "Short entry received for symbol: %s but opposite trades are open for other symbol(s)".formatted(symbol)
            );
            return;
        }
        pickMyTradeService.placeSellOrders(symbol, price, longFilteredAccConfig);
        tradeGroupLedgerService.createTradeGrpLedgerRecord(longFilteredAccConfig, TradeDirection.LONG, new BigDecimal(price));
        alertService.sendTelegramMessage("Sell order placed for symbol: %s at price: %s".formatted(symbol, price));
    }

    public void byPassAndDoCloseTrades(String symbol, String price) {
        Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap = tradeAccountConfigService.getTradeAccountConfigGroupedByTradeGroup(
            symbol
        );
        pickMyTradeService.placeCloseOrders(symbol, price, trdGrpAndAccountsMap);
        tradeGroupLedgerService.closeOpenTradesForTradeGrpAndParentSymbol(trdGrpAndAccountsMap.keySet(), symbol);
        alertService.sendTelegramMessage("Close order placed for symbol: %s at price: %s".formatted(symbol, price));
    }

    private Map<String, List<TradeAccountConfig>> filterTradeAccConfigForOpenTrades(
        String entryTradeSymbol,
        Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap,
        TradeDirection entryTradeDirection,
        TradeDirection oppositeDirection
    ) {
        Map<String, List<TradeAccountConfig>> filtered = new HashMap<>();

        List<String> correlatedAssets = CORRELATED_ASSETS_SYMBOL_MAP.get(entryTradeSymbol);

        for (String trdGrp : trdGrpAndAccountsMap.keySet()) {
            boolean isOppositeTradeOpen = false;
            boolean isSameDirectionAlreadyTradeOpen = false;

            //ensure no existing open trades for same direction
            List<TradeGroupLedger> currOpenTradeForSameDirection = tradeGroupLedgerService.getOpenRecordsFor(
                trdGrp,
                entryTradeSymbol,
                entryTradeDirection
            );

            if (!currOpenTradeForSameDirection.isEmpty()) {
                isSameDirectionAlreadyTradeOpen = true;
            }

            for (String eachCorrAsset : correlatedAssets) {
                List<TradeGroupLedger> openRecords = tradeGroupLedgerService.getOpenRecordsFor(trdGrp, eachCorrAsset, oppositeDirection);
                if (!openRecords.isEmpty()) {
                    String openRecordsMsg = openRecords
                        .stream()
                        .map(
                            rec ->
                                "grpName:%s, sym:%s, dir:%s|".formatted(
                                        rec.getTradeAccGroupName(),
                                        rec.getSymbol(),
                                        rec.getTradeDirection().name()
                                    )
                        )
                        .toList()
                        .toString();
                    alertService.sendTelegramMessage(
                        "Received %s for %s, but opposite trades are open for symbol: %s, %s".formatted(
                                entryTradeDirection.name(),
                                entryTradeSymbol,
                                eachCorrAsset,
                                openRecordsMsg
                            )
                    );
                    isOppositeTradeOpen = true;
                }
            }
            if (!isOppositeTradeOpen && !isSameDirectionAlreadyTradeOpen) {
                filtered.put(trdGrp, trdGrpAndAccountsMap.get(trdGrp));
            }
        }
        return filtered;
    }
}
