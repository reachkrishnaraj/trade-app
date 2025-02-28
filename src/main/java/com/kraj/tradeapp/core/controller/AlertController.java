package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
@Validated
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/tradingView")
    public ResponseEntity<?> customDirectTradingViewAlert(@RequestBody String message) {
        alertService.handleDirectTradingViewAlert(message);
        return ResponseEntity.ok().build();
    }
}
