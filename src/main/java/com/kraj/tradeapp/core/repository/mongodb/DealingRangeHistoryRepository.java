package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.DealingRangeHistory;
import com.kraj.tradeapp.core.model.EventInterval;
import com.kraj.tradeapp.core.model.Quadrant;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DealingRangeHistoryRepository extends MongoRepository<DealingRangeHistory, String> {
    // Find by symbol and interval
    List<DealingRangeHistory> findBySymbolAndIntervalOrderByTimestampDesc(String symbol, EventInterval interval, Pageable pageable);

    // Find by symbol, interval and time range
    List<DealingRangeHistory> findBySymbolAndIntervalAndTimestampBetweenOrderByTimestampDesc(
        String symbol,
        EventInterval interval,
        ZonedDateTime start,
        ZonedDateTime end
    );

    // Find by symbol, interval and after timestamp
    List<DealingRangeHistory> findBySymbolAndIntervalAndTimestampAfterOrderByTimestampDesc(
        String symbol,
        EventInterval interval,
        ZonedDateTime after
    );

    // Find by interval and event type
    List<DealingRangeHistory> findByIntervalAndEventTypeOrderByTimestampDesc(EventInterval interval, String eventType, Pageable pageable);

    // Find recent events across all symbols
    List<DealingRangeHistory> findByIntervalOrderByTimestampDesc(EventInterval interval, Pageable pageable);

    // Find by quadrant and interval
    List<DealingRangeHistory> findByCurrentQuadrantAndIntervalOrderByTimestampDesc(
        Quadrant quadrant,
        EventInterval interval,
        Pageable pageable
    );

    // Custom queries
    @Query("{'symbol': ?0, 'interval': ?1, 'eventType': ?2, 'timestamp': {$gte: ?3}}")
    List<DealingRangeHistory> findBySymbolIntervalEventTypeAndTimestampAfter(
        String symbol,
        EventInterval interval,
        String eventType,
        ZonedDateTime after
    );

    @Query(
        "{'interval': ?0, 'currentQuadrant': {$in: ['BREACH_ABOVE_RANGE', 'BREACH_BELOW_RANGE', 'Q1_75_100', 'Q4_0_25']}, 'timestamp': {$gte: ?1}}"
    )
    List<DealingRangeHistory> findExtremePositionsSince(EventInterval interval, ZonedDateTime since);

    @Query("{'symbol': ?0, 'interval': ?1, 'timestamp': {$gte: ?2, $lte: ?3}}")
    List<DealingRangeHistory> findBySymbolIntervalAndTimeRange(
        String symbol,
        EventInterval interval,
        ZonedDateTime start,
        ZonedDateTime end
    );

    // Count queries for statistics
    @Query(value = "{'symbol': ?0, 'interval': ?1, 'timestamp': {$gte: ?2}}", count = true)
    long countBySymbolIntervalSince(String symbol, EventInterval interval, ZonedDateTime since);

    @Query(value = "{'symbol': ?0, 'interval': ?1, 'currentQuadrant': ?2, 'timestamp': {$gte: ?3}}", count = true)
    long countBySymbolIntervalQuadrantSince(String symbol, EventInterval interval, Quadrant quadrant, ZonedDateTime since);
}
