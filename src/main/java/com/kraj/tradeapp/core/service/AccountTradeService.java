package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.AccountPNL;
import com.kraj.tradeapp.core.model.Trade;
import com.kraj.tradeapp.core.repository.AccountPNLRepository;
import com.kraj.tradeapp.core.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class AccountTradeService {

    private final TradeRepository accountTradeRepository;

    @Autowired
    public AccountTradeService(TradeRepository tradeRepository) {
        this.accountTradeRepository = tradeRepository;
    }

    public List<Trade> getOpenTradesForSymbol(String symbol) {
        return accountTradeRepository.findBySymbolAndStatus(symbol, "IN_PROGRESS");
    }

    public List<Trade> getOpenTradesForAccount(String accountId) {
        return accountTradeRepository.findByAccountIdAndStatus(accountId, "IN_PROGRESS");
    }
}
