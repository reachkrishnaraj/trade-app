package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.CommonUtil;
import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import com.kraj.tradeapp.core.service.SignalActionsService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimulationSignalActionProcessor implements SignalActionProcessor {

    private final Logger log = LoggerFactory.getLogger(SimulationSignalActionProcessor.class);
    private final Random random = new Random();

    @Value("${trading.simulation.enabled:false}")
    private boolean simulationEnabled;

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
        boolean isAlertable,
        boolean isAnnounce
    ) {
        log.debug("Creating SIMULATED signal action DTO for symbol: {}", symbol);

        SignalActionDTO signalAction = new SignalActionDTO();

        // NO ID GENERATION - Service will handle this
        signalAction.setSymbol(symbol);
        signalAction.setPrice(price);
        signalAction.setSignalName(buildSimulatedSignalName(indicator, direction));
        signalAction.setIndicatorName(indicatorDisplayName != null ? indicatorDisplayName : indicator);
        signalAction.setInterval(interval);
        signalAction.setMessage("[SIMULATION] " + alertMessage);
        signalAction.setDirection(mapDirectionToSignalDirection(direction));
        signalAction.setDateTime(CommonUtil.getNYLocalDateTimeNow());
        signalAction.setStatus(determineSimulatedStatus(score));
        signalAction.setAnnounce(isAnnounce);

        log.info(
            "Created SIMULATED SignalAction DTO: {} for {} at price {}, isAnnounce: {}",
            signalAction.getSignalName(),
            signalAction.getSymbol(),
            signalAction.getPrice()
        );

        return signalAction; // PURE - Just return DTO, no storage
    }

    @Override
    public String getProcessorType() {
        return "SIMULATION";
    }

    @Override
    public boolean isEnabled() {
        return simulationEnabled;
    }

    // Helper methods for simulation
    private String buildSimulatedSignalName(String indicator, String direction) {
        String baseName = "SIM_" + indicator;

        if ("BULL".equals(direction)) {
            baseName += "_BULLISH";
        } else if ("BEAR".equals(direction)) {
            baseName += "_BEARISH";
        } else {
            baseName += "_NEUTRAL";
        }

        // Add random element for simulation
        String[] variations = { "_WEAK", "_MODERATE", "_STRONG" };
        baseName += variations[random.nextInt(variations.length)];

        return baseName;
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

    private SignalActionDTO.SignalStatus determineSimulatedStatus(BigDecimal score) {
        // In simulation, randomly assign status for testing
        if (score != null && score.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return SignalActionDTO.SignalStatus.EXECUTED;
        }

        // 80% pending, 20% executed for simulation variety
        return random.nextDouble() < 0.8 ? SignalActionDTO.SignalStatus.PENDING : SignalActionDTO.SignalStatus.EXECUTED;
    }
}
