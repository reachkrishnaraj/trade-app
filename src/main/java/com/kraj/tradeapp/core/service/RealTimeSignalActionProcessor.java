package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RealTimeSignalActionProcessor implements SignalActionProcessor {

    private final Logger log = LoggerFactory.getLogger(RealTimeSignalActionProcessor.class);

    @Value("${trading.realtime.enabled:true}")
    private boolean realTimeEnabled;

    // NO DEPENDENCIES - Pure processor

    @Override
    public SignalActionDTO createSignalActionDTO(
        String symbol,
        BigDecimal price,
        String indicator,
        String indicatorDisplayName,
        String interval,
        String alertMessage,
        String direction,
        ZonedDateTime eventTime,
        BigDecimal score,
        boolean isStrategy,
        boolean isAlertable
    ) {
        log.debug("Creating REAL-TIME signal action DTO for symbol: {}", symbol);

        SignalActionDTO signalAction = new SignalActionDTO();

        // NO ID GENERATION - Service will handle this
        signalAction.setSymbol(symbol);
        signalAction.setPrice(price);
        signalAction.setSignalName(buildRealTimeSignalName(indicator, alertMessage, score));
        signalAction.setIndicatorName(indicatorDisplayName != null ? indicatorDisplayName : indicator);
        signalAction.setInterval(interval);
        signalAction.setMessage(alertMessage);
        signalAction.setDirection(mapDirectionToSignalDirection(direction));
        signalAction.setDateTime(eventTime != null ? eventTime.toLocalDateTime() : LocalDateTime.now());
        signalAction.setStatus(determineRealTimeStatus(isStrategy, isAlertable, score));

        log.info(
            "Created REAL-TIME SignalAction DTO: {} for {} at price {}",
            signalAction.getSignalName(),
            signalAction.getSymbol(),
            signalAction.getPrice()
        );

        return signalAction; // PURE - Just return DTO, no storage
    }

    @Override
    public String getProcessorType() {
        return "REAL_TIME";
    }

    @Override
    public boolean isEnabled() {
        return realTimeEnabled;
    }

    // Helper methods for real-time processing
    private String buildRealTimeSignalName(String indicator, String alertMessage, BigDecimal score) {
        String signalName = indicator;

        // Add signal strength based on score
        if (score != null) {
            if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
                signalName += "_STRONG";
            } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
                signalName += "_MODERATE";
            } else if (score.compareTo(BigDecimal.valueOf(40)) >= 0) {
                signalName += "_WEAK";
            }
        }

        // Add specific signal type from alert message
        if (alertMessage != null) {
            String upperMessage = alertMessage.toUpperCase();
            if (upperMessage.contains("OVERSOLD")) {
                signalName += "_OVERSOLD";
            } else if (upperMessage.contains("OVERBOUGHT")) {
                signalName += "_OVERBOUGHT";
            } else if (upperMessage.contains("BREAKOUT")) {
                signalName += "_BREAKOUT";
            } else if (upperMessage.contains("CROSSOVER")) {
                signalName += "_CROSSOVER";
            } else if (upperMessage.contains("BOUNCE")) {
                signalName += "_BOUNCE";
            }
        }

        return signalName;
    }

    private SignalActionDTO.SignalDirection mapDirectionToSignalDirection(String direction) {
        if (direction == null) return SignalActionDTO.SignalDirection.HOLD;

        switch (direction.toUpperCase()) {
            case "BULL":
            case "BULLISH":
            case "BUY":
                return SignalActionDTO.SignalDirection.BUY;
            case "BEAR":
            case "BEARISH":
            case "SELL":
                return SignalActionDTO.SignalDirection.SELL;
            default:
                return SignalActionDTO.SignalDirection.HOLD;
        }
    }

    private SignalActionDTO.SignalStatus determineRealTimeStatus(boolean isStrategy, boolean isAlertable, BigDecimal score) {
        // Auto-execute very high confidence signals
        if (score != null && score.compareTo(BigDecimal.valueOf(95)) >= 0 && isAlertable) {
            return SignalActionDTO.SignalStatus.EXECUTED;
        }

        // Auto-execute high confidence non-strategy signals
        if (!isStrategy && score != null && score.compareTo(BigDecimal.valueOf(90)) >= 0 && isAlertable) {
            return SignalActionDTO.SignalStatus.EXECUTED;
        }

        // All other signals start as pending for manual review
        return SignalActionDTO.SignalStatus.PENDING;
    }
}
