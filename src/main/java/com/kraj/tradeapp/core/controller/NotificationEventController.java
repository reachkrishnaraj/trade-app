package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.NotificationEvent;
import com.kraj.tradeapp.core.model.dto.NotificationEventRequest;
import com.kraj.tradeapp.core.service.NotificationEventService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationEventService notificationService;

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
