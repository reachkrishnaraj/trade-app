package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import com.kraj.tradeapp.core.service.NotificationProcessorService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to simulate new signal actions for demonstration purposes
 * NOW USES REAL EVENT PIPELINE
 */
@Component
public class SignalActionsScheduler {

    private final Logger log = LoggerFactory.getLogger(SignalActionsScheduler.class);

    private final SignalActionsService signalActionsService;
    private final NotificationProcessorService notificationProcessorService; // ADDED for real events
    private final Random random = new Random();

    // Configuration to enable/disable simulation
    @Value("${trading.scheduler.simulation.enabled:false}")
    private boolean simulationEnabled;

    @Value("${trading.scheduler.auto-processing.enabled:false}")
    private boolean autoProcessingEnabled;

    // Sample data for simulation
    private final List<String> symbols = Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NFLX", "NVDA", "AMD", "INTC");

    private final List<String> indicators = Arrays.asList(
        "RSI",
        "MACD",
        "BOLLINGER_BANDS",
        "EMA_CROSSOVER",
        "SUPPORT_RESISTANCE",
        "VOLUME_SPIKE",
        "STOCHASTIC",
        "MOMENTUM_INDICATOR"
    );

    private final List<String> intervals = Arrays.asList("15m", "30m", "1h", "2h", "4h", "6h", "1d");
    private final List<String> directions = Arrays.asList("BULL", "BEAR", "NEUTRAL");

    @Autowired
    public SignalActionsScheduler(
        SignalActionsService signalActionsService,
        NotificationProcessorService notificationProcessorService // ADDED
    ) {
        this.signalActionsService = signalActionsService;
        this.notificationProcessorService = notificationProcessorService; // ADDED
    }

    /**
     * Simulate new signal actions every 30 seconds (for demo purposes)
     * NOW USES REAL EVENT PIPELINE - Only runs if simulation is enabled
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void simulateNewSignalActions() {
        // Only run if simulation is enabled
        if (!simulationEnabled) {
            return;
        }

        try {
            // Randomly decide if we should generate a new signal (30% chance)
            if (random.nextDouble() < 0.3) {
                String symbol = symbols.get(random.nextInt(symbols.size()));
                String indicator = indicators.get(random.nextInt(indicators.size()));
                String interval = intervals.get(random.nextInt(intervals.size()));
                String direction = directions.get(random.nextInt(directions.size()));

                // Generate realistic price based on symbol
                BigDecimal price = generateRealisticPrice(symbol);
                String alertMessage = generateAlertMessage(indicator, direction);

                // USE REAL EVENT PIPELINE instead of old simulation
                notificationProcessorService.simulateRealTradingViewWebhook(symbol, price, indicator, direction, interval, alertMessage);

                log.info("Generated new real event: {} {} for {} at ${} ({})", direction, indicator, symbol, price, interval);
            }
        } catch (Exception e) {
            log.error("Error generating simulated real event", e);
        }
    }

    /**
     * Simulate random execution/cancellation of pending signals every 45 seconds
     * This still works with the existing service methods
     */
    @Scheduled(fixedRate = 45000) // 45 seconds
    public void simulateSignalProcessing() {
        // Only run if auto-processing is enabled
        if (!autoProcessingEnabled) {
            return;
        }

        try {
            var allSignals = signalActionsService.getAllSignalActions();
            var pendingSignals = allSignals.stream().filter(signal -> signal.getStatus() == SignalActionDTO.SignalStatus.PENDING).toList();

            if (!pendingSignals.isEmpty() && random.nextDouble() < 0.4) { // 40% chance
                var signalToProcess = pendingSignals.get(random.nextInt(pendingSignals.size()));

                // 70% chance to execute, 30% chance to cancel
                if (random.nextDouble() < 0.7) {
                    signalActionsService.executeSignalAction(signalToProcess.getId());
                    log.info("Auto-executed signal: {} for {}", signalToProcess.getSignalName(), signalToProcess.getSymbol());
                } else {
                    signalActionsService.cancelSignalAction(signalToProcess.getId());
                    log.info("Auto-cancelled signal: {} for {}", signalToProcess.getSignalName(), signalToProcess.getSymbol());
                }
            }
        } catch (Exception e) {
            log.error("Error in signal processing simulation", e);
        }
    }

