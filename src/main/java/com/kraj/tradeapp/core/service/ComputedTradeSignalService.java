package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.ComputedTradeSignal;
import com.kraj.tradeapp.core.repository.ComputedTradeSignalRepository;
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

    private final ComputedTradeSignalRepository computedTradeSignalRepository;

    @Autowired
    public ComputedTradeSignalService(ComputedTradeSignalRepository computedTradeSignalRepository) {
        this.computedTradeSignalRepository = computedTradeSignalRepository;
    }

    public List<ComputedTradeSignal> getSignalsForSymbol(String symbol) {
        return computedTradeSignalRepository.findBySymbol(symbol);
    }

    public List<ComputedTradeSignal> getHighConfidenceSignals(BigDecimal minConfidence) {
        return computedTradeSignalRepository.findByConfidenceGreaterThan(minConfidence);
    }

    public void deleteOldSignals(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        computedTradeSignalRepository.deleteOldSignals(cutoffDate);
    }
}
