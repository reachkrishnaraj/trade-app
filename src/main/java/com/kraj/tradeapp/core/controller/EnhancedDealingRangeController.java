package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.EventInterval;
import com.kraj.tradeapp.core.model.Quadrant;
import com.kraj.tradeapp.core.model.dto.DealingRangeDto;
import com.kraj.tradeapp.core.model.dto.DealingRangeHistoryDto;
import com.kraj.tradeapp.core.service.EnhancedDealingRangeService;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dealing-range/v2")
@RequiredArgsConstructor
@Slf4j
public class EnhancedDealingRangeController {

    private final EnhancedDealingRangeService dealingRangeService;

    // ========================================================================
    // WEBHOOK ENDPOINTS
    // ========================================================================

    /**
     * Main webhook endpoint for Pine Script JSON alerts
     * POST /api/dealing-range/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String jsonPayload) {
        log.info("Received dealing range webhook: {}", jsonPayload);
        dealingRangeService.processDealingRangeWebhook(jsonPayload);
        return ResponseEntity.ok("Dealing range webhook processed successfully");
    }

    // ========================================================================
    // CURRENT QUADRANT ENDPOINTS
    // ========================================================================

    /**
     * Get current quadrant for a specific symbol and timeframe
     * GET /api/dealing-range/current/{symbol}?interval=M15
     */
    @GetMapping("/current/{symbol}")
    public ResponseEntity<DealingRangeDto> getCurrentQuadrant(
        @PathVariable String symbol,
        @RequestParam(required = false) EventInterval interval
    ) {
        Optional<DealingRangeDto> result = interval != null
            ? dealingRangeService.getCurrentQuadrant(symbol, interval)
            : dealingRangeService.getCurrentQuadrant(symbol);

        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get current quadrants for all symbols in a specific timeframe
     * GET /api/dealing-range/current?interval=M15
     */
    @GetMapping("/current")
    public ResponseEntity<List<DealingRangeDto>> getCurrentQuadrants(@RequestParam EventInterval interval) {
        List<DealingRangeDto> quadrants = dealingRangeService.getCurrentQuadrants(interval);
        return ResponseEntity.ok(quadrants);
    }

    /**
     * Get symbols currently in specific quadrant
     * GET /api/dealing-range/current/quadrant/{quadrant}?interval=M15
     */
    @GetMapping("/current/quadrant/{quadrant}")
    public ResponseEntity<List<DealingRangeDto>> getSymbolsInQuadrant(
        @PathVariable Quadrant quadrant,
        @RequestParam EventInterval interval
    ) {
        List<DealingRangeDto> symbols = dealingRangeService.getSymbolsInQuadrant(quadrant, interval);
        return ResponseEntity.ok(symbols);
    }

    /**
     * Get symbols in extreme positions (breaches, Q1, Q4)
     * GET /api/dealing-range/current/extremes?interval=M15
     */
    @GetMapping("/current/extremes")
    public ResponseEntity<List<DealingRangeDto>> getCurrentExtremesPositions(@RequestParam EventInterval interval) {
        List<DealingRangeDto> extremes = dealingRangeService
            .getCurrentQuadrants(interval)
            .stream()
            .filter(DealingRangeDto::isExtremePosition)
            .toList();
        return ResponseEntity.ok(extremes);
    }

    // ========================================================================
    // HISTORY ENDPOINTS
    // ========================================================================

    /**
     * Get quadrant change history for a symbol
     * GET /api/dealing-range/history/{symbol}?interval=M15&limit=50
     */
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<DealingRangeHistoryDto>> getQuadrantHistory(
        @PathVariable String symbol,
        @RequestParam EventInterval interval,
        @RequestParam(defaultValue = "50") int limit
    ) {
        List<DealingRangeHistoryDto> history = dealingRangeService.getQuadrantHistory(symbol, interval, limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get recent quadrant changes across all symbols
     * GET /api/dealing-range/history/recent?interval=M15&limit=100
     */
    @GetMapping("/history/recent")
    public ResponseEntity<List<DealingRangeHistoryDto>> getRecentQuadrantChanges(
        @RequestParam EventInterval interval,
        @RequestParam(defaultValue = "100") int limit
    ) {
        List<DealingRangeHistoryDto> changes = dealingRangeService.getRecentQuadrantChanges(interval, limit);
        return ResponseEntity.ok(changes);
    }

    /**
     * Get quadrant changes in specific time range
     * GET /api/dealing-range/history/{symbol}/range?interval=M15&start=2024-01-01T00:00:00Z&end=2024-01-02T00:00:00Z
     */
    @GetMapping("/history/{symbol}/range")
    public ResponseEntity<List<DealingRangeHistoryDto>> getQuadrantChangesBetween(
        @PathVariable String symbol,
        @RequestParam EventInterval interval,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end
    ) {
        List<DealingRangeHistoryDto> history = dealingRangeService.getQuadrantChangesBetween(symbol, interval, start, end);
        return ResponseEntity.ok(history);
    }

    /**
     * Get quadrant statistics for a symbol over time
     * GET /api/dealing-range/stats/{symbol}?interval=M15&days=7
     */
    @GetMapping("/stats/{symbol}")
    public ResponseEntity<Map<String, Object>> getQuadrantStatistics(
        @PathVariable String symbol,
        @RequestParam EventInterval interval,
        @RequestParam(defaultValue = "7") int days
    ) {
        Map<String, Object> stats = dealingRangeService.getQuadrantStatistics(symbol, interval, days);
        return ResponseEntity.ok(stats);
    }

    // ========================================================================
    // MONITORING ENDPOINTS
    // ========================================================================

    /**
     * Find symbols stuck in extreme positions for extended periods
     * GET /api/dealing-range/monitoring/stuck-extremes?interval=M15&minutes=60
     */
    @GetMapping("/monitoring/stuck-extremes")
    public ResponseEntity<List<DealingRangeDto>> findSymbolsStuckInExtremes(
        @RequestParam EventInterval interval,
        @RequestParam(defaultValue = "60") int minutes
    ) {
        List<DealingRangeDto> stuckSymbols = dealingRangeService.findSymbolsInExtremesForDuration(interval, minutes);
        return ResponseEntity.ok(stuckSymbols);
    }

    /**
     * Get alerts for symbols that just changed quadrants
     * GET /api/dealing-range/monitoring/recent-changes?interval=M15&minutes=5
     */
    @GetMapping("/monitoring/recent-changes")
    public ResponseEntity<List<DealingRangeHistoryDto>> getRecentChangesInTimeWindow(
        @RequestParam EventInterval interval,
        @RequestParam(defaultValue = "5") int minutes
    ) {
        ZonedDateTime since = ZonedDateTime.now().minusMinutes(minutes);
        List<DealingRangeHistoryDto> recentChanges = dealingRangeService
            .getRecentQuadrantChanges(interval, 100)
            .stream()
            .filter(h -> h.getTimestamp().isAfter(since))
            .filter(h -> "QUADRANT_CHANGE".equals(h.getEventType()))
            .toList();

        return ResponseEntity.ok(recentChanges);
    }

    // ========================================================================
    // BULK QUERY ENDPOINTS
    // ========================================================================

    /**
     * Get current quadrants for multiple symbols
     * POST /api/dealing-range/current/bulk
     * Body: {"symbols": ["NQ1!", "ES1!", "EURUSD"], "interval": "M15"}
     */
    @PostMapping("/current/bulk")
    public ResponseEntity<List<DealingRangeDto>> getCurrentQuadrantsBulk(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> symbols = (List<String>) request.get("symbols");
        String intervalStr = (String) request.get("interval");
        EventInterval interval = EventInterval.getFromValue(intervalStr);

        List<DealingRangeDto> result = symbols
            .stream()
            .map(symbol -> dealingRangeService.getCurrentQuadrant(symbol, interval))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        return ResponseEntity.ok(result);
    }

    // ========================================================================
    // SUMMARY ENDPOINTS
    // ========================================================================

    /**
     * Get summary dashboard data
     * GET /api/dealing-range/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<List<DealingRangeDto>> getSummaryDashboard() {
        List<DealingRangeDto> allQuadrants = dealingRangeService.getCurrentQuadrants();

        Map<Quadrant, Long> quadrantCounts = allQuadrants
            .stream()
            .collect(java.util.stream.Collectors.groupingBy(DealingRangeDto::getCurrentQuadrant, java.util.stream.Collectors.counting()));

        long totalSymbols = allQuadrants.size();
        long extremeCount = allQuadrants.stream().mapToLong(dto -> dto.isExtremePosition() ? 1 : 0).sum();

        Map<String, Object> summary = Map.of(
            "totalSymbols",
            totalSymbols,
            "extremePositions",
            extremeCount,
            "quadrantCounts",
            quadrantCounts,
            "timestamp",
            ZonedDateTime.now()
        );

        return ResponseEntity.ok(allQuadrants);
    }

    // ========================================================================
    // TESTING ENDPOINTS
    // ========================================================================

    /**
     * Create test data
     * POST /api/dealing-range/test-data
     */
    @PostMapping("/test-data")
    public ResponseEntity<String> createTestData() {
        dealingRangeService.createJsonTestData();
        return ResponseEntity.ok("Test data created successfully");
    }

    /**
     * Simulate alert for testing
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
        return ResponseEntity.ok("Alert simulated successfully");
    }

    // ========================================================================
    // HEALTH CHECK
    // ========================================================================

    /**
     * Health check endpoint
     * GET /api/dealing-range/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of("status", "UP", "timestamp", ZonedDateTime.now(), "service", "DealingRangeService");
        return ResponseEntity.ok(health);
    }
}