    /**
     * Periodic cleanup of old executed/cancelled signals (every 30 minutes)
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void periodicCleanup() {
        try {
            var allSignals = signalActionsService.getAllSignalActions();

            // Log statistics
            var countsByStatus = signalActionsService.getSignalActionCountsByStatus();
            log.info(
                "Signal Statistics - Total: {}, Pending: {}, Executed: {}, Cancelled: {}",
                allSignals.size(),
                countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.PENDING, 0L),
                countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.EXECUTED, 0L),
                countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.CANCELLED, 0L)
            );
            // In a real application, you would clean up old signals here
            // For now, just log the activity

        } catch (Exception e) {
            log.error("Error in periodic cleanup", e);
        }
    }

    /**
     * Market hours check - more active simulation during "market hours" (every 2 minutes)
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    public void marketHoursSimulation() {
        if (!simulationEnabled) {
            return;
        }

        try {
            // Simulate higher activity during certain hours (simulate market hours)
            // This is just for demo - in real app you'd check actual market hours
            int hour = java.time.LocalTime.now().getHour();
            boolean isMarketHours = hour >= 9 && hour <= 16; // 9 AM to 4 PM

            if (isMarketHours && random.nextDouble() < 0.6) { // 60% chance during "market hours"
                // Generate 1-3 signals during active periods
                int signalCount = 1 + random.nextInt(3);

                for (int i = 0; i < signalCount; i++) {
                    String symbol = symbols.get(random.nextInt(symbols.size()));
                    String indicator = indicators.get(random.nextInt(indicators.size()));
                    String interval = intervals.get(random.nextInt(Math.min(4, intervals.size()))); // Prefer shorter intervals
                    String direction = directions.get(random.nextInt(directions.size()));

                    BigDecimal price = generateRealisticPrice(symbol);
                    String alertMessage = generateAlertMessage(indicator, direction) + " (Market Hours)";

                    notificationProcessorService.simulateRealTradingViewWebhook(
                        symbol,
                        price,
                        indicator,
                        direction,
                        interval,
                        alertMessage
                    );

                    // Small delay between signals
                    Thread.sleep(500);
                }

                log.info("Generated {} market hours signals", signalCount);
            }
        } catch (Exception e) {
            log.error("Error in market hours simulation", e);
        }
    }

    // Helper methods for realistic data generation
    private BigDecimal generateRealisticPrice(String symbol) {
        double basePrice;
        double volatility;

        switch (symbol) {
            case "AAPL":
                basePrice = 150.0;
                volatility = 5.0;
                break;
            case "GOOGL":
                basePrice = 2750.0;
                volatility = 50.0;
                break;
            case "MSFT":
                basePrice = 380.0;
                volatility = 10.0;
                break;
            case "AMZN":
                basePrice = 3200.0;
                volatility = 60.0;
                break;
            case "TSLA":
                basePrice = 220.0;
                volatility = 15.0;
                break;
            case "META":
                basePrice = 320.0;
                volatility = 15.0;
                break;
            case "NFLX":
                basePrice = 450.0;
                volatility = 20.0;
                break;
            case "NVDA":
                basePrice = 450.0;
                volatility = 25.0;
                break;
            case "AMD":
                basePrice = 120.0;
                volatility = 8.0;
                break;
            case "INTC":
                basePrice = 45.0;
                volatility = 3.0;
                break;
            default:
                basePrice = 100.0;
                volatility = 5.0;
        }

        // Add realistic price movement
        double priceVariation = (random.nextGaussian() * volatility * 0.1);
        double finalPrice = basePrice + priceVariation;

        return BigDecimal.valueOf(finalPrice).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateAlertMessage(String indicator, String direction) {
        String actionWord = direction.equals("BULL") ? "bullish" : direction.equals("BEAR") ? "bearish" : "neutral";

        switch (indicator) {
            case "RSI":
                return String.format(
                    "RSI indicates %s conditions - %s signal detected",
                    direction.equals("BULL") ? "oversold" : direction.equals("BEAR") ? "overbought" : "neutral",
                    actionWord
                );
            case "MACD":
                return String.format("MACD %s crossover detected with strong momentum", actionWord);
            case "BOLLINGER_BANDS":
                return String.format("Bollinger Bands show %s squeeze with %s breakout potential", actionWord, actionWord);
            case "EMA_CROSSOVER":
                return String.format("EMA crossover confirms %s trend formation", actionWord);
            case "SUPPORT_RESISTANCE":
                return String.format(
                    "Key %s level %s - %s opportunity identified",
                    direction.equals("BULL") ? "support" : "resistance",
                    direction.equals("BULL") ? "holding" : "broken",
                    actionWord
                );
            case "VOLUME_SPIKE":
                return String.format("Unusual volume spike detected with %s pressure", actionWord);
            case "STOCHASTIC":
                return String.format("Stochastic shows %s divergence - %s momentum building", actionWord, actionWord);
            case "MOMENTUM_INDICATOR":
                return String.format("Momentum indicator confirms %s acceleration", actionWord);
            default:
                return String.format("%s indicator shows %s signal", indicator, actionWord);
        }
    }
}
