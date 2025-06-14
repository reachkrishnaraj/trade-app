package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import com.kraj.tradeapp.core.service.SignalActionsService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for testing and simulating signal events
 */
@RestController
@RequestMapping("/api/v1/testing")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SignalTestEventController {

    private final Logger log = LoggerFactory.getLogger(SignalTestEventController.class);

    private final SignalActionsService signalActionsService;
    private final Random random = new Random();

    // Sample data for realistic simulation
    private final String[] SYMBOLS = { "AAPL", "GOOGL", "TSLA", "MSFT", "AMZN", "NVDA", "META", "BTC-USD", "ETH-USD", "SPY" };
    private final String[] INDICATORS = { "RSI", "MACD", "Bollinger Bands", "EMA", "Support/Resistance", "Volume", "Stochastic" };
    private final String[] INTERVALS = { "15m", "30m", "1h", "2h", "4h", "6h", "1d" };
    private final String[] SIGNAL_NAMES = {
        "OVERSOLD",
        "OVERBOUGHT",
        "BULLISH_CROSS",
        "BEARISH_CROSS",
        "BREAKOUT",
        "BREAKDOWN",
        "VOLUME_SPIKE",
    };

    @Autowired
    public SignalTestEventController(SignalActionsService signalActionsService) {
        this.signalActionsService = signalActionsService;
    }

    /**
     * POST /simulate-signal : Create and broadcast a random signal for testing
     */
    @PostMapping("/simulate-signal")
    public ResponseEntity<Map<String, Object>> simulateRandomSignal() {
        try {
            String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
            String indicator = INDICATORS[random.nextInt(INDICATORS.length)];
            String interval = INTERVALS[random.nextInt(INTERVALS.length)];
            String signalName = SIGNAL_NAMES[random.nextInt(SIGNAL_NAMES.length)];

            // Generate realistic price based on symbol
            BigDecimal price = generateRealisticPrice(symbol);

            SignalActionDTO.SignalDirection direction = SignalActionDTO.SignalDirection.values()[random.nextInt(3)];
            String message = generateRealisticMessage(indicator, signalName, direction);

            signalActionsService.simulateNewSignal(symbol, price, signalName, indicator, interval, message, direction);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Random signal created and broadcasted");
            response.put(
                "signal",
                Map.of(
                    "symbol",
                    symbol,
                    "price",
                    price,
                    "indicator",
                    indicator,
                    "interval",
                    interval,
                    "direction",
                    direction,
                    "message",
                    message
                )
            );

            log.info("Simulated random signal: {} {} at {} ({})", direction, symbol, price, indicator);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error simulating signal: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to simulate signal: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /simulate-specific-signal : Create a specific signal for testing
     */
    @PostMapping("/simulate-specific-signal")
    public ResponseEntity<Map<String, Object>> simulateSpecificSignal(@RequestBody Map<String, Object> signalRequest) {
        try {
            String symbol = (String) signalRequest.getOrDefault("symbol", "AAPL");
            BigDecimal price = new BigDecimal(signalRequest.getOrDefault("price", "150.00").toString());
            String signalName = (String) signalRequest.getOrDefault("signalName", "TEST_SIGNAL");
            String indicator = (String) signalRequest.getOrDefault("indicator", "RSI");
            String interval = (String) signalRequest.getOrDefault("interval", "1h");
            String message = (String) signalRequest.getOrDefault("message", "Test signal message");
            String directionStr = (String) signalRequest.getOrDefault("direction", "BUY");

            SignalActionDTO.SignalDirection direction = SignalActionDTO.SignalDirection.valueOf(directionStr.toUpperCase());

            signalActionsService.simulateNewSignal(symbol, price, signalName, indicator, interval, message, direction);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Specific signal created and broadcasted");
            response.put("signal", signalRequest);

            log.info("Simulated specific signal: {} {} at {} ({})", direction, symbol, price, indicator);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error simulating specific signal: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to simulate specific signal: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /simulate-bulk-signals : Create multiple signals at once for testing
     */
    @PostMapping("/simulate-bulk-signals")
    public ResponseEntity<Map<String, Object>> simulateBulkSignals(@RequestParam(defaultValue = "5") int count) {
        try {
            for (int i = 0; i < Math.min(count, 20); i++) { // Limit to 20 signals max
                simulateRandomSignal();
                // Small delay between signals to make them more realistic
                Thread.sleep(100);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", count + " signals created and broadcasted");
            response.put("count", count);

            log.info("Simulated {} bulk signals", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error simulating bulk signals: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to simulate bulk signals: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /start-auto-simulation : Start automatic signal generation for testing
     */
    @PostMapping("/start-auto-simulation")
    public ResponseEntity<Map<String, String>> startAutoSimulation() {
        // Note: In a real application, you'd use a proper scheduler like @Scheduled
        // This is just for demonstration
        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    simulateRandomSignal();
                    Thread.sleep(3000); // Wait 3 seconds between signals
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Auto simulation interrupted");
            }
        }).start();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Auto simulation started - will generate 10 signals over 30 seconds");

        log.info("Started auto signal simulation");
        return ResponseEntity.ok(response);
    }

    // Helper methods
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
            default:
                return new BigDecimal(100 + random.nextDouble() * 50).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    private String generateRealisticMessage(String indicator, String signalName, SignalActionDTO.SignalDirection direction) {
        String actionWord = direction == SignalActionDTO.SignalDirection.BUY
            ? "bullish"
            : direction == SignalActionDTO.SignalDirection.SELL ? "bearish" : "neutral";

        switch (indicator) {
            case "RSI":
                return String.format("RSI indicates %s conditions with %s signal", actionWord, signalName.toLowerCase());
            case "MACD":
                return String.format("MACD shows %s momentum with %s pattern", actionWord, signalName.toLowerCase());
            case "Bollinger Bands":
                return String.format("Price action near Bollinger Bands suggests %s movement", actionWord);
            case "EMA":
                return String.format("EMA crossover indicates %s trend formation", actionWord);
            case "Support/Resistance":
                return String.format("Key level %s shows potential %s opportunity", signalName.toLowerCase(), actionWord);
            case "Volume":
                return String.format("Volume analysis confirms %s pressure with %s", actionWord, signalName.toLowerCase());
            default:
                return String.format("%s indicator shows %s signal for potential %s move", indicator, signalName.toLowerCase(), actionWord);
        }
    }
}
