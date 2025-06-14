package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to simulate new signal actions for demonstration purposes
 */
@Component
public class SignalActionsScheduler {

    private final Logger log = LoggerFactory.getLogger(SignalActionsScheduler.class);

    private final SignalActionsService signalActionsService;
    private final Random random = new Random();

    // Sample data for simulation
    private final List<String> symbols = Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NFLX", "NVDA", "AMD", "INTC");

    private final List<String> signalNames = Arrays.asList(
        "RSI_OVERSOLD",
        "RSI_OVERBOUGHT",
        "MACD_BULLISH",
        "MACD_BEARISH",
        "BREAKOUT_PATTERN",
        "SUPPORT_BOUNCE",
        "RESISTANCE_BREAK",
        "VOLUME_SPIKE",
        "GOLDEN_CROSS",
        "DEATH_CROSS",
        "BOLLINGER_SQUEEZE",
        "MOMENTUM_SHIFT"
    );

    @Autowired
    public SignalActionsScheduler(SignalActionsService signalActionsService) {
        this.signalActionsService = signalActionsService;
    }

    /**
     * Simulate new signal actions every 30 seconds (for demo purposes)
     * In production, this would be replaced by real signal generation logic
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void simulateNewSignalActions() {
        try {
            // Randomly decide if we should generate a new signal (30% chance)
            if (random.nextDouble() < 0.3) {
                String symbol = symbols.get(random.nextInt(symbols.size()));
                String signalName = signalNames.get(random.nextInt(signalNames.size()));

                // Generate a random price between 50 and 500
                BigDecimal price = BigDecimal.valueOf(50 + (random.nextDouble() * 450)).setScale(2, RoundingMode.HALF_UP);

                signalActionsService.simulateNewSignal(symbol, price, signalName);

                log.info("Generated new signal: {} for {} at ${}", signalName, symbol, price);
            }
        } catch (Exception e) {
            log.error("Error generating simulated signal action", e);
        }
    }

    /**
     * Simulate random execution/cancellation of pending signals every 45 seconds
     */
    @Scheduled(fixedRate = 45000) // 45 seconds
    public void simulateSignalProcessing() {
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
}
