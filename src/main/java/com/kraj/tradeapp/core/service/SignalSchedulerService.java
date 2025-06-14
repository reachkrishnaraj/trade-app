package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import java.math.BigDecimal;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for automatically generating trading signals at scheduled intervals
 * This simulates real-world signal generation from trading algorithms
 */
@Service
public class SignalSchedulerService {

    private final Logger log = LoggerFactory.getLogger(SignalSchedulerService.class);

    private final SignalActionsService signalActionsService;
    private final Random random = new Random();

    // Market data for realistic simulation
    private final String[] SYMBOLS = {
        "AAPL",
        "GOOGL",
        "TSLA",
        "MSFT",
        "AMZN",
        "NVDA",
        "META",
        "BTC-USD",
        "ETH-USD",
        "SPY",
        "QQQ",
        "AMD",
        "NFLX",
        "CRM",
    };
    private final String[] INDICATORS = {
        "RSI",
        "MACD",
        "Bollinger Bands",
        "EMA",
        "SMA",
        "Support/Resistance",
        "Volume",
        "Stochastic",
        "Williams %R",
        "CCI",
    };
    private final String[] INTERVALS = { "15m", "30m", "1h", "2h", "4h", "6h", "1d" };

    // Signal patterns for different market conditions
    private final SignalPattern[] SIGNAL_PATTERNS = {
        new SignalPattern("RSI_OVERSOLD", "RSI", "Price in oversold territory, potential reversal", SignalActionDTO.SignalDirection.BUY),
        new SignalPattern(
            "RSI_OVERBOUGHT",
            "RSI",
            "Price in overbought territory, potential reversal",
            SignalActionDTO.SignalDirection.SELL
        ),
        new SignalPattern("MACD_BULLISH_CROSS", "MACD", "MACD line crossed above signal line", SignalActionDTO.SignalDirection.BUY),
        new SignalPattern("MACD_BEARISH_CROSS", "MACD", "MACD line crossed below signal line", SignalActionDTO.SignalDirection.SELL),
        new SignalPattern(
            "BOLLINGER_SQUEEZE",
            "Bollinger Bands",
            "Bollinger Bands showing volatility squeeze",
            SignalActionDTO.SignalDirection.HOLD
        ),
        new SignalPattern("VOLUME_BREAKOUT", "Volume", "High volume breakout detected", SignalActionDTO.SignalDirection.BUY),
        new SignalPattern(
            "SUPPORT_BOUNCE",
            "Support/Resistance",
            "Price bounced off key support level",
            SignalActionDTO.SignalDirection.BUY
        ),
        new SignalPattern(
            "RESISTANCE_REJECTION",
            "Support/Resistance",
            "Price rejected at resistance level",
            SignalActionDTO.SignalDirection.SELL
        ),
        new SignalPattern("EMA_GOLDEN_CROSS", "EMA", "50 EMA crossed above 200 EMA", SignalActionDTO.SignalDirection.BUY),
        new SignalPattern("EMA_DEATH_CROSS", "EMA", "50 EMA crossed below 200 EMA", SignalActionDTO.SignalDirection.SELL),
    };

    @Autowired
    public SignalSchedulerService(SignalActionsService signalActionsService) {
        this.signalActionsService = signalActionsService;
    }

