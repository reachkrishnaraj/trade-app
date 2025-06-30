package com.kraj.tradeapp.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraj.tradeapp.core.model.*;
import com.kraj.tradeapp.core.model.dto.DealingRangeDto;
import com.kraj.tradeapp.core.model.dto.DealingRangeHistoryDto;
import com.kraj.tradeapp.core.model.persistance.NotificationEvent;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import com.kraj.tradeapp.core.repository.mongodb.DealingRangeHistoryRepository;
import com.kraj.tradeapp.core.repository.mongodb.DealingRangeSnapshotRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedDealingRangeService {

    private final DealingRangeSnapshotRepository dealingRangeSnapshotRepository;
    private final DealingRangeHistoryRepository dealingRangeHistoryRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationEventRepository notificationEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========================================================================
    // MAIN PROCESSING METHODS
    // ========================================================================

    /**
     * Enhanced webhook processing with better history tracking
     */
    public void processDealingRangeWebhook(String jsonPayload) {
        try {
            log.info("Processing enhanced dealing range webhook: {}", jsonPayload);

            DealingRangeSnapshot newSnapshot = createSnapshotFromJson(jsonPayload);
            if (newSnapshot == null) {
                log.warn("Could not create snapshot from JSON payload: {}", jsonPayload);
                return;
            }

            // Check for current snapshot
            Optional<DealingRangeSnapshot> currentSnapshotOpt = dealingRangeSnapshotRepository.findBySymbolAndInterval(
                newSnapshot.getSymbol(),
                newSnapshot.getInterval()
            );

            boolean isQuadrantChange = false;
            boolean isSignificantPriceChange = false;

            if (currentSnapshotOpt.isPresent()) {
                DealingRangeSnapshot currentSnapshot = currentSnapshotOpt.get();

                // Check if quadrant changed
                isQuadrantChange = !currentSnapshot.getCurrentQuadrant().equals(newSnapshot.getCurrentQuadrant());

                // Check for significant price change (configurable threshold)
                BigDecimal priceChangeThreshold = new BigDecimal("0.001"); // 0.1%
                if (currentSnapshot.getCurrentPrice() != null && newSnapshot.getCurrentPrice() != null) {
                    BigDecimal priceChange = newSnapshot
                        .getCurrentPrice()
                        .subtract(currentSnapshot.getCurrentPrice())
                        .abs()
                        .divide(currentSnapshot.getCurrentPrice(), 6, BigDecimal.ROUND_HALF_UP);
                    isSignificantPriceChange = priceChange.compareTo(priceChangeThreshold) > 0;
                }

                log.info(
                    "Status check for {}: quadrant_change={}, significant_price_change={}",
                    newSnapshot.getSymbol(),
                    isQuadrantChange,
                    isSignificantPriceChange
                );
            } else {
                // New symbol, always process
                isQuadrantChange = true;
                log.info("New symbol detected: {}", newSnapshot.getSymbol());
            }

            // Process if quadrant changed or if it's a new symbol
            if (isQuadrantChange || isSignificantPriceChange || !currentSnapshotOpt.isPresent()) {
                // Save history record before updating current snapshot
                saveHistoryRecord(newSnapshot, isQuadrantChange ? "QUADRANT_CHANGE" : "PRICE_UPDATE");

                // Update current snapshot
                saveOrUpdateCurrentSnapshot(newSnapshot);

                // Create notification event
                //NotificationEvent notificationEvent = createNotificationEvent(newSnapshot, isQuadrantChange);
                //notificationEventRepository.save(notificationEvent);

                // Send real-time update
                DealingRangeDto dto = convertToDto(newSnapshot);
                dto.setQuadrantChanged(isQuadrantChange);
                messagingTemplate.convertAndSend("/topic/dealing-range", dto);
                messagingTemplate.convertAndSend("/topic/dealing-range/" + newSnapshot.getSymbol(), dto);

                log.info(
                    "Successfully processed dealing range for {}: {} at {} (quadrant_changed: {})",
                    newSnapshot.getSymbol(),
                    newSnapshot.getCurrentQuadrant().getDisplayName(),
                    newSnapshot.getCurrentPrice(),
                    isQuadrantChange
                );
            } else {
                log.debug("No significant change for {}, skipping processing", newSnapshot.getSymbol());
            }
        } catch (Exception e) {
            log.error("Error processing dealing range webhook: {}", jsonPayload, e);
        }
    }

    // ========================================================================
    // CURRENT STATUS QUERIES
    // ========================================================================

    /**
     * Get current quadrant for symbol and timeframe
     */
    public Optional<DealingRangeDto> getCurrentQuadrant(String symbol, EventInterval interval) {
        if (StringUtils.isBlank(symbol)) {
            return Optional.empty();
        }

        return dealingRangeSnapshotRepository.findBySymbolAndInterval(symbol.toUpperCase(), interval).map(this::convertToDto);
    }

    /**
     * Get current quadrant for symbol (latest interval)
     */
    public Optional<DealingRangeDto> getCurrentQuadrant(String symbol) {
        if (StringUtils.isBlank(symbol)) {
            return Optional.empty();
        }

        return dealingRangeSnapshotRepository.findBySymbol(symbol.toUpperCase()).map(this::convertToDto);
    }

    /**
     * Get all symbols with their current quadrants for a specific interval
     */
    public List<DealingRangeDto> getCurrentQuadrants() {
        return dealingRangeSnapshotRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Get all symbols with their current quadrants for a specific interval
     */
    public List<DealingRangeDto> getCurrentQuadrants(EventInterval interval) {
        return dealingRangeSnapshotRepository.findByInterval(interval).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Get symbols currently in specific quadrant
     */
    public List<DealingRangeDto> getSymbolsInQuadrant(Quadrant quadrant, EventInterval interval) {
        return dealingRangeSnapshotRepository
            .findByCurrentQuadrantAndInterval(quadrant, interval)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // ========================================================================
    // HISTORY QUERIES
    // ========================================================================

    /**
     * Get quadrant change history for a symbol
     */
    public List<DealingRangeHistoryDto> getQuadrantHistory(String symbol, EventInterval interval, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));

        return dealingRangeHistoryRepository
            .findBySymbolAndIntervalOrderByTimestampDesc(symbol.toUpperCase(), interval, pageRequest)
            .stream()
            .map(this::convertHistoryToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get recent quadrant changes across all symbols
     */
    public List<DealingRangeHistoryDto> getRecentQuadrantChanges(EventInterval interval, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));

        return dealingRangeHistoryRepository
            .findByIntervalAndEventTypeOrderByTimestampDesc(interval, "QUADRANT_CHANGE", pageRequest)
            .stream()
            .map(this::convertHistoryToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get quadrant changes in time range
     */
    public List<DealingRangeHistoryDto> getQuadrantChangesBetween(
        String symbol,
        EventInterval interval,
        ZonedDateTime start,
        ZonedDateTime end
    ) {
        return dealingRangeHistoryRepository
            .findBySymbolAndIntervalAndTimestampBetweenOrderByTimestampDesc(symbol.toUpperCase(), interval, start, end)
            .stream()
            .map(this::convertHistoryToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get statistics for symbol's quadrant distribution over time
     */
    public Map<String, Object> getQuadrantStatistics(String symbol, EventInterval interval, int days) {
        ZonedDateTime since = ZonedDateTime.now().minusDays(days);

        List<DealingRangeHistory> history = dealingRangeHistoryRepository.findBySymbolAndIntervalAndTimestampAfterOrderByTimestampDesc(
            symbol.toUpperCase(),
            interval,
            since
        );

        Map<Quadrant, Long> quadrantCounts = history
            .stream()
            .collect(Collectors.groupingBy(DealingRangeHistory::getCurrentQuadrant, Collectors.counting()));

        long totalEvents = history.size();
        Map<String, Double> quadrantPercentages = quadrantCounts
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getKey().name(),
                    entry -> totalEvents > 0 ? ((entry.getValue() * 100.0) / totalEvents) : 0.0
                )
            );

        Map<String, Object> stats = new HashMap<>();
        stats.put("symbol", symbol);
        stats.put("interval", interval);
        stats.put("days", days);
        stats.put("totalEvents", totalEvents);
        stats.put("quadrantCounts", quadrantCounts);
        stats.put("quadrantPercentages", quadrantPercentages);

        return stats;
    }

    // ========================================================================
    // MONITORING AND ALERTS
    // ========================================================================

    /**
     * Check for symbols that have been in extreme positions for extended periods
     */
    public List<DealingRangeDto> findSymbolsInExtremesForDuration(EventInterval interval, int minutes) {
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(minutes);

        // Find symbols currently in extremes
        List<DealingRangeSnapshot> extremeSnapshots = dealingRangeSnapshotRepository.findExtremePositionsByInterval(interval);

        List<DealingRangeDto> result = new ArrayList<>();

        for (DealingRangeSnapshot snapshot : extremeSnapshots) {
            // Check if they've been in extremes for the specified duration
            List<DealingRangeHistory> recentHistory =
                dealingRangeHistoryRepository.findBySymbolAndIntervalAndTimestampAfterOrderByTimestampDesc(
                    snapshot.getSymbol(),
                    interval,
                    threshold
                );

            boolean stuckInExtreme = recentHistory
                .stream()
                .allMatch(
                    h ->
                        h.getCurrentQuadrant() == Quadrant.BREACH_ABOVE_RANGE ||
                        h.getCurrentQuadrant() == Quadrant.BREACH_BELOW_RANGE ||
                        h.getCurrentQuadrant() == Quadrant.Q1_75_100 ||
                        h.getCurrentQuadrant() == Quadrant.Q4_0_25
                );

            if (stuckInExtreme) {
                DealingRangeDto dto = convertToDto(snapshot);
                dto.setMinutesInCurrentQuadrant(minutes);
                result.add(dto);
            }
        }

        return result;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private void saveHistoryRecord(DealingRangeSnapshot snapshot, String eventType) {
        DealingRangeHistory historyRecord = DealingRangeHistory.builder()
            .symbol(snapshot.getSymbol())
            .currentPrice(snapshot.getCurrentPrice())
            .currentQuadrant(snapshot.getCurrentQuadrant())
            .rangeHigh(snapshot.getRangeHigh())
            .rangeLow(snapshot.getRangeLow())
            .rangeSize(snapshot.getRangeSize())
            .q1Level(snapshot.getQ1Level())
            .q2Level(snapshot.getQ2Level())
            .q3Level(snapshot.getQ3Level())
            .chartTimeframe(snapshot.getChartTimeframe())
            .interval(snapshot.getInterval())
            .timestamp(snapshot.getRangeCalculatedAt())
            .eventType(eventType)
            .alertMessage(snapshot.getAlertMessage())
            .source(snapshot.getSource())
            .lookbackBars(snapshot.getLookbackBars())
            .build();

        dealingRangeHistoryRepository.save(historyRecord);
        log.debug("Saved history record for {} - {}", snapshot.getSymbol(), eventType);
    }

    private void saveOrUpdateCurrentSnapshot(DealingRangeSnapshot newSnapshot) {
        Optional<DealingRangeSnapshot> existingOpt = dealingRangeSnapshotRepository.findBySymbolAndInterval(
            newSnapshot.getSymbol(),
            newSnapshot.getInterval()
        );

        if (existingOpt.isPresent()) {
            DealingRangeSnapshot existing = existingOpt.get();
            updateSnapshot(existing, newSnapshot);
            dealingRangeSnapshotRepository.save(existing);
            log.debug("Updated snapshot for {} ({})", existing.getSymbol(), existing.getInterval());
        } else {
            dealingRangeSnapshotRepository.save(newSnapshot);
            log.debug("Created new snapshot for {} ({})", newSnapshot.getSymbol(), newSnapshot.getInterval());
        }
    }

    private NotificationEvent createNotificationEvent(DealingRangeSnapshot snapshot, boolean isQuadrantChange) {
        return NotificationEvent.builder()
            .created(ZonedDateTime.now())
            .symbol(snapshot.getSymbol())
            .datetime(snapshot.getRangeCalculatedAt())
            .candleType(CandleType.CLASSIC.name())
            .direction(isQuadrantChange ? Direction.UNKNOWN.name() : Direction.NEUTRAL.name())
            .source(IndicatorSource.TRADING_VIEW.name())
            .tradeSignalProcessStatus(ProcessingStatus.PROCESSED.name())
            .indicatorDisplayName(Indicator.DEALING_RANGE.name())
            .indicator(Indicator.DEALING_RANGE.name())
            .indicatorSubCategory(snapshot.getCurrentQuadrant().name())
            .indicatorSubCategoryDisplayName(snapshot.getCurrentQuadrant().getDisplayName())
            .lastUpdated(ZonedDateTime.now())
            .rawAlertMsg(snapshot.getAlertMessage())
            .rawPayload(snapshot.getAlertMessage())
            .interval(snapshot.getInterval().name())
            .price(snapshot.getCurrentPrice())
            .isAlertable(isQuadrantChange)
            .isStrategy(false)
            .strategyName(Strategy.NONE.name())
            .strategyProcessStatus(ProcessingStatus.NOT_APPLICABLE.name())
            .build();
    }

    private DealingRangeSnapshot createSnapshotFromJson(String jsonPayload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);

            String symbol = getCleanedSymbol(getJsonStringValue(jsonNode, "ticker"));
            if (StringUtils.isBlank(symbol)) {
                log.warn("Missing ticker in JSON payload");
                return null;
            }

            BigDecimal currentPrice = getJsonDecimalValue(jsonNode, "price");
            String quadrantStr = getJsonStringValue(jsonNode, "quadrant");
            Quadrant quadrant = parseQuadrant(quadrantStr);

            if (currentPrice == null || quadrant == Quadrant.UNKNOWN) {
                log.warn("Missing required fields: price={}, quadrant={}", currentPrice, quadrant);
                return null;
            }

            // Parse range fields
            BigDecimal rangeHigh = getJsonDecimalValue(jsonNode, "range_high");
            BigDecimal rangeLow = getJsonDecimalValue(jsonNode, "range_low");
            BigDecimal rangeSize = getJsonDecimalValue(jsonNode, "range_size");

            // Parse quadrant levels
            BigDecimal q1Level = getJsonDecimalValue(jsonNode, "q1_low");
            BigDecimal q2Level = getJsonDecimalValue(jsonNode, "q2_low");
            BigDecimal q3Level = getJsonDecimalValue(jsonNode, "q3_low");

            // Parse interval
            String intervalStr = getJsonStringValue(jsonNode, "interval");
            String chartTimeframe = getJsonStringValue(jsonNode, "chart_tf", "UNKNOWN");
            EventInterval interval = determineInterval(intervalStr, chartTimeframe);

            // Parse event type
            String eventType = getJsonStringValue(jsonNode, "event_type", "STATUS_UPDATE");

            // Parse timestamp
            ZonedDateTime eventTime = parseJsonTimestamp(jsonNode);

            return DealingRangeSnapshot.builder()
                .symbol(symbol.toUpperCase())
                .currentPrice(currentPrice)
                .currentQuadrant(quadrant)
                .rangeHigh(rangeHigh)
                .rangeLow(rangeLow)
                .rangeSize(rangeSize)
                .q1Level(q1Level)
                .q2Level(q2Level)
                .q3Level(q3Level)
                .chartTimeframe(chartTimeframe)
                .interval(interval)
                .lastUpdated(ZonedDateTime.now())
                .rangeCalculatedAt(eventTime)
                .alertMessage(getJsonStringValue(jsonNode, "alert_message", jsonPayload))
                .source(getJsonStringValue(jsonNode, "source", "WEBHOOK"))
                .lookbackBars(getJsonIntegerValue(jsonNode, "lookback_bars", 20))
                .build();
        } catch (Exception e) {
            log.error("Error creating snapshot from JSON: {}", jsonPayload, e);
            return null;
        }
    }

    // ========================================================================
    // CONVERSION METHODS
    // ========================================================================

    private DealingRangeDto convertToDto(DealingRangeSnapshot snapshot) {
        return DealingRangeDto.builder()
            .symbol(snapshot.getSymbol())
            .currentPrice(snapshot.getCurrentPrice())
            .currentQuadrant(snapshot.getCurrentQuadrant())
            .quadrantDisplayName(snapshot.getCurrentQuadrant().getDisplayName())
            .quadrantPercentage(snapshot.getQuadrantPercentage())
            .rangeHigh(snapshot.getRangeHigh())
            .rangeLow(snapshot.getRangeLow())
            .rangeSize(snapshot.getRangeSize())
            .q1Level(snapshot.getQ1Level())
            .q2Level(snapshot.getQ2Level())
            .q3Level(snapshot.getQ3Level())
            .chartTimeframe(snapshot.getChartTimeframe())
            .interval(snapshot.getInterval())
            .lastUpdated(snapshot.getLastUpdated())
            .isInRange(snapshot.isInRange())
            .isExtremePosition(snapshot.isExtremePosition())
            .alertMessage(snapshot.getAlertMessage())
            .quadrantChanged(false) // Will be set by calling method if needed
            .build();
    }

    private DealingRangeHistoryDto convertHistoryToDto(DealingRangeHistory history) {
        DealingRangeHistoryDto dto = DealingRangeHistoryDto.builder()
            .symbol(history.getSymbol())
            .currentPrice(history.getCurrentPrice())
            .currentQuadrant(history.getCurrentQuadrant())
            .quadrantDisplayName(history.getCurrentQuadrant().getDisplayName())
            .quadrantPercentage(history.getQuadrantPercentage())
            .rangeHigh(history.getRangeHigh())
            .rangeLow(history.getRangeLow())
            .rangeSize(history.getRangeSize())
            .q1Level(history.getQ1Level())
            .q2Level(history.getQ2Level())
            .q3Level(history.getQ3Level())
            .interval(history.getInterval())
            .timestamp(history.getTimestamp())
            .eventType(history.getEventType())
            .isInRange(history.isInRange())
            .isExtremePosition(history.isExtremePosition())
            .alertMessage(history.getAlertMessage())
            .build();

        // Calculate minutes ago
        dto.setMinutesAgo(dto.getMinutesAgo());
        return dto;
    }

    private void updateSnapshot(DealingRangeSnapshot existing, DealingRangeSnapshot newData) {
        existing.setCurrentPrice(newData.getCurrentPrice());
        existing.setCurrentQuadrant(newData.getCurrentQuadrant());
        existing.setRangeHigh(newData.getRangeHigh());
        existing.setRangeLow(newData.getRangeLow());
        existing.setQ1Level(newData.getQ1Level());
        existing.setQ2Level(newData.getQ2Level());
        existing.setQ3Level(newData.getQ3Level());
        existing.setChartTimeframe(newData.getChartTimeframe());
        existing.setInterval(newData.getInterval());
        existing.setLastUpdated(ZonedDateTime.now());
        existing.setRangeCalculatedAt(newData.getRangeCalculatedAt());
        existing.setAlertMessage(newData.getAlertMessage());
        existing.setSource(newData.getSource());
        existing.setLookbackBars(newData.getLookbackBars());
        existing.setRangeSize(newData.getRangeSize());
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String getCleanedSymbol(String symbol) {
        return StringUtils.replaceChars(symbol, "1!", "").replace("2!", "").replace("!", "");
    }

    private EventInterval determineInterval(String intervalStr, String chartTimeframe) {
        if (StringUtils.isNotBlank(intervalStr)) {
            EventInterval interval = EventInterval.getFromValue(intervalStr);
            if (interval != EventInterval.NA) {
                return interval;
            }
        }
        return convertChartTimeframeToInterval(chartTimeframe);
    }

    // ========================================================================
    // JSON PARSING HELPER METHODS
    // ========================================================================

    private String getJsonStringValue(JsonNode jsonNode, String fieldName) {
        return getJsonStringValue(jsonNode, fieldName, null);
    }

    private String getJsonStringValue(JsonNode jsonNode, String fieldName, String defaultValue) {
        JsonNode field = jsonNode.get(fieldName);
        if (field != null && !field.isNull()) {
            return field.asText();
        }
        return defaultValue;
    }

    private BigDecimal getJsonDecimalValue(JsonNode jsonNode, String fieldName) {
        JsonNode field = jsonNode.get(fieldName);
        if (field != null && !field.isNull()) {
            try {
                return new BigDecimal(field.asText());
            } catch (NumberFormatException e) {
                log.warn("Could not parse decimal from field {}: {}", fieldName, field.asText());
            }
        }
        return null;
    }

    private Integer getJsonIntegerValue(JsonNode jsonNode, String fieldName, Integer defaultValue) {
        JsonNode field = jsonNode.get(fieldName);
        if (field != null && !field.isNull()) {
            try {
                return Integer.parseInt(field.asText());
            } catch (NumberFormatException e) {
                log.warn("Could not parse integer from field {}: {}", fieldName, field.asText());
            }
        }
        return defaultValue;
    }

    private BigDecimal[] parseRange(String rangeStr) {
        BigDecimal[] result = { null, null };

        if (StringUtils.isBlank(rangeStr)) {
            return result;
        }

        try {
            String[] parts = rangeStr.split("-");
            if (parts.length == 2) {
                result[0] = new BigDecimal(parts[0].trim()); // rangeLow
                result[1] = new BigDecimal(parts[1].trim()); // rangeHigh
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse range: {}", rangeStr);
        }

        return result;
    }

    private ZonedDateTime parseJsonTimestamp(JsonNode jsonNode) {
        // Try different timestamp field names
        String[] timestampFields = { "timestamp", "time", "datetime" };

        for (String field : timestampFields) {
            JsonNode timeField = jsonNode.get(field);
            if (timeField != null && !timeField.isNull()) {
                return parseTimestamp(timeField.asText());
            }
        }

        return ZonedDateTime.now();
    }

    private Quadrant parseQuadrant(String value) {
        if (StringUtils.isBlank(value)) {
            return Quadrant.UNKNOWN;
        }
        return Quadrant.fromString(value);
    }

    private ZonedDateTime parseTimestamp(String timeStr) {
        if (StringUtils.isBlank(timeStr)) {
            return ZonedDateTime.now();
        }

        try {
            long timestamp = Long.parseLong(timeStr);

            // Determine if it's milliseconds or seconds
            if (timestamp > 1_000_000_000_000L) {
                return Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("UTC"));
            } else if (timestamp > 1_000_000_000L) {
                return Instant.ofEpochSecond(timestamp).atZone(ZoneId.of("UTC"));
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse timestamp: {}", timeStr);
        }

        return ZonedDateTime.now();
    }

    /**
     * Convert chart timeframe string to EventInterval enum
     * Examples: "60" -> H1, "15" -> M15, "240" -> H4, "1440" -> D1
     */
    private EventInterval convertChartTimeframeToInterval(String chartTimeframe) {
        if (StringUtils.isBlank(chartTimeframe)) {
            return EventInterval.NA;
        }

        try {
            // Try direct string match first (e.g., "1h", "15m", "1d")
            EventInterval directMatch = EventInterval.getFromValue(chartTimeframe);
            if (directMatch != EventInterval.NA) {
                return directMatch;
            }

            // Try parsing as numeric minutes
            int minutes = Integer.parseInt(chartTimeframe);
            return switch (minutes) {
                case 1 -> EventInterval.M1;
                case 2 -> EventInterval.M2;
                case 3 -> EventInterval.M3;
                case 5 -> EventInterval.M5;
                case 15 -> EventInterval.M15;
                case 30 -> EventInterval.M30;
                case 60 -> EventInterval.H1;
                case 240 -> EventInterval.H4;
                case 1440 -> EventInterval.D1;
                case 10080 -> EventInterval.W1;
                default -> EventInterval.NA;
            };
        } catch (NumberFormatException e) {
            log.warn("Could not convert chart timeframe to interval: {}", chartTimeframe);
            return EventInterval.NA;
        }
    }

    // ========================================================================
    // TESTING AND SIMULATION METHODS
    // ========================================================================

    public void createJsonTestData() {
        log.info("Creating JSON test dealing range data with intervals...");

        simulateDealingRangeJsonAlert(
            "NQ1!",
            new BigDecimal("21846.5"),
            Quadrant.Q4_0_25,
            new BigDecimal("22097.75"),
            new BigDecimal("21790"),
            "60",
            EventInterval.H1
        );

        simulateDealingRangeJsonAlert(
            "ES1!",
            new BigDecimal("5845.25"),
            Quadrant.Q2_50_75,
            new BigDecimal("5900"),
            new BigDecimal("5800"),
            "15",
            EventInterval.M15
        );

        simulateDealingRangeJsonAlert(
            "EURUSD",
            new BigDecimal("1.0650"),
            Quadrant.BREACH_ABOVE_RANGE,
            new BigDecimal("1.0600"),
            new BigDecimal("1.0500"),
            "240",
            EventInterval.H4
        );

        simulateDealingRangeJsonAlert(
            "GBPUSD",
            new BigDecimal("1.2125"),
            Quadrant.Q1_75_100,
            new BigDecimal("1.2200"),
            new BigDecimal("1.2000"),
            "60",
            EventInterval.H1
        );

        simulateDealingRangeJsonAlert(
            "USDJPY",
            new BigDecimal("145.50"),
            Quadrant.Q3_25_50,
            new BigDecimal("148.00"),
            new BigDecimal("144.00"),
            "5",
            EventInterval.M5
        );

        log.info("JSON test dealing range data with intervals created successfully");
    }

    public void simulateDealingRangeJsonAlert(
        String symbol,
        BigDecimal currentPrice,
        Quadrant quadrant,
        BigDecimal rangeHigh,
        BigDecimal rangeLow,
        String chartTimeframe,
        EventInterval interval
    ) {
        try {
            // Create JSON payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("ticker", symbol);
            payload.put("quadrant", quadrant.name());
            payload.put("price", currentPrice);
            payload.put("range_high", rangeHigh);
            payload.put("range_low", rangeLow);
            payload.put("range_size", rangeHigh.subtract(rangeLow));
            payload.put("chart_tf", chartTimeframe);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("event_type", "QUADRANT_CHANGE");

            // Add interval if provided
            if (interval != null && interval != EventInterval.NA) {
                payload.put("interval", interval.getValue());
            }

            // Calculate quadrant levels
            BigDecimal rangeSize = rangeHigh.subtract(rangeLow);
            payload.put("q1_low", rangeLow.add(rangeSize.multiply(BigDecimal.valueOf(0.75))));
            payload.put("q2_low", rangeLow.add(rangeSize.multiply(BigDecimal.valueOf(0.50))));
            payload.put("q3_low", rangeLow.add(rangeSize.multiply(BigDecimal.valueOf(0.25))));

            String jsonPayload = objectMapper.writeValueAsString(payload);

            log.info(
                "Simulating JSON dealing range alert for {}: {} at {} (interval: {})",
                symbol,
                quadrant.getDisplayName(),
                currentPrice,
                interval != null ? interval.getValue() : "from chart_tf"
            );
            processDealingRangeWebhook(jsonPayload);
        } catch (Exception e) {
            log.error("Error simulating JSON dealing range alert", e);
        }
    }
}
