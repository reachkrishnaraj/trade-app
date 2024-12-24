package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.ComputedTradeSignal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputedTradeSignalRepository {
    List<ComputedTradeSignal> findBySymbol(String symbol);

    List<ComputedTradeSignal> findByConfidenceGreaterThan(BigDecimal confidence);

    List<ComputedTradeSignal> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("DELETE FROM ComputedTradeSignal c WHERE c.datetime < :date")
    void deleteOldSignals(@Param("date") LocalDateTime date);
}
