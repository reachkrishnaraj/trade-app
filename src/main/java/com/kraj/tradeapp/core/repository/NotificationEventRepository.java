package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
    List<NotificationEvent> getBetweenDatetime(
        @Param("symbol") String symbol,
        @Param("start") ZonedDateTime start,
        @Param("end") ZonedDateTime end
    );

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.indicator = :indicator AND ne.interval = :interval " +
        "AND ne.symbol = :symbol ORDER BY ne.datetime DESC"
    )
    Optional<NotificationEvent> getLatestByIndicatorAndInterval(
        @Param("indicator") String indicator,
        @Param("interval") String interval,
        @Param("symbol") String symbol
    );

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.indicator = :indicator AND ne.interval = :interval " +
        "AND ne.direction = :direction AND ne.symbol = :symbol"
    )
    List<NotificationEvent> findByIndicatorAndIntervalAndDirection(
        @Param("indicator") String indicator,
        @Param("interval") String interval,
        @Param("direction") String direction,
        @Param("symbol") String symbol
    );

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.indicatorSubCategory = :indicatorSubCategory AND ne.symbol = :symbol")
    List<NotificationEvent> getForCategory(@Param("indicatorSubCategory") String indicatorSubCategory, @Param("symbol") String symbol);

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.indicatorSubCategory = :indicatorSubCategory AND ne.symbol = :symbol " +
        "ORDER BY ne.datetime DESC"
    )
    Optional<NotificationEvent> getLatestCategoryEvent(
        @Param("indicatorSubCategory") String indicatorSubCategory,
        @Param("symbol") String symbol
    );

    @Query(
        "SELECT ne FROM NotificationEvent ne WHERE ne.indicatorSubCategory = :indicatorSubCategory AND ne.symbol = :symbol " +
        "AND ne.interval = :interval ORDER BY ne.datetime DESC"
    )
    Optional<NotificationEvent> getLatestCategoryEventForInterval(
        @Param("indicatorSubCategory") String indicatorSubCategory,
        @Param("symbol") String symbol,
        @Param("interval") String interval
    );

    List<NotificationEvent> findByDatetimeBetween(ZonedDateTime start, ZonedDateTime end);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.indicator = :indicator AND ne.interval = :interval " + "ORDER BY ne.datetime DESC")
    List<NotificationEvent> findTopByIndicatorAndIntervalOrderByDatetimeDesc(
        @Param("indicator") String indicator,
        @Param("interval") String interval,
        Pageable pageable
    );

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.tradeSignalProcessStatus = 'PENDING' AND ne.created BETWEEN :start AND :end")
    List<NotificationEvent> findEventsPendingTradeSignalProcessing(@Param("start") ZonedDateTime start, @Param("end") ZonedDateTime end);
}
