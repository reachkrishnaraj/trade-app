package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.Trade;
import com.kraj.tradeapp.core.service.AccountTradeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trades")
@Validated
@RequiredArgsConstructor
public class AccountTradeController {

    private final AccountTradeService tradeService;

    @GetMapping("/open/symbol/{symbol}")
    public ResponseEntity<List<Trade>> getOpenTradesBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(tradeService.getOpenTradesForSymbol(symbol));
    }

    @GetMapping("/open/account/{accountId}")
    public ResponseEntity<List<Trade>> getOpenTradesByAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(tradeService.getOpenTradesForAccount(accountId));
    }

    @PostMapping
    public ResponseEntity<Trade> createTrade(@RequestBody Trade trade) {
        return ResponseEntity.ok(tradeService.createTrade(trade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trade> getTradeById(@PathVariable Long id) {
        return ResponseEntity.ok(tradeService.getTradeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trade> updateTrade(@PathVariable Long id, @RequestBody Trade trade) {
        return ResponseEntity.ok(tradeService.updateTrade(id, trade));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrade(@PathVariable Long id) {
        tradeService.deleteTrade(id);
        return ResponseEntity.noContent().build();
    }
}
