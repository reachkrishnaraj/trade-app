package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import com.kraj.tradeapp.core.service.SignalActionsService;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for testing signal events - CLEAN IMPLEMENTATION
 * Uses SignalActionsService processor interface directly
 */
@RestController
@RequestMapping("/api/v1/testing")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SignalTestEventController {

    private final Logger log = LoggerFactory.getLogger(SignalTestEventController.class);

    private final SignalActionsService signalActionsService; // ONLY DEPENDENCY NEEDED
    private final Random random = new Random();

    // Sample data for realistic testing
    private final String[] SYMBOLS = { "AAPL", "GOOGL", "TSLA", "MSFT", "AMZN", "NVDA", "META", "BTC-USD", "ETH-USD", "SPY" };
    private final String[] INDICATORS = {
        "RSI",
        "MACD",
        "BOLLINGER_BANDS",
        "EMA_CROSSOVER",
        "SUPPORT_RESISTANCE",
        "VOLUME_SPIKE",
        "STOCHASTIC",
    };
    private final String[] INTERVALS = { "15m", "30m", "1h", "2h", "4h", "6h", "1d" };
    private final String[] DIRECTIONS = { "BULL", "BEAR", "NEUTRAL" };

    @Autowired
    public SignalTestEventController(SignalActionsService signalActionsService) {
        this.signalActionsService = signalActionsService; // ONLY DEPENDENCY
    }

    /**
     * POST /simulate-signal : Create random signal using active processor
     * CLEAN IMPLEMENTATION - Uses processor interface directly
     */
    @PostMapping("/simulate-signal")
    public ResponseEntity<Map<String, Object>> simulateRandomSignal() {
        try {
            String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
            String indicator = INDICATORS[random.nextInt(INDICATORS.length)];
            String interval = INTERVALS[random.nextInt(INTERVALS.length)];
            String direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];

            BigDecimal price = generateRealisticPrice(symbol);
            String alertMessage = generateRealisticMessage(indicator, direction);
            BigDecimal score = generateRealisticScore();

            // CLEAN APPROACH - Use SignalActionsService processor interface
            SignalActionDTO signalAction = signalActionsService.createSignalActionFromExternalEvent(
                symbol,
                price,
                indicator,
                indicator, // indicatorDisplayName
                interval,
                alertMessage,
                direction,
                ZonedDateTime.now(),
                score,
                false, // isStrategy
                true, // isAlertable
                true
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Signal created using active processor");
            response.put("processorInfo", signalActionsService.getProcessorInfo());
            response.put(
                "signalAction",
                Map.of(
                    "id",
                    signalAction.getId(),
                    "symbol",
                    signalAction.getSymbol(),
                    "price",
                    signalAction.getPrice(),
                    "indicator",
                    signalAction.getIndicatorName(),
                    "interval",
                    signalAction.getInterval(),
                    "direction",
                    signalAction.getDirection().toString(),
                    "status",
                    signalAction.getStatus().toString(),
                    "signalName",
                    signalAction.getSignalName(),
                    "message",
                    signalAction.getMessage()
                )
            );

            log.info(
                "Created signal using active processor: {} {} for {} at price {}",
                signalAction.getDirection(),
                indicator,
                symbol,
                price
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating test signal: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to create signal: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /simulate-specific-signal : Create specific signal using active processor
     * CLEAN IMPLEMENTATION
     */
    @PostMapping("/simulate-specific-signal")
    public ResponseEntity<Map<String, Object>> simulateSpecificSignal(@RequestBody Map<String, Object> signalRequest) {
        try {
            String symbol = (String) signalRequest.getOrDefault("symbol", "AAPL");
            BigDecimal price = new BigDecimal(signalRequest.getOrDefault("price", "150.00").toString());
            String indicator = (String) signalRequest.getOrDefault("indicator", "RSI");
            String interval = (String) signalRequest.getOrDefault("interval", "1h");
            String alertMessage = (String) signalRequest.getOrDefault("alertMessage", "Test signal message");
            String direction = (String) signalRequest.getOrDefault("direction", "BULL");
            BigDecimal score = signalRequest.get("score") != null
                ? new BigDecimal(signalRequest.get("score").toString())
                : generateRealisticScore();
            boolean isStrategy = Boolean.parseBoolean(signalRequest.getOrDefault("isStrategy", "false").toString());
            boolean isAlertable = Boolean.parseBoolean(signalRequest.getOrDefault("isAlertable", "true").toString());

            // CLEAN APPROACH - Use SignalActionsService processor interface
            SignalActionDTO signalAction = signalActionsService.createSignalActionFromExternalEvent(
                symbol,
                price,
                indicator,
                indicator,
                interval,
                alertMessage,
                direction,
                ZonedDateTime.now(),
                score,
                isStrategy,
                isAlertable,
                true
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Specific signal created using active processor");
            response.put("processorInfo", signalActionsService.getProcessorInfo());
            response.put("signalAction", signalAction);

            log.info(
                "Created specific signal using active processor: {} {} for {} at price {}",
                signalAction.getDirection(),
                indicator,
                symbol,
                price
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating specific signal: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to create specific signal: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /simulate-bulk-signals : Create multiple signals using active processor
     * CLEAN IMPLEMENTATION
     */
    @PostMapping("/simulate-bulk-signals")
    public ResponseEntity<Map<String, Object>> simulateBulkSignals(@RequestParam(defaultValue = "5") int count) {
        try {
            int actualCount = Math.min(count, 20); // Limit to 20 signals max

            for (int i = 0; i < actualCount; i++) {
                String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
                String indicator = INDICATORS[random.nextInt(INDICATORS.length)];
                String interval = INTERVALS[random.nextInt(INTERVALS.length)];
                String direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
                BigDecimal price = generateRealisticPrice(symbol);
                String alertMessage = generateRealisticMessage(indicator, direction);
                BigDecimal score = generateRealisticScore();

                // CLEAN APPROACH - Use SignalActionsService processor interface
                signalActionsService.createSignalActionFromExternalEvent(
                    symbol,
                    price,
                    indicator,
                    indicator,
                    interval,
                    alertMessage,
                    direction,
                    ZonedDateTime.now(),
                    score,
                    false,
                    true,
                    true
                );

                // Small delay between signals
                Thread.sleep(100);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", actualCount + " signals created using active processor");
            response.put("count", actualCount);
            response.put("processorInfo", signalActionsService.getProcessorInfo());

            log.info("Created {} bulk signals using active processor", actualCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating bulk signals: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to create bulk signals: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /start-auto-simulation : Start automatic signal generation
     * CLEAN IMPLEMENTATION
     */
    @PostMapping("/start-auto-simulation")
    public ResponseEntity<Map<String, String>> startAutoSimulation() {
        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
                    String indicator = INDICATORS[random.nextInt(INDICATORS.length)];
                    String interval = INTERVALS[random.nextInt(INTERVALS.length)];
                    String direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
                    BigDecimal price = generateRealisticPrice(symbol);
                    String alertMessage = generateRealisticMessage(indicator, direction);
                    BigDecimal score = generateRealisticScore();

                    // CLEAN APPROACH - Use SignalActionsService processor interface
                    signalActionsService.createSignalActionFromExternalEvent(
                        symbol,
                        price,
                        indicator,
                        indicator,
                        interval,
                        alertMessage,
                        direction,
                        ZonedDateTime.now(),
                        score,
                        false,
                        true,
                        true
                    );

                    Thread.sleep(3000); // Wait 3 seconds between signals
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Auto simulation interrupted");
            } catch (Exception e) {
                log.error("Error in auto simulation: {}", e.getMessage());
            }
        }).start();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Auto simulation started - will generate 10 signals using active processor over 30 seconds");

        log.info("Started auto signal simulation using active processor");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /signal-stats : Get current signal statistics
     * INCLUDES PROCESSOR INFO
     */
    @GetMapping("/signal-stats")
    public ResponseEntity<Map<String, Object>> getSignalStats() {
        try {
            Map<SignalActionDTO.SignalStatus, Long> countsByStatus = signalActionsService.getSignalActionCountsByStatus();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", signalActionsService.getAllSignalActions().size());
            stats.put("pending", countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.PENDING, 0L));
            stats.put("executed", countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.EXECUTED, 0L));
            stats.put("cancelled", countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.CANCELLED, 0L));
            stats.put("uniqueSymbols", signalActionsService.getUniqueSymbols().size());
            stats.put("uniqueIndicators", signalActionsService.getUniqueIndicatorNames().size());
            stats.put("processorInfo", signalActionsService.getProcessorInfo()); // SHOWS ACTIVE PROCESSOR

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting signal stats: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /processor-info : Get active processor information
     * NEW ENDPOINT
     */
    @GetMapping("/processor-info")
    public ResponseEntity<Map<String, Object>> getProcessorInfo() {
        try {
            Map<String, Object> processorInfo = signalActionsService.getProcessorInfo();
            return ResponseEntity.ok(processorInfo);
        } catch (Exception e) {
            log.error("Error getting processor info: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /clear-signals : Clear all signals for testing
     * UNCHANGED
     */
    @DeleteMapping("/clear-signals")
    public ResponseEntity<Map<String, String>> clearAllSignals() {
        try {
            signalActionsService.clearAllSignalActions();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All signals cleared successfully");

            log.info("Cleared all signals via test controller");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing signals: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to clear signals: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private BigDecimal generateRealisticPrice(String symbol) {
        switch (symbol) {
            case "AAPL":
                return new BigDecimal(145 + random.nextDouble() * 10).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "GOOGL":
                return new BigDecimal(2700 + random.nextDouble() * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "TSLA":
                return new BigDecimal(200 + random.nextDouble() * 50).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "MSFT":
                return new BigDecimal(370 + random.nextDouble() * 20).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "AMZN":
                return new BigDecimal(3150 + random.nextDouble() * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "NVDA":
                return new BigDecimal(440 + random.nextDouble() * 30).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "META":
                return new BigDecimal(310 + random.nextDouble() * 25).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "BTC-USD":
                return new BigDecimal(41000 + random.nextDouble() * 2000).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "ETH-USD":
                return new BigDecimal(2400 + random.nextDouble() * 200).setScale(2, BigDecimal.ROUND_HALF_UP);
            case "SPY":
                return new BigDecimal(450 + random.nextDouble() * 20).setScale(2, BigDecimal.ROUND_HALF_UP);
            default:
                return new BigDecimal(100 + random.nextDouble() * 50).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    private String generateRealisticMessage(String indicator, String direction) {
        String actionWord = direction.equals("BULL") ? "bullish" : direction.equals("BEAR") ? "bearish" : "neutral";

        switch (indicator) {
            case "RSI":
                return String.format(
                    "RSI indicates %s conditions with potential %s opportunity",
                    direction.equals("BULL") ? "oversold" : direction.equals("BEAR") ? "overbought" : "neutral",
                    actionWord
                );
            case "MACD":
                return String.format("MACD shows %s crossover with %s momentum building", actionWord, actionWord);
            case "BOLLINGER_BANDS":
                return String.format("Price action near Bollinger Bands suggests %s movement potential", actionWord);
            case "EMA_CROSSOVER":
                return String.format("EMA crossover indicates %s trend formation with strong momentum", actionWord);
            case "SUPPORT_RESISTANCE":
                return String.format(
                    "Key %s level shows potential %s opportunity with volume confirmation",
                    direction.equals("BULL") ? "support" : "resistance",
                    actionWord
                );
            case "VOLUME_SPIKE":
                return String.format("Volume analysis confirms %s pressure with unusual activity", actionWord);
            case "STOCHASTIC":
                return String.format(
                    "Stochastic oscillator shows %s conditions with %s divergence",
                    direction.equals("BULL") ? "oversold" : direction.equals("BEAR") ? "overbought" : "neutral",
                    actionWord
                );
            default:
                return String.format("%s indicator shows %s signal for potential %s move", indicator, actionWord, actionWord);
        }
    }

    private BigDecimal generateRealisticScore() {
        // Generate score between 40-95 for realistic testing
        return new BigDecimal(40 + random.nextDouble() * 55).setScale(1, BigDecimal.ROUND_HALF_UP);
    }
}
