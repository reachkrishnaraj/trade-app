package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.AccountPNL;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountPNLRepository extends JpaRepository<AccountPNL, String> {
    List<AccountPNL> findByAccountIdAndDateBetween(String accountId, LocalDate startDate, LocalDate endDate);

    List<AccountPNL> findByAccountIdAndDate(String accountId, LocalDate date);

    List<AccountPNL> findByDate(LocalDate date);
}
