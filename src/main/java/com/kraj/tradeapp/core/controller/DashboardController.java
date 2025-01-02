package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.dashboard.ui.dto.CurrentTradeUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.EventsUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.TradingSignalUI;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import com.kraj.tradeapp.core.service.DashboardService;
import com.kraj.tradeapp.core.service.NotificationProcessorService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final NotificationProcessorService notificationProcessorService;

    @GetMapping("/current-trades")
    public ResponseEntity<List<CurrentTradeUI>> getCurrentTrades() {
        return ResponseEntity.ok(dashboardService.getCurrentTrades());
    }

    @GetMapping("/events/{symbol}")
    public ResponseEntity<List<NotificationEventDto>> getNotificationEvents(@PathVariable String symbol) {
        List<NotificationEventDto> events = notificationProcessorService.getNotificationEvents(
            symbol,
            LocalDateTime.now().minusHours(24),
            LocalDateTime.now()
        );
        return ResponseEntity.ok(events);
    }

    @GetMapping("/trading-signals")
    public ResponseEntity<List<TradingSignalUI>> getTradingSignals() {
        return ResponseEntity.ok(dashboardService.getTradingSignals());
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventsUI>> getEvents() {
        return ResponseEntity.ok(dashboardService.getEvents());
    }
}
