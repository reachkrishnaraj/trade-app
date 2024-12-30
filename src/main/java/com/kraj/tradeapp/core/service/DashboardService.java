package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dashboard.ui.dto.CurrentTradeUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.EventsUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.TradingSignalUI;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import com.kraj.tradeapp.core.repository.TradeSignalRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

/**
 * This service handles the WebSocket flow for the dashboard.
 * It retrieves current trades, trading signals, and events, and sends updates via WebSocket.
 * The methods addCurrentTrade, addTradingSignal, and addEvent use the messagingTemplate to send data to specific WebSocket topics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SimpMessageSendingOperations messagingTemplate;

    private final NotificationEventRepository notificationEventRepository;

    private final TradeSignalRepository tradeSignalRepository;

    public List<TradingSignalUI> getTradingSignals() {
        List<TradeSignal> tradeSignals = tradeSignalRepository.findAll();
        return tradeSignals
            .stream()
            .map(signal -> {
                TradingSignalUI tradingSignalUI = new TradingSignalUI();
                tradingSignalUI.setSignal(signal.getSymbol());
                tradingSignalUI.setDateTime(signal.getDatetime());
                tradingSignalUI.setSource(signal.getSource());
                return tradingSignalUI;
            })
            .collect(Collectors.toList());
    }

    public List<EventsUI> getEvents() {
        List<NotificationEvent> notificationEvents = notificationEventRepository.findAll();
        return notificationEvents
            .stream()
            .map(event -> {
                EventsUI eventsUI = new EventsUI();
                eventsUI.setIndicatorName(event.getIndicator());
                eventsUI.setRawMessage(event.getRawMsg());
                eventsUI.setDateTime(event.getDatetime());
                eventsUI.setSignal(event.getDirection());
                eventsUI.setSymbol(event.getSymbol());
                return eventsUI;
            })
            .collect(Collectors.toList());
    }

    /**
     * TODO: update this class to compute and return the actual current trades,
     * trading signals, and events. and to send updates via WebSocket.
     */
    public List<CurrentTradeUI> getCurrentTrades() {
        // mock data
        List<CurrentTradeUI> trades = List.of(
            new CurrentTradeUI("Account1", 1L, new BigDecimal("100.00"), LocalDateTime.of(2023, 10, 1, 15, 30), "Open"),
            new CurrentTradeUI("Account2", 2L, new BigDecimal("200.00"), LocalDateTime.of(2023, 10, 2, 16, 30), "Closed"),
            new CurrentTradeUI("Account3", 3L, new BigDecimal("300.00"), LocalDateTime.now(), "Open")
        );
        return trades;
    }

    public void publishCurrentTrade(CurrentTradeUI trade) {
        log.info("Adding trade: {}", trade);
        messagingTemplate.convertAndSend("/topic/current-trades", List.of(trade));
    }
}
