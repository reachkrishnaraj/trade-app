package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.CommonUtil;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.CurrentTradeUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.EventsUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.TradingSignalUI;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeSignalScoreSnapshot;
import com.kraj.tradeapp.core.service.DashboardService;
import com.kraj.tradeapp.core.service.NotificationProcessorService;
import com.kraj.tradeapp.core.service.TradeSignalSnapshotProcessor;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final NotificationProcessorService notificationProcessorService;
    private final TradeSignalSnapshotProcessor tradeSignalSnapshotProcessor;

    @GetMapping("/current-trades")
    public ResponseEntity<List<CurrentTradeUI>> getCurrentTrades() {
        return ResponseEntity.ok(dashboardService.getCurrentTrades());
    }

    @GetMapping("/events/{symbol}")
    public ResponseEntity<List<NotificationEventDto>> getNotificationEvents(@PathVariable String symbol) {
        List<NotificationEventDto> events = notificationProcessorService.getNotificationEvents(
            symbol,
            ZonedDateTime.now().minusHours(24),
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
        Optional<TradeSignalScoreSnapshot> maybeSnapshot = tradeSignalSnapshotProcessor.getLatestSnapshot(symbol);
        return ResponseEntity.ok(maybeSnapshot.orElseGet(TradeSignalScoreSnapshot::new));
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventsUI>> getEvents() {
        return ResponseEntity.ok(dashboardService.getEvents());
    }
}
