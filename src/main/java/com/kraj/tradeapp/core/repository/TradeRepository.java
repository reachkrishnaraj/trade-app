package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.Trade;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository {
    List<Trade> findBySymbolAndStatus(String symbol, String status);

    List<Trade> findByAccountIdAndStatus(String accountId, String status);
}
