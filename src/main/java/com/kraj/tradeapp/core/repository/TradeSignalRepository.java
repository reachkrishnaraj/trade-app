package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeSignalRepository extends JpaRepository<TradeSignal, Long> {
    List<TradeSignal> findBySymbol(String symbol);

    List<TradeSignal> findByConfidenceGreaterThan(BigDecimal confidence);

    @Query("SELECT t FROM TradeSignal t WHERE t.symbol = :symbol AND t.datetime BETWEEN :start AND :end")
    List<TradeSignal> findByDatetimeBetween(
        @Param("symbol") String symbol,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Transactional
    @Modifying
    @Query("DELETE FROM TradeSignal t WHERE t.datetime < :date")
    void deleteOldSignals(@Param("date") LocalDateTime date);
}
