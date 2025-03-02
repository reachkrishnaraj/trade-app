package com.kraj.tradeapp.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kraj.tradeapp.core.model.CommonUtil;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.service.NotificationProcessorService;
import com.kraj.tradeapp.core.service.OHLCAggregationService;
import com.kraj.tradeapp.core.service.OHLCService;
import com.kraj.tradeapp.core.service.SqlOHLCService;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationProcessorService notificationProcessorService;
    private final OHLCService ohlcService;
    private final OHLCAggregationService ohlcAggregationService;
    private final SqlOHLCService sqlOHLCService;

    @PostMapping("/receiveEvents")
    public ResponseEntity<Void> createNotificationEvent(@RequestBody String payload) {
        notificationProcessorService.queueAndProcessNotification(payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/ohlc")
    public ResponseEntity<?> handleOhlcDataPost(@RequestBody String payload) throws JsonProcessingException {
        handleOhlcDataPostAsync(payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Async
    private void handleOhlcDataPostAsync(String payload) throws JsonProcessingException {
        ohlcService.handleOHLCDataPost(payload);
        sqlOHLCService.handleOHLCDataPost(payload);
    }

    @PostMapping("/sql_ohlc")
    public ResponseEntity<?> handleSqlOhlcDataPost(@RequestBody String payload) throws JsonProcessingException {
        sqlOHLCService.handleOHLCDataPost(payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(value = "/ohlc/{intervalMinutes}", produces = "application/json")
    public ResponseEntity<?> handleSqlOhlcDataGet(@PathVariable int intervalMinutes) {
        return ResponseEntity.ok(ohlcAggregationService.aggregateOHLCWithLookbackV2("ES", intervalMinutes));
    }

    @GetMapping(value = "/sql_ohlc", produces = "application/json")
    public ResponseEntity<?> handleOhlcDataGet() {
        //        sqlOHLCService.aggregateAndSaveOHLC("5m", 5 * 60 * 1000, 61);
        //        sqlOHLCService.aggregateAndSaveOHLC("15m", 15 * 60 * 1000, 60);
        //        sqlOHLCService.aggregateAndSaveOHLC("30m", 30 * 60 * 1000, 60);
        //        sqlOHLCService.aggregateAndSaveOHLC("1h", 60 * 60 * 1000, 50);
        sqlOHLCService.aggregateAndSaveOHLC("4h", 4 * 60 * 60, 12);
        sqlOHLCService.aggregateAndSaveOHLC("1h", 1 * 60 * 60, 48);
        sqlOHLCService.aggregateAndSaveOHLC("30m", 30 * 60, 200);
        sqlOHLCService.aggregateAndSaveOHLC("5m", 5 * 60, 500);
        sqlOHLCService.aggregateAndSaveOHLC("15m", 15 * 60, 200);
        //        sqlOHLCService.aggregateAndSaveOHLC("90m", 90 * 60 * 1000, 50);
        return ResponseEntity.ok("Aggregated OHLC data");
        //        sqlOHLCService.aggregateOHLC("5m", 5 * 60 * 1000, 61);
        //        sqlOHLCService.aggregateOHLC("15m", 15 * 60 * 1000, 10);
        //        sqlOHLCService.aggregateOHLC("30m", 30 * 60 * 1000, 10);
        //        sqlOHLCService.aggregateOHLC("1h", 60 * 60 * 1000, 10);
        //        sqlOHLCService.aggregateOHLC("90m", 90 * 60 * 1000, 10);
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
