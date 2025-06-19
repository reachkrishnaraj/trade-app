package com.kraj.tradeapp.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraj.tradeapp.core.model.DealingRangeSnapshot;
import com.kraj.tradeapp.core.model.EventInterval;
import com.kraj.tradeapp.core.model.Quadrant;
import com.kraj.tradeapp.core.model.dto.DealingRangeDto;
import com.kraj.tradeapp.core.repository.mongodb.DealingRangeSnapshotRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealingRangeService {

    private final DealingRangeSnapshotRepository dealingRangeSnapshotRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========================================================================
    // MAIN PROCESSING METHODS
    // ========================================================================

    /**
     * Main entry point for processing dealing range webhook with JSON payload
     */
    public void processDealingRangeWebhook(String jsonPayload) {
        try {
            log.info("Processing dealing range JSON webhook: {}", jsonPayload);

            DealingRangeSnapshot snapshot = createSnapshotFromJson(jsonPayload);
            if (snapshot == null) {
                log.warn("Could not create snapshot from JSON payload: {}", jsonPayload);
                return;
            }

            saveOrUpdateSnapshot(snapshot);

            // Send real-time update
            DealingRangeDto dto = convertToDto(snapshot);
            messagingTemplate.convertAndSend("/topic/dealing-range", dto);

            log.info(
                "Successfully processed dealing range for {}: {} at {}",
                snapshot.getSymbol(),
                snapshot.getCurrentQuadrant().getDisplayName(),
                snapshot.getCurrentPrice()
            );
        } catch (Exception e) {
            log.error("Error processing dealing range JSON webhook: {}", jsonPayload, e);
        }
    }

    /**
     * Process multiple dealing range updates in batch
     */
    public void processBatchDealingRangeWebhooks(List<String> payloads) {
        log.info("Processing {} dealing range webhooks in batch", payloads.size());

        for (String payload : payloads) {
            try {
                processDealingRangeWebhook(payload);
            } catch (Exception e) {
                log.error("Error processing payload in batch: {}", payload, e);
            }
        }

        log.info("Completed batch processing of {} dealing range webhooks", payloads.size());
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================

    public Optional<DealingRangeDto> getCurrentDealingRange(String symbol) {
        if (StringUtils.isBlank(symbol)) {
            return Optional.empty();
        }
        return dealingRangeSnapshotRepository.findBySymbol(symbol.toUpperCase()).map(this::convertToDto);
    }

    public List<DealingRangeDto> getSymbolsInQuadrant(Quadrant quadrant) {
        return dealingRangeSnapshotRepository.findByCurrentQuadrant(quadrant).stream().map(this::convertToDto).toList();
    }

    public List<DealingRangeDto> getSymbolsInQuadrantByInterval(Quadrant quadrant, EventInterval interval) {
        return dealingRangeSnapshotRepository
            .findByCurrentQuadrantAndInterval(quadrant, interval)
            .stream()
            .map(this::convertToDto)
            .toList();
    }

    public Optional<DealingRangeDto> getDealingRangeByInterval(String symbol, EventInterval interval) {
        if (StringUtils.isBlank(symbol)) {
            return Optional.empty();
        }
        return dealingRangeSnapshotRepository.findBySymbolAndInterval(symbol.toUpperCase(), interval).map(this::convertToDto);
    }

    public List<DealingRangeDto> getDealingRangesByInterval(EventInterval interval) {
        return dealingRangeSnapshotRepository.findByInterval(interval).stream().map(this::convertToDto).toList();
    }

    public List<DealingRangeDto> getExtremePositions() {
        return dealingRangeSnapshotRepository.findExtremePositions().stream().map(this::convertToDto).toList();
    }

    public List<DealingRangeDto> getExtremePositionsByInterval(EventInterval interval) {
        return dealingRangeSnapshotRepository.findExtremePositionsByInterval(interval).stream().map(this::convertToDto).toList();
    }

    public List<DealingRangeDto> getSymbolsWithinRange() {
        return dealingRangeSnapshotRepository.findWithinRange().stream().map(this::convertToDto).toList();
    }

    public List<DealingRangeDto> getSymbolsWithinRangeByInterval(EventInterval interval) {
        return dealingRangeSnapshotRepository.findWithinRangeByInterval(interval).stream().map(this::convertToDto).toList();
    }

    public List<DealingRangeDto> getDealingRangeSummary(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return dealingRangeSnapshotRepository.findAll().stream().map(this::convertToDto).toList();
        }

        return symbols
            .stream()
            .map(String::toUpperCase)
            .map(dealingRangeSnapshotRepository::findBySymbol)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::convertToDto)
            .toList();
    }

    public Map<Quadrant, Long> getQuadrantCounts() {
        List<DealingRangeSnapshot> allSnapshots = dealingRangeSnapshotRepository.findAll();
        return allSnapshots.stream().collect(Collectors.groupingBy(DealingRangeSnapshot::getCurrentQuadrant, Collectors.counting()));
    }

    public Map<Quadrant, Long> getQuadrantCountsByInterval(EventInterval interval) {
        List<DealingRangeSnapshot> snapshots = dealingRangeSnapshotRepository.findByInterval(interval);
        return snapshots.stream().collect(Collectors.groupingBy(DealingRangeSnapshot::getCurrentQuadrant, Collectors.counting()));
    }

    // ========================================================================
    // TESTING METHODS
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
            Quadrant.ABOVE_RANGE,
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
            payload.put("update", "1M Update");
            payload.put("chart_tf", chartTimeframe);
            payload.put("quadrant", quadrant.name());
            payload.put("price", currentPrice);
            payload.put("range", rangeLow + "-" + rangeHigh);
            payload.put("time", System.currentTimeMillis());

            // Add interval if provided
            if (interval != null && interval != EventInterval.NA) {
                payload.put("interval", interval.getValue());
            }

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

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private DealingRangeSnapshot createSnapshotFromJson(String jsonPayload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);

            // Parse required fields
            String symbol = getJsonStringValue(jsonNode, "ticker");
            if (StringUtils.isBlank(symbol)) {
                log.warn("Missing ticker in dealing range JSON data");
                return null;
            }

            BigDecimal currentPrice = getJsonDecimalValue(jsonNode, "price");
            String quadrantStr = getJsonStringValue(jsonNode, "quadrant");
            Quadrant quadrant = parseQuadrant(quadrantStr);

            if (currentPrice == null || quadrant == Quadrant.UNKNOWN) {
                log.warn("Missing required fields: price={}, quadrant={}", currentPrice, quadrant);
                return null;
            }

            // Parse range field (format: "low-high")
            String rangeStr = getJsonStringValue(jsonNode, "range");
            BigDecimal[] rangeBounds = parseRange(rangeStr);
            BigDecimal rangeLow = rangeBounds[0];
            BigDecimal rangeHigh = rangeBounds[1];

            // Parse interval fields - try both 'interval' and 'chart_tf'
            String intervalStr = getJsonStringValue(jsonNode, "interval");
            String chartTimeframe = getJsonStringValue(jsonNode, "chart_tf", "UNKNOWN");

            // If interval not provided, try to parse from chart_tf
            EventInterval interval = EventInterval.NA;
            if (StringUtils.isNotBlank(intervalStr)) {
                interval = EventInterval.getFromValue(intervalStr);
            } else if (StringUtils.isNotBlank(chartTimeframe) && !chartTimeframe.equals("UNKNOWN")) {
                // Convert chart_tf format to interval format
                interval = convertChartTimeframeToInterval(chartTimeframe);
            }

            // Parse optional fields
            String alertMessage = getJsonStringValue(jsonNode, "update");
            String source = getJsonStringValue(jsonNode, "source", "WEBHOOK");
            Integer lookbackBars = getJsonIntegerValue(jsonNode, "lookback_bars", 20);

            // Parse individual quadrant levels if provided
            BigDecimal q1Level = getJsonDecimalValue(jsonNode, "q1_level");
            BigDecimal q2Level = getJsonDecimalValue(jsonNode, "q2_level");
            BigDecimal q3Level = getJsonDecimalValue(jsonNode, "q3_level");

            // Calculate range size and missing levels
            BigDecimal rangeSize = BigDecimal.ZERO;
            if (rangeHigh != null && rangeLow != null) {
                rangeSize = rangeHigh.subtract(rangeLow);

                // Calculate quadrant levels if not provided
                if (rangeSize.compareTo(BigDecimal.ZERO) > 0) {
                    if (q1Level == null) q1Level = rangeLow.add(rangeSize.multiply(BigDecimal.valueOf(0.75)));
                    if (q2Level == null) q2Level = rangeLow.add(rangeSize.multiply(BigDecimal.valueOf(0.50)));
                    if (q3Level == null) q3Level = rangeLow.add(rangeSize.multiply(BigDecimal.valueOf(0.25)));
                }
            }

            // Parse timestamp
            ZonedDateTime eventTime = parseJsonTimestamp(jsonNode);

            return DealingRangeSnapshot.builder()
                .symbol(symbol.toUpperCase())
                .currentPrice(currentPrice)
                .currentQuadrant(quadrant)
                .rangeHigh(rangeHigh)
                .rangeLow(rangeLow)
                .q1Level(q1Level)
                .q2Level(q2Level)
                .q3Level(q3Level)
                .chartTimeframe(chartTimeframe)
                .interval(interval)
                .lastUpdated(ZonedDateTime.now())
                .rangeCalculatedAt(eventTime)
                .alertMessage(alertMessage)
                .source(source)
                .lookbackBars(lookbackBars)
                .rangeSize(rangeSize)
                .build();
        } catch (Exception e) {
            log.error("Error creating snapshot from JSON: {}", jsonPayload, e);
            return null;
        }
    }

    private void saveOrUpdateSnapshot(DealingRangeSnapshot newSnapshot) {
        Optional<DealingRangeSnapshot> existingOpt = dealingRangeSnapshotRepository.findBySymbol(newSnapshot.getSymbol());

        if (existingOpt.isPresent()) {
            // Update existing snapshot (overwrite)
            DealingRangeSnapshot existing = existingOpt.get();
            updateSnapshot(existing, newSnapshot);
            dealingRangeSnapshotRepository.save(existing);

            log.debug("Updated existing snapshot for {}", existing.getSymbol());
        } else {
            // Save new snapshot
            dealingRangeSnapshotRepository.save(newSnapshot);
            log.debug("Created new snapshot for {}", newSnapshot.getSymbol());
        }
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

    private DealingRangeDto convertToDto(DealingRangeSnapshot snapshot) {
        return DealingRangeDto.builder()
            .symbol(snapshot.getSymbol())
            .currentPrice(snapshot.getCurrentPrice())
            .currentQuadrant(snapshot.getCurrentQuadrant())
            .quadrantDisplayName(snapshot.getCurrentQuadrant().getDisplayName())
            .quadrantPercentage(snapshot.getQuadrantPercentage())
            .rangeHigh(snapshot.getRangeHigh())
            .rangeLow(snapshot.getRangeLow())
            .q1Level(snapshot.getQ1Level())
            .q2Level(snapshot.getQ2Level())
            .q3Level(snapshot.getQ3Level())
            .chartTimeframe(snapshot.getChartTimeframe())
            .interval(snapshot.getInterval())
            .lastUpdated(snapshot.getLastUpdated())
            .isInRange(snapshot.isInRange())
            .isExtremePosition(snapshot.isExtremePosition())
            .rangeSize(snapshot.getRangeSize())
            .alertMessage(snapshot.getAlertMessage())
            .build();
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
        String[] timestampFields = { "time", "timestamp", "datetime" };

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
}
