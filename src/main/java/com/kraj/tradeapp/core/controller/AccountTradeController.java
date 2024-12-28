package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.model.persistance.Trade;
import com.kraj.tradeapp.core.service.AccountTradeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
