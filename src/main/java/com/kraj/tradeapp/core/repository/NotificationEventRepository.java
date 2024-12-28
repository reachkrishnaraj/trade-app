package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    List<NotificationEvent> findByIndicatorAndIntervalAndSource(String indicator, String interval, String symbol);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.source = :symbol AND ne.datetime BETWEEN :start AND :end")
    List<NotificationEvent> getBetweenDatetime(String symbol, LocalDateTime start, LocalDateTime end);

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.indicator = :indicator AND ne.interval = :interval AND ne.source = :symbol ORDER BY ne.datetime DESC"
    )
    Optional<NotificationEvent> getLatestByIndicatorAndInterval(String indicator, String interval, String symbol);

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.indicator = :indicator AND ne.interval = :interval AND ne.direction = :direction AND ne.source = :symbol"
    )
    List<NotificationEvent> findByIndicatorAndIntervalAndDirection(String indicator, String interval, String direction, String symbol);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.category = :category AND ne.source = :symbol")
    List<NotificationEvent> getForCategory(String category, String symbol);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.category = :category AND ne.source = :symbol ORDER BY ne.datetime DESC")
    Optional<NotificationEvent> getLatestCategoryEvent(String category, String symbol);

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.category = :category AND ne.source = :symbol AND ne.interval = :interval ORDER BY ne.datetime DESC"
    )
    Optional<NotificationEvent> getLatestCategoryEventForInterval(String category, String symbol, String interval);
}
