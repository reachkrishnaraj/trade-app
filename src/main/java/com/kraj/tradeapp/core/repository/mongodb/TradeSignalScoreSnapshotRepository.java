package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.persistance.mongodb.TradeSignalScoreSnapshot;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeSignalScoreSnapshotRepository extends MongoRepository<TradeSignalScoreSnapshot, String> {
    @Query("{ 'symbol': ?0, 'datetime': { $gte: ?1, $lte: ?2 } }")
    List<TradeSignalScoreSnapshot> findBySymbolAndDatetimeBetween(String symbol, LocalDateTime start, LocalDateTime end);
}
