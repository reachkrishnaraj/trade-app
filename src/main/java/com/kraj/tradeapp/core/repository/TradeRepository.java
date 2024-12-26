package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.Trade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findBySymbolAndStatus(String symbol, String status);

    List<Trade> findByAccountIdAndStatus(String accountId, String status);
}
