package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.Trade;
import com.kraj.tradeapp.core.repository.TradeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class AccountTradeService {

    private final TradeRepository accountTradeRepository;

    public AccountTradeService(TradeRepository tradeRepository) {
        this.accountTradeRepository = tradeRepository;
    }

    public List<Trade> getOpenTradesForSymbol(String symbol) {
        return accountTradeRepository.findBySymbolAndStatus(symbol, "IN_PROGRESS");
    }

    public List<Trade> getOpenTradesForAccount(String accountId) {
        return accountTradeRepository.findByAccountIdAndStatus(accountId, "IN_PROGRESS");
    }

    public Trade createTrade(Trade trade) {
        return accountTradeRepository.save(trade);
    }

    public Trade getTradeById(Long id) {
        return accountTradeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Trade not found"));
    }

    public Trade updateTrade(Long id, Trade trade) {
        Trade existingTrade = getTradeById(id);
        existingTrade.setSymbol(trade.getSymbol());
        existingTrade.setStatus(trade.getStatus());
        existingTrade.setQuantity(trade.getQuantity());
        existingTrade.setPrice(trade.getPrice());
        existingTrade.setAccountId(trade.getAccountId());
        return accountTradeRepository.save(existingTrade);
    }

    public void deleteTrade(Long id) {
        Trade trade = getTradeById(id);
        accountTradeRepository.delete(trade);
    }
}
