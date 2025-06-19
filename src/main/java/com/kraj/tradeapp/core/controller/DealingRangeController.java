package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.EventInterval;
import com.kraj.tradeapp.core.model.Quadrant;
import com.kraj.tradeapp.core.model.dto.DealingRangeDto;
import com.kraj.tradeapp.core.service.DealingRangeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dealing-range")
@RequiredArgsConstructor
@Slf4j
public class DealingRangeController {

    private final DealingRangeService dealingRangeService;

    // ========================================================================
    // WEBHOOK ENDPOINTS
    // ========================================================================

    /**
     * Main webhook endpoint for Pine Script JSON alerts
     * POST /api/dealing-range/webhook
     *
     * Example JSON body:
     * {"ticker":"NQ1!","update":"1M Update","chart_tf":"60","quadrant":"Q4_0_25","price":21846.5,"range":"21790-22097.75"}
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String jsonPayload) {
        log.info("Received dealing range webhook: {}", jsonPayload);
        dealingRangeService.processDealingRangeWebhook(jsonPayload);
        return ResponseEntity.ok("Dealing range JSON webhook processed successfully");
    }

    // ========================================================================
    // BASIC QUERY ENDPOINTS
    // ========================================================================

    /**
     * Get current dealing range for a specific symbol
     * GET /api/dealing-range/{symbol}
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<DealingRangeDto> getCurrentDealingRange(@PathVariable String symbol) {
        Optional<DealingRangeDto> dealingRange = dealingRangeService.getCurrentDealingRange(symbol);
        return dealingRange.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get summary of all symbols
     * GET /api/dealing-range/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<List<DealingRangeDto>> getAllDealingRanges() {
        List<DealingRangeDto> summary = dealingRangeService.getDealingRangeSummary(null);
        return ResponseEntity.ok(summary);
    }

    // ========================================================================
    // QUADRANT-BASED ENDPOINTS
    // ========================================================================

    /**
     * Get all symbols in a specific quadrant
     * GET /api/dealing-range/quadrant/{quadrant}
     */
    @GetMapping("/quadrant/{quadrant}")
    public ResponseEntity<List<DealingRangeDto>> getSymbolsInQuadrant(@PathVariable Quadrant quadrant) {
        List<DealingRangeDto> symbols = dealingRangeService.getSymbolsInQuadrant(quadrant);
        return ResponseEntity.ok(symbols);
    }

    /**
     * Get all symbols in extreme positions (Q1, Q4, ABOVE_RANGE, BELOW_RANGE)
     * GET /api/dealing-range/extreme
     */
    @GetMapping("/extreme")
    public ResponseEntity<List<DealingRangeDto>> getExtremePositions() {
        List<DealingRangeDto> extremeSymbols = dealingRangeService.getExtremePositions();
        return ResponseEntity.ok(extremeSymbols);
    }

    /**
     * Get all symbols within normal range (Q1, Q2, Q3, Q4)
     * GET /api/dealing-range/within-range
     */
    @GetMapping("/within-range")
    public ResponseEntity<List<DealingRangeDto>> getSymbolsWithinRange() {
        List<DealingRangeDto> withinRangeSymbols = dealingRangeService.getSymbolsWithinRange();
        return ResponseEntity.ok(withinRangeSymbols);
    }

    /**
     * Get count by quadrant
     * GET /api/dealing-range/counts
     */
    @GetMapping("/counts")
    public ResponseEntity<Map<Quadrant, Long>> getQuadrantCounts() {
        Map<Quadrant, Long> counts = dealingRangeService.getQuadrantCounts();
        return ResponseEntity.ok(counts);
    }

    // ========================================================================
    // INTERVAL-BASED ENDPOINTS
    // ========================================================================

