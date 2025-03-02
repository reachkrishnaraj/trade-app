package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.SqlOHLCData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SqlOhlcDataRepository extends JpaRepository<SqlOHLCData, Long> {}
