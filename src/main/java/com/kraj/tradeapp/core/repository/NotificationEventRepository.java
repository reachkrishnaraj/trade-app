package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.NotificationEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationEventRepository {
    List<NotificationEvent> findByIndicatorAndInterval(String indicator, String interval);

    List<NotificationEvent> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);
}
