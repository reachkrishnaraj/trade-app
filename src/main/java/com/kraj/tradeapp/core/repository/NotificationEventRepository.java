package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.symbol = :symbol AND ne.datetime BETWEEN :start AND :end")
    List<NotificationEvent> getBetweenDatetime(String symbol, LocalDateTime start, LocalDateTime end);

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.indicator = :indicator AND ne.interval = :interval AND ne.symbol = :symbol ORDER BY ne.datetime DESC"
    )
    Optional<NotificationEvent> getLatestByIndicatorAndInterval(String indicator, String interval, String symbol);

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.indicator = :indicator AND ne.interval = :interval AND ne.direction = :direction AND ne.symbol = :symbol"
    )
    List<NotificationEvent> findByIndicatorAndIntervalAndDirection(String indicator, String interval, String direction, String symbol);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.category = :category AND ne.symbol = :symbol")
    List<NotificationEvent> getForCategory(String category, String symbol);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.category = :category AND ne.symbol = :symbol ORDER BY ne.datetime DESC")
    Optional<NotificationEvent> getLatestCategoryEvent(String category, String symbol);

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.category = :category AND ne.symbol = :symbol AND ne.interval = :interval ORDER BY ne.datetime DESC"
    )
    Optional<NotificationEvent> getLatestCategoryEventForInterval(String category, String symbol, String interval);

    List<NotificationEvent> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT n FROM NotificationEvent n WHERE n.indicator = :indicator AND n.interval = :interval ORDER BY n.datetime DESC")
    List<NotificationEvent> findTopByIndicatorAndIntervalOrderByDatetimeDesc(
        @Param("indicator") String indicator,
        @Param("interval") String interval,
        Pageable pageable
    );
}
