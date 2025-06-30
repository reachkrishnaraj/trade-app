// Clean NotificationProcessorService.java with separated processors

package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.*;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import com.kraj.tradeapp.core.repository.TradeSignalRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessorService implements ApplicationListener<ApplicationReadyEvent> {

    private ApplicationReadyEvent event;
    private final ReentrantLock EVENT_PROCESSOR_LOCK = new ReentrantLock();

    // Core dependencies
    private final SimpMessagingTemplate messagingTemplate;
    private final TradeSignalSnapshotProcessor tradeSignalSnapshotProcessor;
    private final NotificationEventRepository notificationEventRepository;
    private final TradeSignalRepository tradeSignalRepository;
    private final ScoringService scoringService;
    private final StrategyService strategyService;
    private final SignalActionsService signalActionsService; // Clean interface

    // Event processing queues
    private static final String CUSTOM_PAYLOAD_SEPARATOR = "|";
    private final Queue<String> mainEventQueue = new LinkedList<>();
    private final Queue<String> failedEventsQueue = new LinkedList<>();
    private final ConcurrentHashMap<Long, List<String>> qKronosBuckets = new ConcurrentHashMap<>();

    // ========================================================================
    // PUBLIC API METHODS
    // ========================================================================

    /**
     * Queue notification for processing
     */
    public void processSpecificEventAndQueue(
        String symbol,
        String interval,
        String indicator,
        String rawMsg,
        String isCall,
        String isText,
        String announce
    ) {
        EventInterval eventInterval = EventInterval.getFromValue(interval);
        Indicator eventIndicator = Indicator.fromString(indicator);
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put(PayloadKey.SYMBOL.getKeyName(), symbol);
        payloadMap.put(PayloadKey.INTERVAL.getKeyName(), interval);
        payloadMap.put(PayloadKey.INDICATOR_NAME.getKeyName(), eventIndicator.name());
        rawMsg = Optional.ofNullable(rawMsg).filter(StringUtils::isNotBlank).orElse("No message provided");
        payloadMap.put(PayloadKey.ALERT_MESSAGE.getKeyName(), rawMsg);
        payloadMap.put(PayloadKey.TIME.getKeyName(), String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli()));
        payloadMap.put(PayloadKey.SOURCE.getKeyName(), IndicatorSource.TRADING_VIEW.name());
        payloadMap.put(PayloadKey.CANDLE_TYPE.getKeyName(), CandleType.CLASSIC.name());
        EnumSet.of(PayloadKey.PRICE, PayloadKey.PRICE_CLOSE, PayloadKey.PRICE_OPEN, PayloadKey.PRICE_LOW, PayloadKey.PRICE_HIGH).forEach(
            key -> payloadMap.put(key.getKeyName(), "0")
        ); // Default price values
        payloadMap.put(PayloadKey.STRATEGY.getKeyName(), Strategy.NONE.name());
        payloadMap.put(PayloadKey.IS_STRATEGY.getKeyName(), "false");
        payloadMap.put(PayloadKey.CANDLE_TYPE.getKeyName(), CandleType.CLASSIC.name());

        // Parse event data
        EventData eventData = parseEventData(payloadMap, rawMsg);

        // Create and save notification event
        NotificationEvent notificationEvent = createNotificationEvent(eventData);
        notificationEventRepository.save(notificationEvent);

        eventData.isCall = StringUtils.containsIgnoreCase(isCall, "true");
        eventData.isText = StringUtils.containsIgnoreCase(isText, "true");
        eventData.isAnnounce = StringUtils.containsIgnoreCase(announce, "true");

        // Create signal action using the appropriate processor
        createSignalActionFromEvent(notificationEvent, eventData);

        // Send WebSocket notification
        NotificationEventDto eventDto = getDto(notificationEvent);
        messagingTemplate.convertAndSend("/topic/events", List.of(eventDto));
    }

    /**
     * Queue notification for processing
     */
    public void queueAndProcessNotification(String payload) {
        mainEventQueue.offer(payload);
    }

    /**
     * Simulate real TradingView webhook for testing
     */
    public void simulateRealTradingViewWebhook(
        String symbol,
        BigDecimal price,
        String indicator,
        String direction,
        String interval,
        String alertMessage
    ) {
        String payload = String.format(
            "CUSTOM|indicator=%s|symbol=%s|price=%s|dir=%s|interval=%s|alert_message=%s|source=TV|time=%d|candleType=CLASSIC",
            indicator,
            symbol,
            price.toString(),
            direction,
            interval,
            alertMessage,
            System.currentTimeMillis()
        );

        log.info("Simulating TradingView webhook for {}: {}", symbol, indicator);
        queueAndProcessNotification(payload);
    }

    /**
     * Process multiple real events in batch
     */
    public void processMultipleRealEvents(List<String> payloads) {
        log.info("Processing {} real events in batch", payloads.size());

        for (String payload : payloads) {
            try {
                processTradingViewNotificationPriv(payload);
            } catch (Exception e) {
                log.error("Error processing payload in batch: {}", payload, e);
            }
        }

        log.info("Completed batch processing of {} real events", payloads.size());
    }

    // ========================================================================
    // EVENT PROCESSING ENGINE
    // ========================================================================

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.event = event;
    }

    @Scheduled(fixedDelay = 1000)
    public void processTradingViewNotification() {
        if (!EVENT_PROCESSOR_LOCK.tryLock()) {
            return;
        }

        if (mainEventQueue.isEmpty()) {
            EVENT_PROCESSOR_LOCK.unlock();
            return;
        }

        String payload = null;
        try {
            payload = mainEventQueue.peek();
            processTradingViewNotificationPriv(payload);
            mainEventQueue.poll();
        } catch (Exception e) {
            failedEventsQueue.offer(payload);
            mainEventQueue.poll();
            throw new RuntimeException("Error processing event: " + payload + ", added to failure queue", e);
        } finally {
            EVENT_PROCESSOR_LOCK.unlock();
        }
    }

    /**
     * Main event processing method - clean and focused
     */
    public void processTradingViewNotificationPriv(String payload) {
        Map<String, String> payloadMap = getPayloadMap(payload);

        if (payloadMap.isEmpty()) {
            return;
        }

        // Parse event data
        EventData eventData = parseEventData(payloadMap, payload);

        // Create and save notification event
        NotificationEvent notificationEvent = createNotificationEvent(eventData);
        notificationEventRepository.save(notificationEvent);

        Optional<IndicatorMsgRule> mayBeRule = scoringService.findMatchingIndicatorEventRule(
            eventData.indicator.name(),
            eventData.rawAlertMsg
        );

        eventData.isAnnounce = mayBeRule
            .map(
                r ->
                    r
                        .getAnnounceTimeframes()
                        .stream()
                        .anyMatch(timeframe -> timeframe.equalsIgnoreCase(eventData.interval.name()) || timeframe.contains("ANY"))
            )
            .orElse(false);

        eventData.isCall = mayBeRule
            .map(
                r ->
                    r
                        .getCallTimeframes()
                        .stream()
                        .anyMatch(timeframe -> timeframe.equalsIgnoreCase(eventData.interval.name()) || timeframe.contains("ANY"))
            )
            .orElse(false);

        eventData.isText = mayBeRule
            .map(
                r ->
                    r
                        .getTextTimeframes()
                        .stream()
                        .anyMatch(timeframe -> timeframe.equalsIgnoreCase(eventData.interval.name()) || timeframe.contains("ANY"))
            )
            .orElse(false);

        // Create signal action using the appropriate processor
        createSignalActionFromEvent(notificationEvent, eventData);

        // Send WebSocket notification
        NotificationEventDto eventDto = getDto(notificationEvent);
        messagingTemplate.convertAndSend("/topic/events", List.of(eventDto));

        // Handle strategy processing if needed
        handleStrategyProcessing(notificationEvent);

        // Notify signal processor
        tradeSignalSnapshotProcessor.notifyEventForProcessing();
    }

    // ========================================================================
    // HELPER METHODS - CLEAN AND FOCUSED
    // ========================================================================

    private EventData parseEventData(Map<String, String> payloadMap, String payload) {
        EventData data = new EventData();

        // Parse price
        BigDecimal priceVal = getValueFor(PayloadKey.PRICE, payloadMap)
            .filter(CommonUtil::isNumeric)
            .map(BigDecimal::new)
            .orElse(BigDecimal.ZERO);
        BigDecimal priceClose = getValueFor(PayloadKey.PRICE_CLOSE, payloadMap)
            .filter(CommonUtil::isNumeric)
            .map(value -> value.replaceAll("[^\\d.]", ""))
            .map(BigDecimal::new)
            .orElse(BigDecimal.ZERO);
        data.price = priceVal.compareTo(BigDecimal.ZERO) == 0 ? priceClose : priceVal;

        // Parse basic fields
        data.indicator = Indicator.fromString(
            StringUtils.isNotBlank(payloadMap.get("indicator")) ? payloadMap.get("indicator") : "UNKNOWN"
        );
        data.symbol = getValueFor(PayloadKey.SYMBOL, payloadMap).filter(StringUtils::isNotBlank).orElse("UNKNOWN");
        data.interval = EventInterval.getFromValue(
            getValueFor(PayloadKey.INTERVAL, payloadMap).filter(StringUtils::isNotBlank).orElse("UNKNOWN")
        );
        data.source = IndicatorSource.fromString(
            getValueFor(PayloadKey.SOURCE, payloadMap).filter(StringUtils::isNotBlank).orElse("UNKNOWN")
        );
        data.rawAlertMsg = getValueFor(PayloadKey.ALERT_MESSAGE, payloadMap).filter(StringUtils::isNotBlank).orElse("UNKNOWN");
        data.candleType = CandleType.getFromValue(
            getValueFor(PayloadKey.CANDLE_TYPE, payloadMap).filter(StringUtils::isNotBlank).orElse(CandleType.CLASSIC.name())
        );

        // Parse timestamp
        String eventDateTimeStr = getValueFor(PayloadKey.TIME, payloadMap).orElse(ZonedDateTime.now().toString());
        if (CommonUtil.isNumeric(eventDateTimeStr)) {
            ChronoUnit chronoUnit = determineTimeUnit(Long.parseLong(eventDateTimeStr));
            data.eventDateTime = chronoUnit == ChronoUnit.MILLIS
                ? Instant.ofEpochMilli(Long.parseLong(eventDateTimeStr)).atZone(ZoneId.of("UTC"))
                : chronoUnit == ChronoUnit.SECONDS
                    ? Instant.ofEpochSecond(Long.parseLong(eventDateTimeStr)).atZone(ZoneId.of("UTC"))
                    : ZonedDateTime.now();
        } else {
            data.eventDateTime = ZonedDateTime.now();
        }

        // Parse strategy
        data.strategy = Strategy.fromString(getValueFor(PayloadKey.STRATEGY, payloadMap).filter(StringUtils::isNotBlank).orElse(null));

        data.payload = payload;
        return data;
    }

    private NotificationEvent createNotificationEvent(EventData data) {
        StrategyProcessStatus strategyProcessStatus = data.strategy == Strategy.NONE
            ? StrategyProcessStatus.NA
            : StrategyProcessStatus.PENDING;

        Optional<IndicatorMsgRule> mayBeMsgRule = scoringService.findMatchingIndicatorEventRule(data.indicator.name(), data.rawAlertMsg);

        boolean isSkipScoring =
            mayBeMsgRule.isEmpty() ||
            (StringUtils.isNotBlank(mayBeMsgRule.get().getIsSkipScoring()) &&
                StringUtils.equalsIgnoreCase(mayBeMsgRule.get().getIsSkipScoring(), "true"));

        BigDecimal scoreRangeMin = mayBeMsgRule.map(IndicatorMsgRule::getScoreRangeMin).orElse(null);
        BigDecimal scoreRangeMax = mayBeMsgRule.map(IndicatorMsgRule::getScoreRangeMax).orElse(null);
        BigDecimal score = mayBeMsgRule.map(IndicatorMsgRule::getScore).orElse(null);
        String indicatorSubCategory = mayBeMsgRule.map(IndicatorMsgRule::getSubCategory).orElse(data.indicator.name());
        String indicatorSubCategoryDisplayName = mayBeMsgRule
            .map(IndicatorMsgRule::getIndicatorSubCategoryDisplayName)
            .filter(StringUtils::isNotBlank)
            .orElse(data.indicator.name());
        String indicatorDisplayName = mayBeMsgRule
            .map(IndicatorMsgRule::getIndicatorDisplayName)
            .filter(StringUtils::isNotBlank)
            .orElse(data.indicator.name());

        BigDecimal scorePercent = (isSkipScoring || scoreRangeMin == null || scoreRangeMax == null || score == null)
            ? BigDecimal.ZERO
            : ScoringService.calculateBipolarPercentage(scoreRangeMin, scoreRangeMax, score);

        Direction scoreDirection = null;

        if (mayBeMsgRule.isPresent() && StringUtils.isNotBlank(mayBeMsgRule.get().getDirection())) {
            scoreDirection = Direction.valueOf(mayBeMsgRule.get().getDirection().toUpperCase());
        } else {
            scoreDirection = isSkipScoring || score == null ? Direction.UNKNOWN : ScoringService.categorizeScore(scorePercent);
        }

        return NotificationEvent.builder()
            .price(data.price)
            .symbol(data.symbol)
            .minScore(isSkipScoring ? BigDecimal.ZERO : scoreRangeMin)
            .maxScore(isSkipScoring ? BigDecimal.ZERO : scoreRangeMax)
            .score(score == null ? BigDecimal.ZERO : score)
            .scorePercent(isSkipScoring ? BigDecimal.ZERO : scorePercent)
            .direction(scoreDirection.name())
            .created(ZonedDateTime.now())
            .lastUpdated(ZonedDateTime.now())
            .indicatorSubCategory(indicatorSubCategory)
            .indicatorSubCategoryDisplayName(indicatorSubCategoryDisplayName)
            .rawAlertMsg(data.rawAlertMsg)
            .rawPayload(data.payload)
            .interval(data.interval.name())
            .candleType(data.candleType.name())
            .isStrategy(data.strategy != Strategy.NONE)
            .strategyName(data.strategy.name())
            .strategyProcessStatus(strategyProcessStatus.name())
            .source(data.source.name())
            .datetime(data.eventDateTime)
            .indicator(data.indicator.name())
            .indicatorDisplayName(indicatorDisplayName)
            .tradeSignalProcessStatus(isSkipScoring ? ProcessingStatus.NOT_APPLICABLE.name() : ProcessingStatus.PENDING.name())
            .isAlertable(mayBeMsgRule.map(IndicatorMsgRule::isAlertable).orElse(false))
            .build();
    }

    private void createSignalActionFromEvent(NotificationEvent notificationEvent, EventData eventData) {
        try {
            // Use the clean processor interface
            SignalActionDTO signalAction = signalActionsService.createSignalActionFromExternalEvent(
                notificationEvent.getSymbol(),
                notificationEvent.getPrice(),
                notificationEvent.getIndicator(),
                notificationEvent.getIndicatorDisplayName(),
                notificationEvent.getInterval(),
                notificationEvent.getRawAlertMsg(),
                notificationEvent.getDirection(),
                ZonedDateTime.now(),
                notificationEvent.getScore(),
                notificationEvent.isStrategy(),
                notificationEvent.isAlertable(),
                eventData.isAnnounce
            );
            signalAction.setAnnounce(eventData.isAnnounce);

            log.info(
                "Created SignalAction {} from NotificationEvent {} for symbol {}",
                signalAction.getId(),
                notificationEvent.getId(),
                notificationEvent.getSymbol()
            );
        } catch (Exception e) {
            log.error("Error creating SignalAction from NotificationEvent {}: {}", notificationEvent.getId(), e.getMessage());
        }
    }

    private void handleStrategyProcessing(NotificationEvent notificationEvent) {
        if (notificationEvent.isStrategy()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    strategyService.handleStrategyEvent(notificationEvent);
                } catch (Exception e) {
                    log.error("Error processing strategy event", e);
                } finally {
                    executor.shutdown();
                }
            });
        }
    }

    // ========================================================================
    // EXISTING HELPER METHODS - UNCHANGED
    // ========================================================================

    public static Map<String, String> getPayloadMap(String payload) {
        if (StringUtils.trim(payload).isEmpty()) {
            return new HashMap<>();
        }

        String[] payloadStrArr = StringUtils.split(payload, CUSTOM_PAYLOAD_SEPARATOR);
        if (payloadStrArr.length < 2) {
            return new HashMap<>();
        }

        Map<String, String> payloadMap = new HashMap<>();
        for (String payloadItem : payloadStrArr) {
            String[] payloadItemArr = StringUtils.split(payloadItem, "=");
            if (payloadItemArr.length < 2) {
                continue;
            }
            payloadMap.put(payloadItemArr[0].trim(), payloadItemArr[1].trim());
        }
        return payloadMap;
    }

    public static Optional<String> getValueFor(PayloadKey payloadKey, Map<String, String> payloadMap) {
        return Optional.ofNullable(payloadMap.get(payloadKey.getKeyName()));
    }

    public List<NotificationEventDto> getNotificationEvents(String symbol, ZonedDateTime from, ZonedDateTime to) {
        return notificationEventRepository.getBetweenDatetime(symbol, from, to).stream().map(this::getDto).toList();
    }

    public List<NotificationEventDto> getNotificationEventsForInterval(
        String symbol,
        ZonedDateTime from,
        ZonedDateTime to,
        EventInterval interval
    ) {
        return notificationEventRepository
            .getBetweenDatetime(symbol, from, to)
            .stream()
            .filter(e -> StringUtils.equalsAnyIgnoreCase(e.getInterval(), interval.name()))
            .toList()
            .stream()
            .map(this::getDto)
            .toList();
    }

    public List<TradeSignal> getTradeSignals(String symbol) {
        return tradeSignalRepository.findByDatetimeBetween(
            symbol,
            CommonUtil.getNYLocalDateTimeNow().minusHours(24),
            CommonUtil.getNYLocalDateTimeNow()
        );
    }

    private NotificationEventDto getDto(NotificationEvent event) {
        int minsSinceEventTime = (int) event.getDatetime().until(ZonedDateTime.now(), ChronoUnit.MINUTES);
        int hoursSinceEventTime = minsSinceEventTime / 60;
        String sinceCreatedStr = hoursSinceEventTime > 0 ? hoursSinceEventTime + "hr(s) ago" : minsSinceEventTime + "min(s) ago";

        return NotificationEventDto.builder()
            .id(event.getId())
            .datetime(event.getDatetime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .symbol(event.getSymbol())
            .source(event.getSource())
            .indicator(event.getIndicator())
            .indicatorDisplayName(event.getIndicatorDisplayName())
            .direction(event.getDirection())
            .indicatorSubCategory(event.getIndicatorSubCategory())
            .indicatorSubCategoryDisplayName(event.getIndicatorSubCategoryDisplayName())
            .rawAlertMsg(event.getRawAlertMsg())
            .rawPayload(event.getRawPayload())
            .price(event.getPrice())
            .interval(event.getInterval())
            .candleType(event.getCandleType())
            .created(event.getCreated().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .lastUpdated(event.getLastUpdated().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .score(event.getScore())
            .isStrategy(event.isStrategy())
            .strategyName(event.getStrategyName())
            .strategyProcessStatus(event.getStrategyProcessStatus())
            .strategyProcessMsg(event.getStrategyProcessMsg())
            .strategyProcessedAt(event.getStrategyProcessedAt() == null ? null : event.getStrategyProcessedAt().toString())
            .sinceCreatedStr(sinceCreatedStr)
            .build();
    }

    public static ChronoUnit determineTimeUnit(long numericValue) {
        if (numericValue > 1_000_000_000_000L) {
            return ChronoUnit.MILLIS;
        } else if (numericValue > 1_000_000_000L) {
            return ChronoUnit.SECONDS;
        } else {
            return ChronoUnit.FOREVER;
        }
    }

    // ========================================================================
    // INNER CLASS FOR EVENT DATA
    // ========================================================================

    private static class EventData {

        BigDecimal price;
        Indicator indicator;
        String symbol;
        EventInterval interval;
        IndicatorSource source;
        String rawAlertMsg;
        CandleType candleType;
        ZonedDateTime eventDateTime;
        Strategy strategy;
        String payload;
        boolean isAnnounce;
        boolean isCall;
        boolean isText;
    }
}
