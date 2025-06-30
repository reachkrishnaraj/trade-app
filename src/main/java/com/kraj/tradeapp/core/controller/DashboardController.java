package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.CommonUtil;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.CurrentTradeUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.EventsUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.TradingSignalUI;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeSignalScoreSnapshot;
import com.kraj.tradeapp.core.service.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final NotificationProcessorService notificationProcessorService;
    private final TradeSignalSnapshotProcessor tradeSignalSnapshotProcessor;
    private final TradeAccountConfigService tradeAccountConfigService;
    private final StrategyService strategyService;
    private final MasterConfigService masterConfigService;

    @GetMapping("/current-trades")
    public ResponseEntity<List<CurrentTradeUI>> getCurrentTrades() {
        return ResponseEntity.ok(dashboardService.getCurrentTrades());
    }

    @GetMapping("/events/{symbol}")
    public ResponseEntity<List<NotificationEventDto>> getNotificationEvents(@PathVariable String symbol) {
        List<NotificationEventDto> events = notificationProcessorService.getNotificationEvents(
            StringUtils.upperCase(symbol),
            ZonedDateTime.now().minusHours(24 * 7),
            ZonedDateTime.now()
            //            CommonUtil.getNYLocalDateTimeNow().minusHours(24),
            //            CommonUtil.getNYLocalDateTimeNow()
        );
        return ResponseEntity.ok(events);
    }

    @GetMapping("/trading-signals")
    public ResponseEntity<List<TradingSignalUI>> getTradingSignals() {
        return ResponseEntity.ok(dashboardService.getTradingSignals());
    }

    @GetMapping("/signal-snapshot/{symbol}")
    public ResponseEntity<TradeSignalScoreSnapshot> getSignalSnapshot(@PathVariable String symbol) {
        Optional<TradeSignalScoreSnapshot> maybeSnapshot = tradeSignalSnapshotProcessor.getLatestSnapshot(StringUtils.upperCase(symbol));
        return ResponseEntity.ok(maybeSnapshot.orElseGet(TradeSignalScoreSnapshot::new));
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventsUI>> getEvents() {
        return ResponseEntity.ok(dashboardService.getEvents());
    }

    @GetMapping("/loadTradeAccountConfig")
    public ResponseEntity<String> loadTradeAccountConfig() {
        tradeAccountConfigService.loadTradeAccountConfig();
        return ResponseEntity.ok("Trade Account Config Loaded");
    }

    @GetMapping("/testPickMyTrade/buy/{symbol}")
    public ResponseEntity<?> testPickMyTradeBuy(@PathVariable String symbol) {
        strategyService.byPassAndDoLongTrade(StringUtils.upperCase(symbol), "0");
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/testPickMyTrade/close/{symbol}")
    public ResponseEntity<?> testPickMyTradeClose(@PathVariable String symbol) {
        strategyService.byPassAndDoCloseTrades(StringUtils.upperCase(symbol), "0");
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/testPickMyTrade/sell/{symbol}")
    public ResponseEntity<?> testPickMyTradSell(@PathVariable String symbol) {
        strategyService.byPassAndDoShortTrade(StringUtils.upperCase(symbol), "0");
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/seedMasterConfig")
    public ResponseEntity<String> seedMasterConfig() {
        masterConfigService.seedMasterConfig();
        return ResponseEntity.ok("Master Config Seeded");
    }

    @GetMapping("/kraj/automation/{enable}")
    public ResponseEntity<String> krajAutomation(@PathVariable String enable) {
        if (StringUtils.trim(enable).equalsIgnoreCase("yes")) {
            masterConfigService.enableKrajAutomation();
        } else {
            masterConfigService.disableKrajAutomation();
        }
        return ResponseEntity.ok("Ok");
    }

    @GetMapping("/vivek/automation/{enable}")
    public ResponseEntity<String> vivekAutomation(@PathVariable String enable) {
        if (StringUtils.trim(enable).equalsIgnoreCase("yes")) {
            masterConfigService.enableVivekAutomation();
        } else {
            masterConfigService.disableVivekAutomation();
        }
        return ResponseEntity.ok("Ok");
    }
}
