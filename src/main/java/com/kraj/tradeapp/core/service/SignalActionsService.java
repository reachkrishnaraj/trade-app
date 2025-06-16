// Fixed SignalActionsService.java - No circular dependencies

package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SignalActionsService {

    private final Logger log = LoggerFactory.getLogger(SignalActionsService.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final List<SignalActionProcessor> processors;

    // In-memory storage - replace with database repository in production
    private final Map<Long, SignalActionDTO> signalActionsStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Value("${trading.processor.mode:REAL_TIME}")
    private String processorMode; // SIMULATION or REAL_TIME

    @Autowired
    public SignalActionsService(SimpMessagingTemplate simpMessagingTemplate, List<SignalActionProcessor> processors) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.processors = processors;
    }

    // ========================================================================
    // CORE SERVICE METHODS - Used by controllers and processors
    // ========================================================================

    /**
     * Create a new signal action (used by frontend and internal creation)
     * HANDLES: ID generation, storage, broadcasting
     */
    public SignalActionDTO createSignalAction(SignalActionDTO signalActionDTO) {
        log.debug("Creating signal action: {}", signalActionDTO);

        // Assign ID and timestamp if not already set
        if (signalActionDTO.getId() == null) {
            signalActionDTO.setId(idGenerator.getAndIncrement());
        }
        if (signalActionDTO.getDateTime() == null) {
            signalActionDTO.setDateTime(LocalDateTime.now());
        }
        if (signalActionDTO.getStatus() == null) {
            signalActionDTO.setStatus(SignalActionDTO.SignalStatus.PENDING);
        }
        if (signalActionDTO.getDirection() == null) {
            signalActionDTO.setDirection(SignalActionDTO.SignalDirection.HOLD);
        }

        // Store the signal action
        signalActionsStore.put(signalActionDTO.getId(), signalActionDTO);

        // Broadcast update via WebSocket
        broadcastSignalActionsUpdate();

        log.info(
            "Created signal action: {} for {} at price {}",
            signalActionDTO.getSignalName(),
            signalActionDTO.getSymbol(),
            signalActionDTO.getPrice()
        );

        return signalActionDTO;
    }

    /**
     * Create signal action from external event (uses appropriate processor)
     * CLEAN FLOW: Service -> Processor -> DTO -> Service stores it
     */
    public SignalActionDTO createSignalActionFromExternalEvent(
        String symbol,
        BigDecimal price,
        String indicator,
        String indicatorDisplayName,
        String interval,
        String alertMessage,
        String direction,
        ZonedDateTime eventTime,
        BigDecimal score,
        boolean isStrategy,
        boolean isAlertable
    ) {
        log.debug("Creating signal action from external event for symbol: {}", symbol);

        // Get the appropriate processor
        SignalActionProcessor processor = getActiveProcessor();

        SignalActionDTO signalActionDTO;

        if (processor == null) {
            log.warn("No active processor found, using manual creation");
            signalActionDTO = createManualSignalActionDTO(
                symbol,
                price,
                indicator,
                indicatorDisplayName,
                interval,
                alertMessage,
                direction,
                eventTime
            );
        } else {
            log.debug("Using processor: {} for external event", processor.getProcessorType());

            // CLEAN: Processor returns DTO, no circular calls
            signalActionDTO = processor.createSignalActionDTO(
                symbol,
                price,
                indicator,
                indicatorDisplayName,
                interval,
                alertMessage,
                direction,
                eventTime,
                score,
                isStrategy,
                isAlertable
            );
        }

        // SERVICE handles storage, ID generation, and broadcasting
        return createSignalAction(signalActionDTO);
    }

    // ========================================================================
    // SIGNAL MANAGEMENT METHODS (UNCHANGED)
    // ========================================================================

    /**
     * Get all signal actions sorted by dateTime descending
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
     */
    public SignalActionDTO getSignalActionById(Long id) {
        log.debug("Getting signal action by id: {}", id);
        return signalActionsStore.get(id);
    }

    /**
     * Execute a signal action
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

    // ========================================================================
    // UTILITY METHODS (UNCHANGED)
    // ========================================================================

    /**
     * Get unique symbols from all signal actions
     */
    public List<String> getUniqueSymbols() {
        return signalActionsStore.values().stream().map(SignalActionDTO::getSymbol).distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Get unique intervals from all signal actions
     */
    public List<String> getUniqueIntervals() {
        return signalActionsStore.values().stream().map(SignalActionDTO::getInterval).distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Get unique indicator names from all signal actions
     */
    public List<String> getUniqueIndicatorNames() {
        return signalActionsStore.values().stream().map(SignalActionDTO::getIndicatorName).distinct().sorted().collect(Collectors.toList());
    }

    /**
     * Get signal actions count by status
     */
    public Map<SignalActionDTO.SignalStatus, Long> getSignalActionCountsByStatus() {
        return signalActionsStore.values().stream().collect(Collectors.groupingBy(SignalActionDTO::getStatus, Collectors.counting()));
    }

    /**
     * Clear all signal actions (for testing)
     */
    public void clearAllSignalActions() {
        signalActionsStore.clear();
        broadcastSignalActionsUpdate();
        log.info("Cleared all signal actions");
    }

    /**
     * Get active processor information
     */
    public Map<String, Object> getProcessorInfo() {
        SignalActionProcessor activeProcessor = getActiveProcessor();

        Map<String, Object> info = new HashMap<>();
        info.put("configuredMode", processorMode);
        info.put("activeProcessor", activeProcessor != null ? activeProcessor.getProcessorType() : "NONE");
        info.put(
            "availableProcessors",
            processors.stream().map(p -> Map.of("type", p.getProcessorType(), "enabled", p.isEnabled())).collect(Collectors.toList())
        );

        return info;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private SignalActionProcessor getActiveProcessor() {
        return processors
            .stream()
            .filter(processor -> processor.getProcessorType().equals(processorMode))
            .filter(SignalActionProcessor::isEnabled)
            .findFirst()
            .orElse(null);
    }

    private SignalActionDTO createManualSignalActionDTO(
        String symbol,
        BigDecimal price,
        String indicator,
        String indicatorDisplayName,
        String interval,
        String alertMessage,
        String direction,
        ZonedDateTime eventTime
    ) {
        SignalActionDTO signalAction = new SignalActionDTO();
        signalAction.setSymbol(symbol);
        signalAction.setPrice(price);
        signalAction.setSignalName("MANUAL_" + indicator);
        signalAction.setIndicatorName(indicatorDisplayName != null ? indicatorDisplayName : indicator);
        signalAction.setInterval(interval);
        signalAction.setMessage(alertMessage);
        signalAction.setDirection(mapDirection(direction));
        signalAction.setDateTime(eventTime != null ? eventTime.toLocalDateTime() : LocalDateTime.now());
        signalAction.setStatus(SignalActionDTO.SignalStatus.PENDING);

        return signalAction; // Return DTO, let createSignalAction handle storage
    }

    private SignalActionDTO.SignalDirection mapDirection(String direction) {
        if (direction == null) return SignalActionDTO.SignalDirection.HOLD;

        switch (direction.toUpperCase()) {
            case "BULL":
            case "BULLISH":
            case "BUY":
                return SignalActionDTO.SignalDirection.BUY;
            case "BEAR":
            case "BEARISH":
            case "SELL":
                return SignalActionDTO.SignalDirection.SELL;
            default:
                return SignalActionDTO.SignalDirection.HOLD;
        }
    }

    private void broadcastSignalActionsUpdate() {
        List<SignalActionDTO> allSignalActions = getAllSignalActions();
        simpMessagingTemplate.convertAndSend("/topic/signal-actions", allSignalActions);
        log.debug("Broadcasted signal actions update to {} subscribers", allSignalActions.size());
    }
}