    /**
     * Get current dealing range for symbol by specific interval
     * GET /api/dealing-range/{symbol}/interval/{interval}
     */
    @GetMapping("/{symbol}/interval/{interval}")
    public ResponseEntity<DealingRangeDto> getDealingRangeByInterval(@PathVariable String symbol, @PathVariable EventInterval interval) {
        Optional<DealingRangeDto> dealingRange = dealingRangeService.getDealingRangeByInterval(symbol, interval);
        return dealingRange.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all dealing ranges for a specific interval
     * GET /api/dealing-range/interval/{interval}
     */
    @GetMapping("/interval/{interval}")
    public ResponseEntity<List<DealingRangeDto>> getDealingRangesByInterval(@PathVariable EventInterval interval) {
        List<DealingRangeDto> dealingRanges = dealingRangeService.getDealingRangesByInterval(interval);
        return ResponseEntity.ok(dealingRanges);
    }

    /**
     * Get symbols in specific quadrant for a specific interval
     * GET /api/dealing-range/quadrant/{quadrant}/interval/{interval}
     */
    @GetMapping("/quadrant/{quadrant}/interval/{interval}")
    public ResponseEntity<List<DealingRangeDto>> getSymbolsInQuadrantByInterval(
        @PathVariable Quadrant quadrant,
        @PathVariable EventInterval interval
    ) {
        List<DealingRangeDto> symbols = dealingRangeService.getSymbolsInQuadrantByInterval(quadrant, interval);
        return ResponseEntity.ok(symbols);
    }

    /**
     * Get extreme positions for a specific interval
     * GET /api/dealing-range/extreme/interval/{interval}
     */
    @GetMapping("/extreme/interval/{interval}")
    public ResponseEntity<List<DealingRangeDto>> getExtremePositionsByInterval(@PathVariable EventInterval interval) {
        List<DealingRangeDto> extremeSymbols = dealingRangeService.getExtremePositionsByInterval(interval);
        return ResponseEntity.ok(extremeSymbols);
    }

    /**
     * Get symbols within range for a specific interval
     * GET /api/dealing-range/within-range/interval/{interval}
     */
    @GetMapping("/within-range/interval/{interval}")
    public ResponseEntity<List<DealingRangeDto>> getSymbolsWithinRangeByInterval(@PathVariable EventInterval interval) {
        List<DealingRangeDto> withinRangeSymbols = dealingRangeService.getSymbolsWithinRangeByInterval(interval);
        return ResponseEntity.ok(withinRangeSymbols);
    }

    /**
     * Get quadrant counts for specific interval
     * GET /api/dealing-range/counts/interval/{interval}
     */
    @GetMapping("/counts/interval/{interval}")
    public ResponseEntity<Map<Quadrant, Long>> getQuadrantCountsByInterval(@PathVariable EventInterval interval) {
        Map<Quadrant, Long> counts = dealingRangeService.getQuadrantCountsByInterval(interval);
        return ResponseEntity.ok(counts);
    }

    // ========================================================================
    // TESTING ENDPOINTS
    // ========================================================================

    /**
     * Create JSON test data
     * POST /api/dealing-range/test-data
     */
    @PostMapping("/test-data")
    public ResponseEntity<String> createTestData() {
        dealingRangeService.createJsonTestData();
        return ResponseEntity.ok("JSON test data created successfully");
    }

    /**
     * Simulate JSON alert for testing
     * POST /api/dealing-range/simulate
     */
    @PostMapping("/simulate")
    public ResponseEntity<String> simulateAlert(
        @RequestParam String symbol,
        @RequestParam BigDecimal currentPrice,
        @RequestParam Quadrant quadrant,
        @RequestParam BigDecimal rangeHigh,
        @RequestParam BigDecimal rangeLow,
        @RequestParam(defaultValue = "15") String chartTimeframe,
        @RequestParam(required = false) EventInterval interval
    ) {
        dealingRangeService.simulateDealingRangeJsonAlert(symbol, currentPrice, quadrant, rangeHigh, rangeLow, chartTimeframe, interval);
        return ResponseEntity.ok("JSON alert simulated successfully");
    }
}
