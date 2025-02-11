package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dashboard.ui.dto.CurrentTradeUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.EventsUI;
import com.kraj.tradeapp.core.model.dashboard.ui.dto.TradingSignalUI;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
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

    /**
     * TODO: update this class to compute and return the actual current trades,
     * trading signals, and events. and to send updates via WebSocket.
     */
    private final NotificationProcessorService notificationProcessorService;
    private final SimpMessageSendingOperations messagingTemplate;

    private final List<String> SUPPORTED_SYMBOLS = Arrays.asList("NQ", "ES");

    public List<CurrentTradeUI> getCurrentTrades() {
        // mock data
        List<CurrentTradeUI> trades = List.of(
            new CurrentTradeUI("Account1", 1L, new BigDecimal("100.00"), LocalDateTime.of(2023, 10, 1, 15, 30), "Open"),
            new CurrentTradeUI("Account2", 2L, new BigDecimal("200.00"), LocalDateTime.of(2023, 10, 2, 16, 30), "Closed"),
            new CurrentTradeUI("Account3", 3L, new BigDecimal("300.00"), LocalDateTime.now(), "Open")
        );
        return trades;
    }

    public List<TradingSignalUI> getTradingSignals() {
        List<TradingSignalUI> signals = List.of(
            new TradingSignalUI("Buy", "System1", LocalDateTime.of(2023, 10, 1, 15, 30)),
            new TradingSignalUI("Sell", "System2", LocalDateTime.of(2023, 10, 2, 16, 30)),
            new TradingSignalUI("Sell", "test", LocalDateTime.of(2024, 12, 29, 23, 30)),
            new TradingSignalUI("Hold", "System3", LocalDateTime.now())
        );
        return signals;
    }

    public List<EventsUI> getEvents() {
        List<EventsUI> events = List.of(
            new EventsUI("Indicator1", "Raw message 1", LocalDateTime.of(2023, 10, 1, 15, 30), "Buy", "Symbol1"),
            new EventsUI("Indicator2", "Raw message test", LocalDateTime.of(2024, 12, 29, 23, 25), "Sell", "Symbol2"),
            new EventsUI("Indicator2", "Raw message test 2 hours", LocalDateTime.of(2024, 12, 29, 21, 25), "Sell", "Symbol2"),
            new EventsUI("Indicator2", "Raw message 2", LocalDateTime.of(2023, 10, 2, 16, 30), "Sell", "Symbol2"),
            new EventsUI("Indicator3", "Raw message 3", LocalDateTime.now(), "Hold", "Symbol3")
        );
        return events;
    }

    //    private List<EventsDto> getEventsDto(String symbol) {
    //        List<NotificationEvent> notificationEvents = notificationEventRepository.getBetweenDatetime(symbol,
    //            LocalDateTime.now().minusHours(24),
    //            LocalDateTime.now());
    //        return notificationEvents.stream()
    //            .map(event -> EventsDto.builder()
    //                .symbol(event.getSymbol())
    //                .indicatorName(event.getIndicator())
    //                .category(event.getCategory())
    //                .interval(event.getInterval())
    //                .direction(event.getDirection())
    //                .price(event.getPrice().toString())
    //                .derivedVal(event.getDerivedValue())
    //                .datetime(event.getDatetime().toString())
    //                .rawMessage(event.getRawMsg())
    //                .build())
    //            .collect(Collectors.toList());
    //    }

    public void addCurrentTrade(CurrentTradeUI trade) {
        log.info("Adding trade: {}", trade);
        messagingTemplate.convertAndSend("/topic/current-trades", List.of(trade));
    }

    public void addTradingSignal(TradingSignalUI signal) {
        messagingTemplate.convertAndSend("/topic/trading-signals", List.of(signal));
    }

    public void addEvent(EventsUI event) {
        messagingTemplate.convertAndSend("/topic/events", List.of(event));
    }

    //    @Scheduled(fixedRate = 6000) // Push every 5 seconds
    //    public void pushCurrentTrades() {
    //        messagingTemplate.convertAndSend("/topic/v2/current-trades", dashboardService.getCurrentTrades());
    //    }
    //
    //    @Scheduled(fixedRate = 60000)
    //    public void pushTradingSignals() {
    //        messagingTemplate.convertAndSend("/topic/v2/trading-signals", dashboardService.getTradingSignals());
    //    }

    @Scheduled(fixedRate = 60000)
    public void pushEvents() {
        for (String symbol : SUPPORTED_SYMBOLS) {
            List<NotificationEventDto> dtos = notificationProcessorService.getNotificationEvents(
                symbol,
                ZonedDateTime.now().minusHours(24),
                ZonedDateTime.now()
                //                LocalDateTime.now().minusHours(24),
                //                LocalDateTime.now()
            );
            messagingTemplate.convertAndSend("/topic/v2/events", dtos);
        }
    }
}
