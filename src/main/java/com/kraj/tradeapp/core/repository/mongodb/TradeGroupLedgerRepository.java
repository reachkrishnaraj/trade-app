package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.TradeDirection;
import com.kraj.tradeapp.core.model.TradeStatus;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeGroupLedger;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeGroupLedgerRepository extends MongoRepository<TradeGroupLedger, String> {
    @Query("{ 'tradeAccGroupName': ?0 }")
    List<TradeGroupLedger> findByTradeAccGroupName(String tradeAccGroupName);

    @Query("{ 'tradeAccGroupName': ?0, 'created': { '$gte': ?1, '$lte': ?2 } }")
    List<TradeGroupLedger> findByTradeAccGroupNameAndCreatedBetween(String tradeAccGroupName, ZonedDateTime from, ZonedDateTime to);

    @Query("{ 'tradeAccGroupName': ?0, 'tradeStatus': ?1 }")
    List<TradeGroupLedger> findByTradeAccGroupNameAndTradeStatus(String tradeAccGroupName, TradeStatus tradeStatus);

    @Query("{ 'tradeAccGroupName': ?0, 'tradeStatus': ?1, 'parentSymbol': ?2 }")
    List<TradeGroupLedger> findByTradeAccGroupNameTradeStatusAndParentSymbol(
        String tradeAccGroupName,
        TradeStatus tradeStatus,
        String parentSymbol
    );

    @Query("{ 'tradeAccGroupName': ?0, 'tradeStatus': ?1, 'parentSymbol': ?2, 'tradeDirection': ?3 }")
    List<TradeGroupLedger> findRecordsFor(
        String tradeAccGroupName,
        TradeStatus tradeStatus,
        String parentSymbol,
        TradeDirection tradeDirection
    );
}
