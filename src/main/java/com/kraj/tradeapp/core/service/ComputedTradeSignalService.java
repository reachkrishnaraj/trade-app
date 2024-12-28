package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.repository.TradeSignalRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class ComputedTradeSignalService {

    private final TradeSignalRepository tradeSignalRepository;

    @Autowired
    public ComputedTradeSignalService(TradeSignalRepository tradeSignalRepository) {
        this.tradeSignalRepository = tradeSignalRepository;
    }

    public List<TradeSignal> getSignalsForSymbol(String symbol) {
        return tradeSignalRepository.findBySymbol(symbol);
    }

    public List<TradeSignal> getHighConfidenceSignals(BigDecimal minConfidence) {
        return tradeSignalRepository.findByConfidenceGreaterThan(minConfidence);
    }

    public void deleteOldSignals(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        tradeSignalRepository.deleteOldSignals(cutoffDate);
    }
}
