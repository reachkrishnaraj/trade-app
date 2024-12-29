package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.EventInterval;
import com.kraj.tradeapp.core.model.Indicator;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import com.kraj.tradeapp.core.repository.TradeSignalRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        String signalCategory = StringUtils.isNotBlank(payloadMap.get("sig")) ? payloadMap.get("sig") : "UNKNOWN";

        String direction = StringUtils.isNotBlank(payloadMap.get("dir")) ? payloadMap.get("dir") : "UNKNOWN";
        String source = StringUtils.isNotBlank(payloadMap.get("src")) ? payloadMap.get("src") : "TRADING_VIEW";
        String rawMsg = StringUtils.isNotBlank(payloadMap.get("rawMsg")) ? payloadMap.get("rawMsg") : "UNKNOWN";

        String derivedValue = getDerivedValue(indicator, payloadMap);

        String eventDateTimeStr = StringUtils.isNotBlank(payloadMap.get("time")) ? payloadMap.get("time") : LocalDateTime.now().toString();
        LocalDateTime eventDateTime = LocalDateTime.parse(eventDateTimeStr);

        NotificationEvent notificationEvent = NotificationEvent.builder()
            .price(price)
            .direction(direction)
            .rawMsg(rawMsg)
            .created(LocalDateTime.now())
            .lastUpdated(LocalDateTime.now())
            .category(signalCategory)
            .source(source)
            .datetime(eventDateTime)
            .derivedValue(derivedValue)
            .interval(interval.getValue())
            .indicator(indicator.name())
            .lastUpdated(LocalDateTime.now())
            .category(signalCategory)
            .build();
        notificationEventRepository.save(notificationEvent);
    }

    private String getDerivedValue(Indicator indicator, Map<String, String> payload) {
        return "";
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

    public List<NotificationEvent> getNotificationEvents(String symbol, LocalDateTime from, LocalDateTime to) {
        return notificationEventRepository.getBetweenDatetime(symbol, from, to);
    }

    public List<NotificationEvent> getNotificationEventsForInterval(
        String symbol,
        LocalDateTime from,
        LocalDateTime to,
        EventInterval interval
    ) {
        return notificationEventRepository
            .getBetweenDatetime(symbol, from, to)
            .stream()
            .filter(e -> StringUtils.equalsAnyIgnoreCase(e.getInterval(), interval.name()))
            .toList();
    }

    public List<TradeSignal> getTradeSignals(String symbol) {
        return tradeSignalRepository.findByDatetimeBetween(symbol, LocalDateTime.now().minusHours(24), LocalDateTime.now());
    }
}
