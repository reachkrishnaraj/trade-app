package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.ComputedTradeSignal;
import com.kraj.tradeapp.core.model.dto.TradeSignalRequest;
import com.kraj.tradeapp.core.repository.ComputedTradeSignalRepository;
import jakarta.persistence.EntityNotFoundException;
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

    public ComputedTradeSignal createSignal(TradeSignalRequest request) {
        ComputedTradeSignal signal = new ComputedTradeSignal();
        signal.setDatetime(LocalDateTime.now());
        signal.setSymbol(request.getSymbol());
        signal.setSignalType(request.getSignalType());
        signal.setConfidence(request.getConfidence());
        signal.setReason(request.getReason());
        signal.setSource(request.getSource());
        signal.setCreatedTs(LocalDateTime.now());
        signal.setLastUpdated(LocalDateTime.now());
        return computedTradeSignalRepository.save(signal);
    }

    public ComputedTradeSignal updateSignal(Long id, TradeSignalRequest request) {
        ComputedTradeSignal existingSignal = computedTradeSignalRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Signal not found"));
        existingSignal.setSymbol(request.getSymbol());
        existingSignal.setSignalType(request.getSignalType());
        existingSignal.setConfidence(request.getConfidence());
        existingSignal.setReason(request.getReason());
        existingSignal.setSource(request.getSource());
        existingSignal.setLastUpdated(LocalDateTime.now());
        return computedTradeSignalRepository.save(existingSignal);
    }

    public void deleteSignal(Long id) {
        ComputedTradeSignal signal = computedTradeSignalRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Signal not found"));
        computedTradeSignalRepository.delete(signal);
    }
}
