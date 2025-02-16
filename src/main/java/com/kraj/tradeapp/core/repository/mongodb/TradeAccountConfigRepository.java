package com.kraj.tradeapp.core.repository.mongodb;

import com.kraj.tradeapp.core.model.persistance.mongodb.TradeAccountConfig;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeAccountConfigRepository extends MongoRepository<TradeAccountConfig, String> {
    // Get all unique accTradeGroupName as list
    @Query(value = "{}", fields = "{ 'accTradeGroupName' : 1 }")
    List<String> findDistinctAccTradeGroupNames();

    // Get all unique accGroupName as list
    @Query(value = "{}", fields = "{ 'accGroupName' : 1 }")
    List<String> findDistinctAccGroupNames();

    // Get records for a specific accTradeGroupName
    List<TradeAccountConfig> findByAccTradeGroupName(String accTradeGroupName);

    // Get records for a specific accGroupName
    List<TradeAccountConfig> findByAccGroupName(String accGroupName);

    // Get records for a specific symbol (Indexed Field)
    List<TradeAccountConfig> findBySymbol(String symbol);
}