    /**
     * Generate random signals every 30 seconds during market hours simulation
     * In production, this would be based on real market analysis
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void generateRandomSignal() {
        try {
            // Only generate signals during "market hours" simulation (70% chance)
            if (random.nextDouble() > 0.3) {
                SignalPattern pattern = SIGNAL_PATTERNS[random.nextInt(SIGNAL_PATTERNS.length)];
                String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
                String interval = INTERVALS[random.nextInt(INTERVALS.length)];

                BigDecimal price = generateRealisticPrice(symbol);

                signalActionsService.simulateNewSignal(
                    symbol,
                    price,
                    pattern.signalName,
                    pattern.indicator,
                    interval,
                    pattern.message,
                    pattern.direction
                );

                log.debug("Auto-generated signal: {} {} at {} ({})", pattern.direction, symbol, price, pattern.indicator);
            }
        } catch (Exception e) {
            log.error("Error auto-generating signal: {}", e.getMessage());
        }
    }

    /**
     * Generate burst of signals during high volatility periods (every 5 minutes)
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void generateVolatilityBurst() {
        try {
            // 20% chance of volatility burst
            if (random.nextDouble() < 0.2) {
                int signalCount = 2 + random.nextInt(4); // 2-5 signals

                log.info("Generating volatility burst with {} signals", signalCount);

                for (int i = 0; i < signalCount; i++) {
                    SignalPattern pattern = SIGNAL_PATTERNS[random.nextInt(SIGNAL_PATTERNS.length)];
                    String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
                    String interval = INTERVALS[random.nextInt(INTERVALS.length)];

                    BigDecimal price = generateRealisticPrice(symbol);

                    signalActionsService.simulateNewSignal(
                        symbol,
                        price,
                        pattern.signalName + "_VOLATILE",
                        pattern.indicator,
                        interval,
                        "High volatility: " + pattern.message,
                        pattern.direction
                    );

                    // Small delay between burst signals
                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            log.error("Error generating volatility burst: {}", e.getMessage());
        }
    }

    /**
     * Generate crypto-specific signals (every 2 minutes for crypto)
     */
    @Scheduled(fixedRate = 120000) // Every 2 minutes
    public void generateCryptoSignals() {
        try {
            // 40% chance for crypto signals
            if (random.nextDouble() < 0.4) {
                String[] cryptoSymbols = { "BTC-USD", "ETH-USD", "ADA-USD", "DOT-USD", "SOL-USD" };
                String symbol = cryptoSymbols[random.nextInt(cryptoSymbols.length)];

                SignalPattern pattern = SIGNAL_PATTERNS[random.nextInt(SIGNAL_PATTERNS.length)];
                String interval = INTERVALS[random.nextInt(3)]; // Prefer shorter intervals for crypto

                BigDecimal price = generateRealisticPrice(symbol);

                signalActionsService.simulateNewSignal(
                    symbol,
                    price,
                    pattern.signalName + "_CRYPTO",
                    pattern.indicator,
                    interval,
                    "Crypto market: " + pattern.message,
                    pattern.direction
                );

                log.debug("Auto-generated crypto signal: {} {} at {}", pattern.direction, symbol, price);
            }
        } catch (Exception e) {
            log.error("Error generating crypto signal: {}", e.getMessage());
        }
    }

    /**
     * Weekly cleanup of old executed/cancelled signals (every Sunday at 2 AM)
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyCleanup() {
        try {
            log.info("Performing weekly signal cleanup");
            // In a real application, you would clean up old signals from database
            // This is just a placeholder for demonstration
        } catch (Exception e) {
            log.error("Error during weekly cleanup: {}", e.getMessage());
        }
    }

    // Helper methods
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
            case "TSLA":
                basePrice = 220.0;
                volatility = 15.0;
                break;
            case "MSFT":
                basePrice = 380.0;
                volatility = 10.0;
                break;
            case "AMZN":
                basePrice = 3200.0;
                volatility = 60.0;
                break;
            case "NVDA":
                basePrice = 450.0;
                volatility = 20.0;
                break;
            case "META":
                basePrice = 320.0;
                volatility = 15.0;
                break;
            case "BTC-USD":
                basePrice = 42000.0;
                volatility = 1000.0;
                break;
            case "ETH-USD":
                basePrice = 2500.0;
                volatility = 100.0;
                break;
            case "ADA-USD":
                basePrice = 0.45;
                volatility = 0.02;
                break;
            case "DOT-USD":
                basePrice = 6.50;
                volatility = 0.30;
                break;
            case "SOL-USD":
                basePrice = 95.0;
                volatility = 5.0;
                break;
            default:
                basePrice = 100.0;
                volatility = 5.0;
        }

        // Add some random movement
        double priceVariation = (random.nextGaussian() * volatility * 0.1);
        double finalPrice = basePrice + priceVariation;

        return new BigDecimal(finalPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // Inner class for signal patterns
    private static class SignalPattern {

        final String signalName;
        final String indicator;
        final String message;
        final SignalActionDTO.SignalDirection direction;

        SignalPattern(String signalName, String indicator, String message, SignalActionDTO.SignalDirection direction) {
            this.signalName = signalName;
            this.indicator = indicator;
            this.message = message;
            this.direction = direction;
        }
    }
}
