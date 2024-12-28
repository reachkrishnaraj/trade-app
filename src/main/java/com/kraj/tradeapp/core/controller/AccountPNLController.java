package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.dto.AccountPNLRequest;
import com.kraj.tradeapp.core.model.persistance.AccountPNL;
import com.kraj.tradeapp.core.service.AccountPNLService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pnl")
@Validated
@RequiredArgsConstructor
public class AccountPNLController {

    private final AccountPNLService accountPNLService;

    @GetMapping("/{accountId}")
    public ResponseEntity<List<AccountPNL>> getPNL(@PathVariable String accountId, @Valid AccountPNLRequest request) {
        return ResponseEntity.ok(accountPNLService.getPNLForDateRange(accountId, request.getStartDate(), request.getEndDate()));
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<List<AccountPNL>> getDailyPNL(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(accountPNLService.getAllPNLForDate(date));
    }

    @PostMapping
    public ResponseEntity<AccountPNL> createPNL(@RequestBody @Valid AccountPNL accountPNL) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountPNLService.createPNL(accountPNL));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountPNL> updatePNL(@PathVariable String accountId, @RequestBody @Valid AccountPNL accountPNL) {
        return ResponseEntity.ok(accountPNLService.updatePNL(accountId, accountPNL));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePNL(@PathVariable String accountId) {
        accountPNLService.deletePNL(accountId);
        return ResponseEntity.noContent().build();
    }
}
