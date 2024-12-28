package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.NotificationEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    List<NotificationEvent> findByIndicatorAndInterval(String indicator, String interval);

    List<NotificationEvent> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT n FROM NotificationEvent n WHERE n.indicator = :indicator AND n.interval = :interval ORDER BY n.datetime DESC")
    List<NotificationEvent> findTopByIndicatorAndIntervalOrderByDatetimeDesc(
        @Param("indicator") String indicator,
        @Param("interval") String interval,
        Pageable pageable
    );
}
