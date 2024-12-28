package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.service.NotificationProcessorService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationProcessorService notificationProcessorService;

    @PostMapping("/notificationEvents")
    public ResponseEntity<Void> createNotificationEvent(@RequestBody String payload) {
        notificationProcessorService.queueAndProcessNotification(payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/notificationEvents/{symbol}")
    public ResponseEntity<List<NotificationEvent>> getNotificationEvents(@PathVariable String symbol) {
        List<NotificationEvent> events = notificationProcessorService.getNotificationEvents(
            symbol,
            LocalDateTime.now().minusHours(6),
            LocalDateTime.now()
        );
        return ResponseEntity.ok(events);
    }

    @GetMapping("/getTradeSignals/{symbol}")
    public ResponseEntity<List<TradeSignal>> getTradeSignals(String symbol) {
        List<TradeSignal> tradeSignals = notificationProcessorService.getTradeSignals(symbol);
        return ResponseEntity.ok(tradeSignals);
    }

    @GetMapping
    public ResponseEntity<List<NotificationEvent>> getNotifications(@Valid NotificationEventRequest request) {
        return ResponseEntity.ok(
            notificationService.getLatestNotifications(request.getIndicator(), request.getInterval(), request.getLimit())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationEvent> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PostMapping
    public ResponseEntity<NotificationEvent> createNotification(@RequestBody @Valid NotificationEvent notificationEvent) {
        return ResponseEntity.ok(notificationService.createNotification(notificationEvent));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationEvent> updateNotification(
        @PathVariable Long id,
        @RequestBody @Valid NotificationEvent notificationEvent
    ) {
        return ResponseEntity.ok(notificationService.updateNotification(id, notificationEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
