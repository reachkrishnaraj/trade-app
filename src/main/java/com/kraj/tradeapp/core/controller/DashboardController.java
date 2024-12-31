package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.dashboard.ui.dto.CurrentTradeUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.EventsUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.TradingSignalUI;
import com.kraj.tradeapp.core.service.DashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/current-trades")
    public ResponseEntity<List<CurrentTradeUI>> getCurrentTrades() {
        return ResponseEntity.ok(dashboardService.getCurrentTrades());
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
