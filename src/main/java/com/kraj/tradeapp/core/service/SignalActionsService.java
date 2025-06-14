package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SignalActionsService {

    private final Logger log = LoggerFactory.getLogger(SignalActionsService.class);

    private final SimpMessagingTemplate simpMessagingTemplate;

    // In-memory storage for demo purposes - replace with database repository
    private final Map<Long, SignalActionDTO> signalActionsStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Autowired
    public SignalActionsService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        initializeSampleData();
    }

    /**
     * Initialize sample data for demonstration
     */
    private void initializeSampleData() {
        // Create sample signal actions with enhanced data
        createSampleSignalAction(
            "AAPL",
            new BigDecimal("150.25"),
            "RSI_OVERSOLD",
            "RSI",
            "1h",
            "RSI indicates oversold condition, potential buying opportunity",
            SignalActionDTO.SignalDirection.BUY,
            SignalActionDTO.SignalStatus.PENDING,
            5
        );

        createSampleSignalAction(
            "GOOGL",
            new BigDecimal("2750.80"),
            "MACD_BULLISH",
            "MACD",
            "4h",
            "MACD crossover signal indicates bullish momentum",
            SignalActionDTO.SignalDirection.BUY,
            SignalActionDTO.SignalStatus.PENDING,
            15
        );

        createSampleSignalAction(
            "TSLA",
            new BigDecimal("220.15"),
            "BREAKOUT_PATTERN",
            "Support/Resistance",
            "1d",
            "Price broke above resistance level with high volume",
            SignalActionDTO.SignalDirection.BUY,
            SignalActionDTO.SignalStatus.EXECUTED,
            30
        );

        createSampleSignalAction(
            "MSFT",
            new BigDecimal("380.90"),
            "VOLUME_SPIKE",
            "Volume",
            "15m",
            "Unusual volume spike detected, monitor for breakout",
            SignalActionDTO.SignalDirection.HOLD,
            SignalActionDTO.SignalStatus.PENDING,
            2
        );

        createSampleSignalAction(
            "AMZN",
            new BigDecimal("3200.45"),
            "SUPPORT_BOUNCE",
            "Support/Resistance",
            "2h",
            "Price bounced off key support level",
            SignalActionDTO.SignalDirection.BUY,
            SignalActionDTO.SignalStatus.CANCELLED,
            45
        );

        createSampleSignalAction(
            "NVDA",
            new BigDecimal("450.30"),
            "BOLLINGER_UPPER",
            "Bollinger Bands",
            "1h",
            "Price touching upper Bollinger Band, potential reversal",
            SignalActionDTO.SignalDirection.SELL,
            SignalActionDTO.SignalStatus.PENDING,
            8
        );

        createSampleSignalAction(
            "META",
            new BigDecimal("320.75"),
            "EMA_CROSSOVER",
            "EMA",
            "30m",
            "20 EMA crossed above 50 EMA - bullish signal",
            SignalActionDTO.SignalDirection.BUY,
            SignalActionDTO.SignalStatus.PENDING,
            12
        );

        createSampleSignalAction(
            "BTC-USD",
            new BigDecimal("42150.00"),
            "STOCH_OVERBOUGHT",
            "Stochastic",
            "6h",
            "Stochastic RSI shows overbought conditions",
            SignalActionDTO.SignalDirection.SELL,
            SignalActionDTO.SignalStatus.EXECUTED,
            22
        );
    }

    private void createSampleSignalAction(
        String symbol,
        BigDecimal price,
        String signalName,
        String indicatorName,
        String interval,
        String message,
        SignalActionDTO.SignalDirection direction,
        SignalActionDTO.SignalStatus status,
        int minutesAgo
    ) {
        SignalActionDTO signalAction = new SignalActionDTO();
        signalAction.setId(idGenerator.getAndIncrement());
        signalAction.setSymbol(symbol);
        signalAction.setPrice(price);
        signalAction.setSignalName(signalName);
        signalAction.setIndicatorName(indicatorName);
        signalAction.setInterval(interval);
        signalAction.setMessage(message);
        signalAction.setDirection(direction);
        signalAction.setDateTime(LocalDateTime.now().minusMinutes(minutesAgo));
        signalAction.setStatus(status);
        signalActionsStore.put(signalAction.getId(), signalAction);
    }

    /**
     * Get all signal actions sorted by dateTime descending (latest first)
     *
     * @return list of all signal actions
     */
    public List<SignalActionDTO> getAllSignalActions() {
        log.debug("Getting all signal actions");
        return signalActionsStore
            .values()
            .stream()
            .sorted((a, b) -> b.getDateTime().compareTo(a.getDateTime()))
            .collect(Collectors.toList());
    }

    /**
     * Get signal actions with filtering options
     *
     * @param symbol filter by symbol (optional)
     * @param interval filter by interval (optional)
     * @param indicatorName filter by indicator name (optional)
     * @param fromDateTime filter from date time (optional)
     * @param toDateTime filter to date time (optional)
     * @return filtered list of signal actions
     */
    public List<SignalActionDTO> getFilteredSignalActions(
        String symbol,
        String interval,
        String indicatorName,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime
    ) {
        log.debug("Getting filtered signal actions - symbol: {}, interval: {}, indicator: {}", symbol, interval, indicatorName);

        return signalActionsStore
            .values()
            .stream()
            .filter(signal -> symbol == null || symbol.equals(signal.getSymbol()))
            .filter(signal -> interval == null || interval.equals(signal.getInterval()))
            .filter(signal -> indicatorName == null || indicatorName.equals(signal.getIndicatorName()))
            .filter(
                signal -> fromDateTime == null || signal.getDateTime().isAfter(fromDateTime) || signal.getDateTime().isEqual(fromDateTime)
            )
            .filter(signal -> toDateTime == null || signal.getDateTime().isBefore(toDateTime) || signal.getDateTime().isEqual(toDateTime))
            .sorted((a, b) -> b.getDateTime().compareTo(a.getDateTime()))
            .collect(Collectors.toList());
    }

    /**
     * Get signal action by id
     *
     * @param id the id of the signal action
     * @return the signal action or null if not found
     */
    public SignalActionDTO getSignalActionById(Long id) {
        log.debug("Getting signal action by id: {}", id);
        return signalActionsStore.get(id);
    }

    /**
     * Create a new signal action
     *
     * @param signalActionDTO the signal action to create
     * @return the created signal action
     */
    public SignalActionDTO createSignalAction(SignalActionDTO signalActionDTO) {
        log.debug("Creating new signal action: {}", signalActionDTO);

        signalActionDTO.setId(idGenerator.getAndIncrement());
        signalActionDTO.setDateTime(LocalDateTime.now());
        if (signalActionDTO.getStatus() == null) {
            signalActionDTO.setStatus(SignalActionDTO.SignalStatus.PENDING);
        }
        if (signalActionDTO.getDirection() == null) {
            signalActionDTO.setDirection(SignalActionDTO.SignalDirection.HOLD);
        }

        signalActionsStore.put(signalActionDTO.getId(), signalActionDTO);

        // Broadcast update via WebSocket
        broadcastSignalActionsUpdate();

        return signalActionDTO;
    }

    /**
     * Execute a signal action
     *
     * @param id the id of the signal action to execute
     * @throws IllegalStateException if the signal action cannot be executed
     */
    public void executeSignalAction(Long id) {
        log.debug("Executing signal action: {}", id);

        SignalActionDTO signalAction = signalActionsStore.get(id);
        if (signalAction == null) {
            throw new IllegalStateException("Signal action not found with id: " + id);
        }

        if (signalAction.getStatus() != SignalActionDTO.SignalStatus.PENDING) {
            throw new IllegalStateException("Signal action is not in PENDING status");
        }

        // Update status to EXECUTED
        signalAction.setStatus(SignalActionDTO.SignalStatus.EXECUTED);
        signalAction.setDateTime(LocalDateTime.now()); // Update execution time

        // Here you would typically integrate with your trading system
        log.info(
            "Executed signal action for {} ({}) - {} signal at price {}",
            signalAction.getSymbol(),
            signalAction.getInterval(),
            signalAction.getDirection(),
            signalAction.getPrice()
        );

        // Broadcast update via WebSocket
        broadcastSignalActionsUpdate();
    }

    /**
     * Cancel a signal action
     *
     * @param id the id of the signal action to cancel
     * @throws IllegalStateException if the signal action cannot be cancelled
     */
    public void cancelSignalAction(Long id) {
        log.debug("Cancelling signal action: {}", id);

        SignalActionDTO signalAction = signalActionsStore.get(id);
        if (signalAction == null) {
            throw new IllegalStateException("Signal action not found with id: " + id);
        }

        if (signalAction.getStatus() != SignalActionDTO.SignalStatus.PENDING) {
            throw new IllegalStateException("Signal action is not in PENDING status");
        }

        // Update status to CANCELLED
        signalAction.setStatus(SignalActionDTO.SignalStatus.CANCELLED);
        signalAction.setDateTime(LocalDateTime.now()); // Update cancellation time

        log.info(
            "Cancelled signal action for {} ({}) - {} signal at price {}",
            signalAction.getSymbol(),
            signalAction.getInterval(),
            signalAction.getDirection(),
            signalAction.getPrice()
        );

        // Broadcast update via WebSocket
        broadcastSignalActionsUpdate();
    }

    /**
     * Get unique symbols from all signal actions
     *
     * @return list of unique symbols
     */
    public List<String> getUniqueSymbols() {
        return signalActionsStore.values().stream().map(SignalActionDTO::getSymbol).distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Get unique intervals from all signal actions
     *
     * @return list of unique intervals
     */
    public List<String> getUniqueIntervals() {
        return signalActionsStore.values().stream().map(SignalActionDTO::getInterval).distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Get unique indicator names from all signal actions
     *
     * @return list of unique indicator names
     */
    public List<String> getUniqueIndicatorNames() {
        return signalActionsStore.values().stream().map(SignalActionDTO::getIndicatorName).distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Broadcast signal actions update via WebSocket
     */
    private void broadcastSignalActionsUpdate() {
        List<SignalActionDTO> allSignalActions = getAllSignalActions();
        simpMessagingTemplate.convertAndSend("/topic/signal-actions", allSignalActions);
        log.debug("Broadcasted signal actions update to {} subscribers", allSignalActions.size());
    }

    /**
     * Simulate receiving a new signal (for testing purposes)
     * This method can be called by external systems or scheduled tasks
     */
    public void simulateNewSignal(String symbol, BigDecimal price, String signalName) {
        simulateNewSignal(symbol, price, signalName, "Unknown", "1h", "Simulated signal for testing", SignalActionDTO.SignalDirection.HOLD);
    }

    /**
     * Enhanced simulate new signal with all fields
     */
    public void simulateNewSignal(
        String symbol,
        BigDecimal price,
        String signalName,
        String indicatorName,
        String interval,
        String message,
        SignalActionDTO.SignalDirection direction
    ) {
        SignalActionDTO newSignal = new SignalActionDTO();
        newSignal.setSymbol(symbol);
        newSignal.setPrice(price);
        newSignal.setSignalName(signalName);
        newSignal.setIndicatorName(indicatorName);
        newSignal.setInterval(interval);
        newSignal.setMessage(message);
        newSignal.setDirection(direction);
        newSignal.setStatus(SignalActionDTO.SignalStatus.PENDING);

        createSignalAction(newSignal);
        log.info("Simulated new signal: {} ({}) for {} at {} - {}", signalName, indicatorName, symbol, price, direction);
    }
}
