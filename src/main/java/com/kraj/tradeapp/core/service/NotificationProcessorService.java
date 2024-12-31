package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.*;
import com.kraj.tradeapp.core.model.dto.NotificationEventDto;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import com.kraj.tradeapp.core.repository.QueueRepository;
import com.kraj.tradeapp.core.repository.TradeSignalRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProcessorService {

    private final NotificationEventRepository notificationEventRepository;
    private final TradeSignalRepository tradeSignalRepository;
    private final QueueRepository queueRepository;

    private static final String CUSTOM_PAYLOAD_SEPARATOR = "|";
    private final Queue<String> eventQueue = new LinkedList<>();

    public void queueAndProcessNotification(String payload) {
        eventQueue.offer(payload);
    }

    @Scheduled(fixedDelay = 500)
    public void processTradingViewNotification() {
        try {
            processTradingViewNotificationPriv();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing notification");
        }
    }

    public void processTradingViewNotificationPriv() {
        if (eventQueue.isEmpty()) {
            return;
        }
        String payload = eventQueue.poll();
        Map<String, String> payloadMap = getPayloadMap(payload);
        //handle empty
        if (payloadMap.isEmpty()) {
            return;
        }

        BigDecimal price = StringUtils.isNotBlank(payloadMap.get("price")) ? new BigDecimal(payloadMap.get("price")) : BigDecimal.ZERO;

        String indicatorRaw = StringUtils.isNotBlank(payloadMap.get("ind")) ? payloadMap.get("ind") : "UNKNOWN";
        Indicator indicator = Indicator.fromString(indicatorRaw);

        String symbol = StringUtils.isNotBlank(payloadMap.get("sym")) ? payloadMap.get("sym") : "UNKNOWN";

        String intervalRaw = StringUtils.isNotBlank(payloadMap.get("int")) ? payloadMap.get("int") : "UNKNOWN";
        EventInterval interval = EventInterval.getFromValue(intervalRaw);

        String signalCategory = StringUtils.isNotBlank(payloadMap.get("sig_cat")) ? payloadMap.get("sig_cat") : "UNKNOWN";

        String direction = StringUtils.isNotBlank(payloadMap.get("dir")) ? payloadMap.get("dir") : "UNKNOWN";
        String source = StringUtils.isNotBlank(payloadMap.get("src")) ? payloadMap.get("src") : "TRADING_VIEW";
        String rawMsg = StringUtils.isNotBlank(payloadMap.get("rawMsg")) ? payloadMap.get("rawMsg") : "UNKNOWN";

        String derivedValue = getDerivedValue(indicator, payloadMap);

        String eventDateTimeStr = StringUtils.isNotBlank(payloadMap.get("time")) ? payloadMap.get("time") : ZonedDateTime.now().toString();
        LocalDateTime eventDateTime = ZonedDateTime.parse(eventDateTimeStr)
            .withZoneSameInstant(ZoneId.of("America/New_York"))
            .toLocalDateTime();

        String tradeActionStr = StringUtils.isNotBlank(payloadMap.get("t_action")) ? payloadMap.get("t_action") : TradeAction.NONE.name();
        TradeAction tradeAction = TradeAction.fromString(tradeActionStr);

        NotificationEvent notificationEvent = NotificationEvent.builder()
            .price(price)
            .symbol(symbol)
            .direction(direction)
            .rawMsg(rawMsg)
            .created(LocalDateTime.now())
            .lastUpdated(LocalDateTime.now())
            .category(signalCategory)
            .source(source)
            .datetime(eventDateTime)
            .derivedValue(derivedValue)
            .interval(interval.name())
            .indicator(indicator.name())
            .lastUpdated(LocalDateTime.now())
            .category(signalCategory)
            .tradeAction(tradeAction.name())
            .build();
        notificationEvent.setStrategy(indicator.isStrategy());
        notificationEvent.setImportance(indicator.getDefaultImportance().name());
        notificationEvent = overrideFieldsIfNeeded(notificationEvent, indicator, payload);
        notificationEventRepository.save(notificationEvent);
        //handle trading signal event here
    }

    private NotificationEvent overrideFieldsIfNeeded(NotificationEvent notificationEvent, Indicator indicator, String payload) {
        switch (indicator) {
            case Q_LINE,
                Q_BANDS,
                Q_CLOUD,
                Q_GRID,
                Q_MOMENTUM,
                Q_MONEY_BALL,
                Q_ORACLE_SQUEEZER,
                Q_SMC_2ND_TREND_RIBBON,
                Q_SMC_TREND_RIBBON,
                Q_ELITE,
                Q_WAVE,
                QKRONOS,
                QSUMO,
                QGRID_ELITE,
                QCLOUD_TREND_TRADER,
                QSCALPER -> {
                notificationEvent.setCategory(SignalCategory.DIRECTION.name());
                Direction direction = StringUtils.containsIgnoreCase(payload, "BULL")
                    ? Direction.BULL
                    : StringUtils.containsIgnoreCase(payload, "BEAR") ? Direction.BEAR : Direction.NEUTRAL;
                notificationEvent.setDirection(direction.name());
                notificationEvent.setDerivedValue(direction.name());
                return notificationEvent;
            }
            case ABSORPTION, EXHAUSTION, BIG_TRADES -> {}
            case DELTA_TURNAROUND -> {}
            case SPEED_OF_TAPE -> {}
            case FVG_DETECTOR -> {}
            case IFVG_DETECTOR -> {}
            case FVG_REJECTION_DETECTOR -> {}
            case DISPLACEMENT_DETECTOR -> {}
            case SMT_DIVERGENCE_DETECTOR -> {}
            case LIQUIDITY_SWEEP_DETECTOR -> {}
            case PRICE_DROP_DETECTOR -> {}
            case SMC_BREAKOUT -> {}
            case STACKED_IMBALANCES -> {}
            case BJ_KEY_LEVELS -> {}
            case SR_CHANNEL_V2 -> {}
            case SMC_CONCEPTS -> {}
            case BPR_DETECTOR -> {}
            case CONSOLIDATION_ZONE_TRACKER -> {}
            case UNKNOWN -> {}
        }
        return notificationEvent;
    }

    private String getDerivedValue(Indicator indicator, Map<String, String> payload) {
        return "sample_derived_value";
    }

    //    private Optional<String> parseIndicator(String payload){
    //        if(!payload.toUpperCase().startsWith("PAYLOAD=CUSTOM")){
    //            //not a custom payload
    //        }
    //        String[] payloadStrArr = StringUtils.split(payload, CUSTOM_PAYLOAD_SEPARATOR);
    //        if(payloadStrArr.length < 2){
    //            return Optional.empty();
    //        }
    //    }

    private Map<String, String> getPayloadMap(String payload) {
        if (!payload.toUpperCase().startsWith("PAYLOAD=CUSTOM")) {
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

    public List<NotificationEventDto> getNotificationEvents(String symbol, LocalDateTime from, LocalDateTime to) {
        return notificationEventRepository.getBetweenDatetime(symbol, from, to).stream().map(this::getDto).toList();
    }

    public List<NotificationEventDto> getNotificationEventsForInterval(
        String symbol,
        LocalDateTime from,
        LocalDateTime to,
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
        return tradeSignalRepository.findByDatetimeBetween(symbol, LocalDateTime.now().minusHours(24), LocalDateTime.now());
    }

    private NotificationEventDto getDto(NotificationEvent event) {
        int minsSinceEventTime = (int) event.getDatetime().until(LocalDateTime.now(), java.time.temporal.ChronoUnit.MINUTES);
        int hoursSinceEventTime = minsSinceEventTime / 60;
        String sinceCreatedStr = hoursSinceEventTime > 0 ? hoursSinceEventTime + "hr(s) ago" : minsSinceEventTime + "min(s) ago";
        return NotificationEventDto.builder()
            .id(event.getId())
            .datetime(event.getDatetime())
            .symbol(event.getSymbol())
            .source(event.getSource())
            .indicator(event.getIndicator())
            .derivedValue(event.getDerivedValue())
            .direction(event.getDirection())
            .category(event.getCategory())
            .rawMsg(event.getRawMsg())
            .price(event.getPrice())
            .interval(event.getInterval())
            .created(event.getCreated())
            .lastUpdated(event.getLastUpdated())
            .isStrategy(event.isStrategy())
            .importance(event.getImportance())
            .sinceCreatedStr(sinceCreatedStr)
            .build();
    }
}
