package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dto.TradeSignalRequest;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.repository.TradeSignalRepository;
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

    public TradeSignal createSignal(TradeSignalRequest request) {
        TradeSignal signal = new TradeSignal();
        signal.setDatetime(LocalDateTime.now());
        signal.setSymbol(request.getSymbol());
        signal.setConfidence(request.getConfidence());
        signal.setReason(request.getReason());
        signal.setSource(request.getSource());
        signal.setCreatedTs(LocalDateTime.now());
        signal.setLastUpdated(LocalDateTime.now());
        return tradeSignalRepository.save(signal);
    }

    public TradeSignal updateSignal(Long id, TradeSignalRequest request) {
        TradeSignal existingSignal = tradeSignalRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Signal not found"));
        existingSignal.setSymbol(request.getSymbol());
        existingSignal.setConfidence(request.getConfidence());
        existingSignal.setReason(request.getReason());
        existingSignal.setSource(request.getSource());
        existingSignal.setLastUpdated(LocalDateTime.now());
        return tradeSignalRepository.save(existingSignal);
    }

    public void deleteSignal(Long id) {
        TradeSignal signal = tradeSignalRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Signal not found"));
        tradeSignalRepository.delete(signal);
    }
}
