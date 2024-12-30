package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dashboard.ui.dto.TradingSignalUI;
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
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class ComputedTradeSignalService {

    private final TradeSignalRepository tradeSignalRepository;

    private final SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public ComputedTradeSignalService(TradeSignalRepository tradeSignalRepository, SimpMessageSendingOperations messagingTemplate) {
        this.tradeSignalRepository = tradeSignalRepository;
        this.messagingTemplate = messagingTemplate;
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
        tradeSignalRepository.save(signal);
        sendTradeSignal(signal);
        return signal;
    }

    private void sendTradeSignal(TradeSignal signal) {
        log.info("Sending trade signal: {}", signal);
        TradingSignalUI tradingSignalUI = new TradingSignalUI();
        tradingSignalUI.setDateTime(signal.getDatetime());
        tradingSignalUI.setSignal(signal.getSymbol());
        tradingSignalUI.setSource(signal.getSource());
        messagingTemplate.convertAndSend("/topic/trading-signals", List.of(signal));
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
