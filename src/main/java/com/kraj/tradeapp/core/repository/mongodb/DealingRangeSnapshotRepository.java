package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.DealingRangeSnapshot;
import com.kraj.tradeapp.core.model.EventInterval;
import com.kraj.tradeapp.core.model.Quadrant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DealingRangeSnapshotRepository extends MongoRepository<DealingRangeSnapshot, String> {
    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    Optional<DealingRangeSnapshot> findBySymbol(String symbol);

    @Query("{'currentQuadrant': ?0}")
    List<DealingRangeSnapshot> findByCurrentQuadrant(Quadrant quadrant);

    @Query("{'currentQuadrant': {$in: ?0}}")
    List<DealingRangeSnapshot> findByCurrentQuadrantIn(List<Quadrant> quadrants);

    // ========================================================================
    // INTERVAL-BASED QUERIES
    // ========================================================================

    // Find by interval
    @Query("{'interval': ?0}")
    List<DealingRangeSnapshot> findByInterval(EventInterval interval);

    // Find by symbol and interval
    @Query("{'symbol': ?0, 'interval': ?1}")
    Optional<DealingRangeSnapshot> findBySymbolAndInterval(String symbol, EventInterval interval);

    // Find by quadrant and interval
    @Query("{'currentQuadrant': ?0, 'interval': ?1}")
    List<DealingRangeSnapshot> findByCurrentQuadrantAndInterval(Quadrant quadrant, EventInterval interval);

    // ========================================================================
    // POSITION-BASED QUERIES
    // ========================================================================

    // Find symbols in extreme positions (all intervals)
    @Query("{'currentQuadrant': {$in: ['Q1_75_100', 'Q4_0_25', 'BELOW_RANGE', 'ABOVE_RANGE']}}")
    List<DealingRangeSnapshot> findExtremePositions();

    // Find symbols within range (all intervals)
    @Query("{'currentQuadrant': {$in: ['Q1_75_100', 'Q2_50_75', 'Q3_25_50', 'Q4_0_25']}}")
    List<DealingRangeSnapshot> findWithinRange();

    // Find extreme positions by interval
    @Query("{'currentQuadrant': {$in: ['Q1_75_100', 'Q4_0_25', 'BELOW_RANGE', 'ABOVE_RANGE']}, 'interval': ?0}")
    List<DealingRangeSnapshot> findExtremePositionsByInterval(EventInterval interval);

    // Find within range by interval
    @Query("{'currentQuadrant': {$in: ['Q1_75_100', 'Q2_50_75', 'Q3_25_50', 'Q4_0_25']}, 'interval': ?0}")
    List<DealingRangeSnapshot> findWithinRangeByInterval(EventInterval interval);
}
