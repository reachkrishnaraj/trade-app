package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.NotificationEvent;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

    public List<NotificationEvent> getLatestNotifications(String indicator, String interval, Integer limit) {
        if (limit == null) {
            return notificationEventRepository.findByIndicatorAndInterval(indicator, interval);
        }
        return notificationEventRepository.findTopByIndicatorAndIntervalOrderByDatetimeDesc(indicator, interval, PageRequest.of(0, limit));
    }

    public List<NotificationEvent> getNotificationsForDateRange(LocalDateTime start, LocalDateTime end) {
        return notificationEventRepository.findByDatetimeBetween(start, end);
    }

    public NotificationEvent getNotificationById(Long id) {
        return notificationEventRepository.findById(id).orElseThrow(() -> new RuntimeException("NotificationEvent not found"));
    }

    public NotificationEvent createNotification(NotificationEvent notificationEvent) {
        return notificationEventRepository.save(notificationEvent);
    }

    public NotificationEvent updateNotification(Long id, NotificationEvent notificationEvent) {
        NotificationEvent existingNotification = getNotificationById(id);
        existingNotification.setIndicator(notificationEvent.getIndicator());
        existingNotification.setInterval(notificationEvent.getInterval());
        existingNotification.setDatetime(notificationEvent.getDatetime());
        return notificationEventRepository.save(existingNotification);
    }

    public void deleteNotification(Long id) {
        notificationEventRepository.deleteById(id);
    }
}
