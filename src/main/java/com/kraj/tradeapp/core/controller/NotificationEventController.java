package com.kraj.tradeapp.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kraj.tradeapp.core.model.CommonUtil;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.service.NotificationProcessorService;
import com.kraj.tradeapp.core.service.OHLCService;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationProcessorService notificationProcessorService;
    private final OHLCService ohlcService;

    @PostMapping("/receiveEvents")
    public ResponseEntity<Void> createNotificationEvent(@RequestBody String payload) {
        notificationProcessorService.queueAndProcessNotification(payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/ohlc")
    public ResponseEntity<?> handleOhlcDataPost(@RequestBody String payload) throws JsonProcessingException {
        ohlcService.handleOHLCDataPost(payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/notificationEvents/{symbol}")
    public ResponseEntity<List<NotificationEventDto>> getNotificationEvents(@PathVariable String symbol) {
        List<NotificationEventDto> events = notificationProcessorService.getNotificationEvents(
            symbol,
            ZonedDateTime.now().minusHours(24),
            ZonedDateTime.now()
        );
        return ResponseEntity.ok(events);
    }

    @GetMapping("/getTradeSignals/{symbol}")
    public ResponseEntity<List<TradeSignal>> getTradeSignals(String symbol) {
        List<TradeSignal> tradeSignals = notificationProcessorService.getTradeSignals(symbol);
        return ResponseEntity.ok(tradeSignals);
    }
}
