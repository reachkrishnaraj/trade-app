package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.crosstrade.client.CrossTradeApiClient;
import com.kraj.tradeapp.core.model.dto.Account;
import com.kraj.tradeapp.core.model.persistance.AccountPNL;
import com.kraj.tradeapp.core.repository.AccountPNLRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class CrossTradeService {

    @Autowired
    private CrossTradeApiClient crossTradeApiClient;

    @Autowired
    private AccountPNLRepository accountPNLRepository;

    public AccountPNL getCurrentAccountGrossPNL(String accountId) {
        Account account = crossTradeApiClient.getAccount(accountId);
        if (account != null) {
            AccountPNL accountPNL = new AccountPNL();
            accountPNL.setAccountId(String.valueOf(account.getId()));
            accountPNL.setAccountName(account.getName());
            accountPNL.setProfitLoss(BigDecimal.valueOf(account.getItem().getGrossRealizedProfitLoss()));
            accountPNL.setDate(LocalDate.now());
            accountPNL.setCreatedTs(LocalDateTime.now());
            accountPNL.setAccountIdDate(account.getId() + "_" + LocalDate.now());
            return accountPNL;
        }
        return null;
    }

    public List<AccountPNL> getAllAccountsPNL() {
        List<Account> accounts = crossTradeApiClient.getAccounts();
        return accounts.stream().map(account -> getCurrentAccountGrossPNL(String.valueOf(account.getId()))).collect(Collectors.toList());
    }
}
