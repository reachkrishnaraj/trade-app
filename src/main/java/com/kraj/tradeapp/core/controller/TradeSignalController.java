package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.persistance.TradeSignal;
import com.kraj.tradeapp.core.service.ComputedTradeSignalService;
import com.kraj.tradeapp.core.service.OpenAIService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/signals")
@Validated
@RequiredArgsConstructor
public class TradeSignalController {

    private final ComputedTradeSignalService signalService;

    private final OpenAIService openAIService;

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<TradeSignal>> getSignalsBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(signalService.getSignalsForSymbol(symbol));
    }

    @GetMapping("/high-confidence")
    public ResponseEntity<List<TradeSignal>> getHighConfidenceSignals(
        @RequestParam @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal minConfidence
    ) {
        return ResponseEntity.ok(signalService.getHighConfidenceSignals(minConfidence));
    }
}
