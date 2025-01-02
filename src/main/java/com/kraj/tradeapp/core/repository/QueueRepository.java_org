package com.kraj.tradeapp.core.repository;

import com.kraj.tradeapp.core.model.persistance.QueueRecord;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueRepository extends JpaRepository<QueueRecord, Long> {
    @Modifying
    @Query("UPDATE QueueRecord q SET q.status = 'PROCESSED', q.processedAt = CURRENT_TIMESTAMP WHERE q.id = :id")
    @Transactional
    void updateAsProcessed(@Param("id") Long id);

    @Query("SELECT q FROM QueueRecord q WHERE q.status = :status AND q.createdAt <= :cutoffTime")
    List<QueueRecord> findByStatusAndCreatedBefore(@Param("status") String status, @Param("cutoffTime") LocalDateTime cutoffTime);
}
