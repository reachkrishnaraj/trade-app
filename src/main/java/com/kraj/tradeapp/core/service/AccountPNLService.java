package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.persistance.AccountPNL;
import com.kraj.tradeapp.core.repository.AccountPNLRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class AccountPNLService {

    private final AccountPNLRepository accountPNLRepository;

    @Autowired
    public AccountPNLService(AccountPNLRepository accountPNLRepository) {
        this.accountPNLRepository = accountPNLRepository;
    }

    public List<AccountPNL> getPNLForDateRange(String accountId, LocalDate startDate, LocalDate endDate) {
        return accountPNLRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate);
    }

    public List<AccountPNL> getPNLForDate(String accountId, LocalDate date) {
        return accountPNLRepository.findByAccountIdAndDate(accountId, date);
    }

    public List<AccountPNL> getAllPNLForDate(LocalDate date) {
        return accountPNLRepository.findByDate(date);
    }
}
