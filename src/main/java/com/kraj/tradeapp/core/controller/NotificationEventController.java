package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.NotificationEvent;
import com.kraj.tradeapp.core.model.dto.NotificationEventRequest;
import com.kraj.tradeapp.core.service.NotificationEventService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationEventService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationEvent>> getNotifications(@Valid NotificationEventRequest request) {
        return ResponseEntity.ok(notificationService.getLatestNotifications(request.getIndicator(), request.getInterval()));
    }
}
