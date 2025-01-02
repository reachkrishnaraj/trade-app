package com.kraj.tradeapp.core.model.persistance;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "message_queue")
public class QueueRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_app_default_seq_gen")
    @SequenceGenerator(name = "trade_app_default_seq_gen", sequenceName = "trade_app_def_seq")
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(nullable = false)
    private String status = "PENDING";
}
