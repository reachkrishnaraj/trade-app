package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import com.kraj.tradeapp.core.service.NotificationProcessorService;
import com.kraj.tradeapp.core.service.SignalActionsService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SignalActionsController {

    private final Logger log = LoggerFactory.getLogger(SignalActionsController.class);

    private final SignalActionsService signalActionsService;
    private final NotificationProcessorService notificationProcessorService; // ONLY NEW DEPENDENCY

    @Autowired
    public SignalActionsController(
        SignalActionsService signalActionsService,
        NotificationProcessorService notificationProcessorService // ONLY NEW PARAMETER
    ) {
        this.signalActionsService = signalActionsService;
        this.notificationProcessorService = notificationProcessorService; // ONLY NEW ASSIGNMENT
    }

    /**
     * GET /signal-actions : Get all signal actions with optional filtering
     * UNCHANGED - Your frontend will work exactly the same
     */
    @GetMapping("/signal-actions")
    public ResponseEntity<List<SignalActionDTO>> getAllSignalActions(
        @RequestParam(required = false) String symbol,
        @RequestParam(required = false) String interval,
        @RequestParam(required = false) String indicatorName,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime
    ) {
        log.debug(
            "REST request to get signal actions with filters - symbol: {}, interval: {}, indicator: {}, from: {}, to: {}",
            symbol,
            interval,
            indicatorName,
            fromDateTime,
            toDateTime
        );

        List<SignalActionDTO> signalActions;

        // If any filters are provided, use filtered method
        if (symbol != null || interval != null || indicatorName != null || fromDateTime != null || toDateTime != null) {
            signalActions = signalActionsService.getFilteredSignalActions(symbol, interval, indicatorName, fromDateTime, toDateTime);
        } else {
            signalActions = signalActionsService.getAllSignalActions();
        }

        return ResponseEntity.ok(signalActions);
    }

    /**
     * GET /signal-actions/{id} : Get signal action by id
     * UNCHANGED - Your frontend will work exactly the same
     */
    @GetMapping("/signal-actions/{id}")
    public ResponseEntity<SignalActionDTO> getSignalAction(@PathVariable Long id) {
        log.debug("REST request to get SignalAction : {}", id);

        SignalActionDTO signalAction = signalActionsService.getSignalActionById(id);
        if (signalAction != null) {
            return ResponseEntity.ok(signalAction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /signal-actions/{id}/execute : Execute a signal action
     * UNCHANGED - Your frontend will work exactly the same
     */
    @PostMapping("/signal-actions/{id}/execute")
    public ResponseEntity<Map<String, String>> executeSignalAction(@PathVariable Long id) {
        log.debug("REST request to execute SignalAction : {}", id);

        try {
            signalActionsService.executeSignalAction(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Signal action executed successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.error("Error executing signal action: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /signal-actions/{id}/cancel : Cancel a signal action
     * UNCHANGED - Your frontend will work exactly the same
     */
    @PostMapping("/signal-actions/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelSignalAction(@PathVariable Long id) {
        log.debug("REST request to cancel SignalAction : {}", id);

        try {
            signalActionsService.cancelSignalAction(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Signal action cancelled successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.error("Error cancelling signal action: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /signal-actions : Create a new signal action
     * UNCHANGED - Your frontend will work exactly the same
     */
    @PostMapping("/signal-actions")
    public ResponseEntity<SignalActionDTO> createSignalAction(@RequestBody SignalActionDTO signalActionDTO) {
        log.debug("REST request to save SignalAction : {}", signalActionDTO);

        SignalActionDTO result = signalActionsService.createSignalAction(signalActionDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /signal-actions/filter-options : Get available filter options
     * UNCHANGED - Your frontend will work exactly the same
     */
    @GetMapping("/signal-actions/filter-options")
    public ResponseEntity<Map<String, List<String>>> getFilterOptions() {
        log.debug("REST request to get filter options");

        Map<String, List<String>> filterOptions = new HashMap<>();
        filterOptions.put("symbols", signalActionsService.getUniqueSymbols());
        filterOptions.put("intervals", signalActionsService.getUniqueIntervals());
        filterOptions.put("indicators", signalActionsService.getUniqueIndicatorNames());

        return ResponseEntity.ok(filterOptions);
    }

    // ============================================================================
    // OPTIONAL NEW ENDPOINTS - Your frontend doesn't need to use these
    // These are just for testing real events if needed
    // ============================================================================

    /**
     * POST /signal-actions/test-real-event : Test real event (OPTIONAL - for testing only)
     * Your frontend doesn't need to use this - it's just for testing
     */
    @PostMapping("/signal-actions/test-real-event")
    public ResponseEntity<Map<String, String>> testRealEvent(@RequestBody Map<String, Object> eventData) {
        log.debug("REST request to test real event: {}", eventData);

        try {
            String symbol = (String) eventData.getOrDefault("symbol", "AAPL");
            BigDecimal price = new BigDecimal(eventData.getOrDefault("price", "150.0").toString());
            String indicator = (String) eventData.getOrDefault("indicator", "RSI");
            String direction = (String) eventData.getOrDefault("direction", "BULL");
            String interval = (String) eventData.getOrDefault("interval", "1h");
            String alertMessage = (String) eventData.getOrDefault("alertMessage", "Test alert");

            notificationProcessorService.simulateRealTradingViewWebhook(symbol, price, indicator, direction, interval, alertMessage);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Real event test completed successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing real event: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error testing real event: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /signal-actions/count : Get simple count (OPTIONAL - for dashboard widgets)
     * Your frontend can use this if you want to show counts
     */
    @GetMapping("/signal-actions/count")
    public ResponseEntity<Map<String, Object>> getSignalActionsCount() {
        log.debug("REST request to get signal actions count");

        try {
            Map<SignalActionDTO.SignalStatus, Long> countsByStatus = signalActionsService.getSignalActionCountsByStatus();

            Map<String, Object> response = new HashMap<>();
            response.put("total", signalActionsService.getAllSignalActions().size());
            response.put("pending", countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.PENDING, 0L));
            response.put("executed", countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.EXECUTED, 0L));
            response.put("cancelled", countsByStatus.getOrDefault(SignalActionDTO.SignalStatus.CANCELLED, 0L));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting signal actions count: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
