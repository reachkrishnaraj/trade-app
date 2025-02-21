package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.*;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import com.kraj.tradeapp.core.repository.TradeSignalRepository;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessorService implements ApplicationListener<ApplicationReadyEvent> {

    private ApplicationReadyEvent event;

    private final ReentrantLock EVENT_PROCESSOR_LOCK = new ReentrantLock();

    private final TradeSignalSnapshotProcessor tradeSignalSnapshotProcessor;
    private final NotificationEventRepository notificationEventRepository;
    private final TradeSignalRepository tradeSignalRepository;
    private final ScoringService scoringService;
    private final TelegramBotConfig telegramBotConfig;
    private final StrategyService strategyService;
    //private final QueueRepository queueRepository;

    private static final String CUSTOM_PAYLOAD_SEPARATOR = "|";
    private final Queue<String> mainEventQueue = new LinkedList<>();
    private final Queue<String> failedEventsQueue = new LinkedList<>();
    private final ConcurrentHashMap<Long, List<String>> qKronosBuckets = new ConcurrentHashMap<>();

    public void queueAndProcessNotification(String payload) {
        mainEventQueue.offer(payload);
    }

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
            telegramBotConfig.sendMessageToDefaultBotAllChatIds("Error processing event:%s, added to failure queue".formatted(payload));
            failedEventsQueue.offer(payload);
            mainEventQueue.poll();
            throw new RuntimeException("Error processing event:%s, added to failure queue".formatted(payload), e);
        } finally {
            EVENT_PROCESSOR_LOCK.unlock();
        }
    }

    public void processTradingViewNotificationPriv(String payload) {
        Map<String, String> payloadMap = getPayloadMap(payload);
        //handle empty
        if (payloadMap.isEmpty()) {
            return;
        }

        BigDecimal priceVal = getValueFor(PayloadKey.PRICE, payloadMap)
            .filter(CommonUtil::isNumeric)
            .map(BigDecimal::new)
            .orElse(BigDecimal.ZERO);
        BigDecimal priceClose = getValueFor(PayloadKey.PRICE_CLOSE, payloadMap)
            .filter(CommonUtil::isNumeric)
            .map(BigDecimal::new)
            .orElse(BigDecimal.ZERO);
        BigDecimal price = priceVal.compareTo(BigDecimal.ZERO) == 0 ? priceClose : priceVal;

        String indicatorRaw = StringUtils.isNotBlank(payloadMap.get("indicator")) ? payloadMap.get("indicator") : "UNKNOWN";
        Indicator indicator = Indicator.fromString(indicatorRaw);

        String symbol = getValueFor(PayloadKey.SYMBOL, payloadMap).filter(StringUtils::isNotBlank).orElse("UNKNOWN");

        String intervalRaw = getValueFor(PayloadKey.INTERVAL, payloadMap).filter(StringUtils::isNotBlank).orElse("UNKNOWN");
        EventInterval interval = EventInterval.getFromValue(intervalRaw);

        //        String direction = StringUtils.isNotBlank(payloadMap.get("dir")) ? payloadMap.get("dir") : "UNKNOWN";
        String sourceStr = getValueFor(PayloadKey.SOURCE, payloadMap).filter(StringUtils::isNotBlank).orElse("UNKNOWN");
        IndicatorSource source = IndicatorSource.fromString(sourceStr);
        String rawAlertMsg = getValueFor(PayloadKey.ALERT_MESSAGE, payloadMap).filter(StringUtils::isNotBlank).orElse("UNKNOWN");
        String candleTypeStr = getValueFor(PayloadKey.CANDLE_TYPE, payloadMap)
            .filter(StringUtils::isNotBlank)
            .orElse(CandleType.CLASSIC.name());
        CandleType candleType = CandleType.getFromValue(candleTypeStr);

        //String derivedValue = getDerivedValue(indicator, payloadMap);

        @Nullable
        String eventDateTimeStr = getValueFor(PayloadKey.TIME, payloadMap).orElse(ZonedDateTime.now().toString());

        ZonedDateTime eventDateTime = null;
        if (CommonUtil.isNumeric(eventDateTimeStr)) {
            ChronoUnit chronoUnit = determineTimeUnit(Long.parseLong(eventDateTimeStr));
            eventDateTime = chronoUnit == ChronoUnit.MILLIS
                ? Instant.ofEpochMilli(Long.parseLong(eventDateTimeStr)).atZone(ZoneId.of("UTC"))
                : chronoUnit == ChronoUnit.SECONDS
                    ? Instant.ofEpochSecond(Long.parseLong(eventDateTimeStr)).atZone(ZoneId.of("UTC"))
                    : ZonedDateTime.now();
            //            eventDateTime = chronoUnit == ChronoUnit.MILLIS
            //                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(eventDateTimeStr)), ZoneId.of("America/New_York"))
            //                : chronoUnit == ChronoUnit.SECONDS
            //                    ? LocalDateTime.ofEpochSecond(
            //                        Long.parseLong(eventDateTimeStr),
            //                        0,
            //                        ZoneId.of("America/New_York").getRules().getOffset(Instant.now())
            //                    )
            //                    : LocalDateTime.now();
        } else {
            //eventDateTime = ZonedDateTime.parse(eventDateTimeStr).withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
            eventDateTime = ZonedDateTime.now();
        }
        //stale event, set to now
        //eventDateTime = CommonUtil.getNYLocalDateTimeNow();

        //        if (indicator == Indicator.QUANTVUE_QKRONOS) {
        //            long epochSecondsWithDelay = eventDateTime.toEpochSecond(ZoneId.of("America/New_York").getRules().getOffset(Instant.now())) + 5;
        //            long epochSecond = eventDateTime.toEpochSecond(ZoneId.of("America/New_York").getRules().getOffset(Instant.now()));
        //            qKronosBuckets.computeIfAbsent(epochSecond, k -> new ArrayList<>()).add(payload);
        //            return;
        //        }

        @Nullable
        String strategyStr = getValueFor(PayloadKey.STRATEGY, payloadMap).filter(StringUtils::isNotBlank).orElse(null);
        Strategy strategy = Strategy.fromString(strategyStr);

        StrategyProcessStatus strategyProcessStatus = strategy == Strategy.NONE ? StrategyProcessStatus.NA : StrategyProcessStatus.PENDING;

        Optional<IndicatorMsgRule> mayBeMsgRule = scoringService.findMatchingIndicatorEventRule(indicator.name(), rawAlertMsg);

        if (mayBeMsgRule.isPresent() && mayBeMsgRule.get().isAlertable()) {
            telegramBotConfig.sendMessageToDefaultBotAllChatIds("Alertable event found: %s".formatted(rawAlertMsg));
        }

        boolean isSkipScoring =
            mayBeMsgRule.isEmpty() ||
            (StringUtils.isNotBlank(mayBeMsgRule.get().getIsSkipScoring()) &&
                StringUtils.equalsIgnoreCase(mayBeMsgRule.get().getIsSkipScoring(), "true"));

        @Nullable
        BigDecimal scoreRangeMin = mayBeMsgRule.map(IndicatorMsgRule::getScoreRangeMin).orElse(null);
        @Nullable
        BigDecimal scoreRangeMax = mayBeMsgRule.map(IndicatorMsgRule::getScoreRangeMax).orElse(null);
        @Nullable
        BigDecimal score = mayBeMsgRule.map(IndicatorMsgRule::getScore).orElse(null);

        BigDecimal scorePercent = (isSkipScoring || scoreRangeMin == null || scoreRangeMax == null || score == null)
            ? BigDecimal.ZERO
            : ScoringService.calculateBipolarPercentage(scoreRangeMin, scoreRangeMax, score);

        Direction scoreDirection = isSkipScoring || score == null ? Direction.UNKNOWN : ScoringService.categorizeScore(scorePercent);

        NotificationEvent notificationEvent = NotificationEvent.builder()
            .price(price)
            .symbol(symbol)
            .minScore(scoreRangeMin)
            .maxScore(scoreRangeMax)
            .score(score == null ? BigDecimal.ZERO : score)
            .scorePercent(scorePercent)
            .direction(scoreDirection.name())
            .created(ZonedDateTime.now())
            .lastUpdated(ZonedDateTime.now())
            .indicatorSubCategory(mayBeMsgRule.map(IndicatorMsgRule::getSubCategory).orElse("UNKNOWN"))
            .indicatorSubCategoryDisplayName(
                mayBeMsgRule.map(IndicatorMsgRule::getIndicatorSubCategoryDisplayName).filter(StringUtils::isNotBlank).orElse("UNKNOWN")
            )
            .rawAlertMsg(rawAlertMsg)
            .rawPayload(payload)
            .interval(interval.name())
            .candleType(candleType.name())
            .isStrategy(strategy != Strategy.NONE)
            .strategyName(strategy.name())
            .strategyProcessStatus(strategyProcessStatus.name())
            .source(source.name())
            .datetime(eventDateTime)
            .indicator(indicator.name())
            .indicatorDisplayName(
                mayBeMsgRule.map(IndicatorMsgRule::getIndicatorDisplayName).filter(StringUtils::isNotBlank).orElse("UNKNOWN")
            )
            .tradeSignalProcessStatus(isSkipScoring ? ProcessingStatus.NOT_APPLICABLE.name() : ProcessingStatus.PENDING.name())
            .isAlertable(mayBeMsgRule.map(IndicatorMsgRule::isAlertable).orElse(false))
            .build();

        //execute in async
        if (notificationEvent.isStrategy()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    strategyService.handleStrategyEvent(notificationEvent);
                } catch (Exception e) {
                    log.error("Error processing strategy event", e);
                    String msg = "Error processing strategy, msg %s, err:%s".formatted(notificationEvent.getRawAlertMsg(), e.getMessage());
                    telegramBotConfig.sendMessageToDefaultBotAllChatIds(msg);
                } finally {
                    executor.shutdown(); // Shutdown the executor after task completion
                }
            });
        }
        notificationEventRepository.save(notificationEvent);
        tradeSignalSnapshotProcessor.notifyEventForProcessing();
        //tradeSignalSnapshotProcessor.processEventForTradeSignalSnapshot(List.of(notificationEvent));
        //send notification snapshot maintanence service
    }

    //    private NotificationEvent overrideFieldsIfNeeded(NotificationEvent notificationEvent, Indicator indicator, String payload) {
    //        switch (indicator) {
    //            case Q_LINE,
    //                Q_BANDS,
    //                Q_CLOUD,
    //                Q_GRID,
    //                Q_MOMENTUM,
    //                Q_MONEY_BALL,
    //                Q_ORACLE_SQUEEZER,
    //                Q_SMC_2ND_TREND_RIBBON,
    //                Q_SMC_TREND_RIBBON,
    //                 QUANTVUE_QELITE,
    //                Q_WAVE,
    //                QKRONOS,
    //                QSUMO,
    //                QGRID_ELITE,
    //                QCLOUD_TREND_TRADER,
    //                QSCALPER -> {
    //                notificationEvent.setIndicatorSubCategory(EventCategory.DIRECTION.name());
    //                Direction direction = StringUtils.containsIgnoreCase(payload, "BULL")
    //                    ? Direction.BULL
    //                    : StringUtils.containsIgnoreCase(payload, "BEAR") ? Direction.BEAR : Direction.NEUTRAL;
    //                notificationEvent.setDirection(direction.name());
    //                notificationEvent.setDerivedValue(direction.name());
    //                return notificationEvent;
    //            }
    //            case ABSORPTION, EXHAUSTION, BIG_TRADES -> {}
    //            case DELTA_TURNAROUND -> {}
    //            case SPEED_OF_TAPE -> {}
    //            case FVG_DETECTOR -> {}
    //            case IFVG_DETECTOR -> {}
    //            case FVG_REJECTION_DETECTOR -> {}
    //            case DISPLACEMENT_DETECTOR -> {}
    //            case SMT_DIVERGENCE_DETECTOR -> {}
    //            case LIQUIDITY_SWEEP_DETECTOR -> {}
    //            case PRICE_DROP_DETECTOR -> {}
    //            case SMC_BREAKOUT -> {}
    //            case STACKED_IMBALANCES -> {}
    //            case BJ_KEY_LEVELS -> {}
    //            case SR_CHANNEL_V2 -> {}
    //            case SMC_CONCEPTS -> {}
    //            case BPR_DETECTOR -> {}
    //            case CONSOLIDATION_ZONE_TRACKER -> {}
    //            case UNKNOWN -> {}
    //        }
    //        return notificationEvent;
    //    }

    //message format:
    //PAYLOAD=CUSTOM|indicator=Q_LINE|price=500.00|time=2021-09-01T14:00:00Z|strategyName=QUANTVUE_QKRONOS|symbol=SPY|source=TV|interval=1m|candleType=CLASSIC|isStrategy=true
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

    private Map<String, String> getPayloadMapV2(String payload) {
        if (!payload.toUpperCase().startsWith("CUSTOM")) {
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
        //int minsSinceEventTime = (int) event.getDatetime().until(CommonUtil.getNYLocalDateTimeNow(), java.time.temporal.ChronoUnit.MINUTES);
        int minsSinceEventTime = (int) event.getDatetime().until(ZonedDateTime.now(), java.time.temporal.ChronoUnit.MINUTES);
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
}
