package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.persistance.mongodb.OHLCData;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OHLCRepository extends MongoRepository<OHLCData, String> {
    @Query(value = "{ 'symbol': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }", sort = "{ 'timestamp': 1 }")
    List<OHLCData> findBySymbolAndTimestampBetween(String symbol, ZonedDateTime start, ZonedDateTime end);
}
