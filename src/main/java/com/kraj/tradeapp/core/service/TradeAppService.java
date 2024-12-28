package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.AccountPNL;
import com.kraj.tradeapp.core.model.ComputedTradeSignal;
import com.kraj.tradeapp.core.model.NotificationEvent;
import com.kraj.tradeapp.core.model.Trade;
import com.kraj.tradeapp.core.repository.AccountPNLRepository;
import com.kraj.tradeapp.core.repository.ComputedTradeSignalRepository;
import com.kraj.tradeapp.core.repository.NotificationEventRepository;
import com.kraj.tradeapp.core.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TradeAppService {

    private final AccountPNLRepository accountPNLRepository;
    private final ComputedTradeSignalRepository computedTradeSignalRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final TradeRepository tradeRepository;

    @Autowired
    public TradeAppService(
        AccountPNLRepository accountPNLRepository,
        ComputedTradeSignalRepository computedTradeSignalRepository,
        NotificationEventRepository notificationEventRepository,
        TradeRepository tradeRepository
    ) {
        this.accountPNLRepository = accountPNLRepository;
        this.computedTradeSignalRepository = computedTradeSignalRepository;
        this.notificationEventRepository = notificationEventRepository;
        this.tradeRepository = tradeRepository;
    }

    public AccountPNL storeAccountPNL() {
        AccountPNL accountPNL = new AccountPNL();
        accountPNL.setAccountIdDate("someIdDate");
        accountPNL.setAccountId("someAccountId");
        accountPNL.setAccountName("someAccountName");
        accountPNL.setProfitLoss(BigDecimal.valueOf(1000));
        accountPNL.setDate(LocalDate.now());
        accountPNL.setCreatedTs(LocalDateTime.now());
        accountPNL.setLastUpdatedTs(LocalDateTime.now());
        return accountPNLRepository.save(accountPNL);
    }

    public ComputedTradeSignal storeComputedTradeSignal() {
        ComputedTradeSignal computedTradeSignal = new ComputedTradeSignal();
        computedTradeSignal.setDatetime(LocalDateTime.now());
        computedTradeSignal.setSymbol("someSymbol");
        computedTradeSignal.setSignalType("BUY");
        computedTradeSignal.setConfidence(BigDecimal.valueOf(95.5));
        computedTradeSignal.setReason("someReason");
        computedTradeSignal.setSource("someSource");
        computedTradeSignal.setCreatedTs(LocalDateTime.now());
        computedTradeSignal.setLastUpdated(LocalDateTime.now());
        return computedTradeSignalRepository.save(computedTradeSignal);
    }

    public NotificationEvent storeNotificationEvent() {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setDatetime(LocalDateTime.now());
        notificationEvent.setSource("someSource");
        notificationEvent.setIndicator("someIndicator");
        notificationEvent.setSignal("someSignal");
        notificationEvent.setRawMsg("someRawMsg");
        notificationEvent.setPrice(BigDecimal.valueOf(100.5));
        notificationEvent.setInterval("someInterval");
        notificationEvent.setCreated(LocalDateTime.now());
        notificationEvent.setLastUpdated(LocalDateTime.now());
        return notificationEventRepository.save(notificationEvent);
    }

    public Trade storeTrade() {
        Trade trade = new Trade();
        trade.setDatetime(LocalDateTime.now());
        trade.setAccountId("someAccountId");
        trade.setAccountName("someAccountName");
        trade.setSymbol("someSymbol");
        trade.setStatus("IN_PROGRESS");
        trade.setQuantity(BigDecimal.valueOf(10));
        trade.setPrice(BigDecimal.valueOf(100.5));
        trade.setTradeType("BUY");
        trade.setCreatedTs(LocalDateTime.now());
        trade.setLastUpdatedTs(LocalDateTime.now());
        return tradeRepository.save(trade);
    }
}
