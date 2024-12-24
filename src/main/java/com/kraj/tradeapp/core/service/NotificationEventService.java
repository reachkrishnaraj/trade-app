package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.AccountPNL;
import com.kraj.tradeapp.core.model.NotificationEvent;
import com.kraj.tradeapp.core.repository.AccountPNLRepository;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class NotificationEventService {

    private final NotificationEventRepository notificationEventRepository;

    @Autowired
    public NotificationEventService(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    public List<NotificationEvent> getLatestNotifications(String indicator, String interval) {
        return notificationEventRepository.findByIndicatorAndInterval(indicator, interval);
    }

    public List<NotificationEvent> getNotificationsForDateRange(LocalDateTime start, LocalDateTime end) {
        return notificationEventRepository.findByDatetimeBetween(start, end);
    }
}
