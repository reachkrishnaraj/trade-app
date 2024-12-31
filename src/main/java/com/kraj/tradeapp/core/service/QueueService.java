package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.persistance.QueueRecord;
import com.kraj.tradeapp.core.repository.QueueRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;

    public List<QueueRecord> findPendingRecords() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(20);
        return queueRepository.findByStatusAndCreatedBefore("PENDING", cutoffTime);
    }

    public void markAsProcessed(Long id) {
        queueRepository.updateAsProcessed(id);
    }
}
