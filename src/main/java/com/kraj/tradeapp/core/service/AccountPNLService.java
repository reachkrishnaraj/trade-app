package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.persistance.AccountPNL;
import com.kraj.tradeapp.core.repository.AccountPNLRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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

    public AccountPNL createPNL(AccountPNL accountPNL) {
        ZonedDateTime currentTimestamp = ZonedDateTime.now();

        if (accountPNL.getCreatedTs() == null) {
            accountPNL.setCreatedTs(currentTimestamp);
        }

        accountPNL.setLastUpdatedTs(currentTimestamp);
        accountPNL.setAccountIdDate(accountPNL.getAccountId() + "-" + currentTimestamp.toLocalDate());
        return accountPNLRepository.save(accountPNL);
    }

    public AccountPNL updatePNL(String accountId, AccountPNL accountPNL) {
        AccountPNL existingAccountPNL = accountPNLRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("AccountPNL not found for accountId: " + accountId));

        ZonedDateTime currentTimestamp = ZonedDateTime.now();
        accountPNL.setAccountId(accountId);
        accountPNL.setCreatedTs(existingAccountPNL.getCreatedTs());
        accountPNL.setLastUpdatedTs(currentTimestamp);

        return accountPNLRepository.save(accountPNL);
    }

    public void deletePNL(String accountId) {
        accountPNLRepository.deleteById(accountId);
    }
}
