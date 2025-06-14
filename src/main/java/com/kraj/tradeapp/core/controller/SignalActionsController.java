package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.dto.SignalActionDTO;
import com.kraj.tradeapp.core.service.SignalActionsService;
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

    @Autowired
    public SignalActionsController(SignalActionsService signalActionsService) {
        this.signalActionsService = signalActionsService;
    }

    /**
     * GET /signal-actions : Get all signal actions with optional filtering
     *
     * @param symbol filter by symbol (optional)
     * @param interval filter by interval (optional)
     * @param indicatorName filter by indicator name (optional)
     * @param fromDateTime filter from date time (optional)
     * @param toDateTime filter to date time (optional)
     * @return the ResponseEntity with status 200 (OK) and the list of signal actions in body
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
     *
     * @param id the id of the signal action to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the signal action, or with status 404 (Not Found)
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
     *
     * @param id the id of the signal action to execute
     * @return the ResponseEntity with status 200 (OK) if successful, or with status 404 (Not Found) or 400 (Bad Request)
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
     *
     * @param id the id of the signal action to cancel
     * @return the ResponseEntity with status 200 (OK) if successful, or with status 404 (Not Found) or 400 (Bad Request)
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
     *
     * @param signalActionDTO the signal action to create
     * @return the ResponseEntity with status 201 (Created) and with body the new signal action
     */
    @PostMapping("/signal-actions")
    public ResponseEntity<SignalActionDTO> createSignalAction(@RequestBody SignalActionDTO signalActionDTO) {
        log.debug("REST request to save SignalAction : {}", signalActionDTO);

        SignalActionDTO result = signalActionsService.createSignalAction(signalActionDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /signal-actions/filter-options : Get available filter options
     *
     * @return the ResponseEntity with status 200 (OK) and filter options in body
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
}
