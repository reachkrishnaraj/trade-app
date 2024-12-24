package com.kraj.tradeapp.core.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_event")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime datetime;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(nullable = false, length = 50)
    private String indicator;

    @Column(nullable = false, length = 20)
    private String signal;

    @Column(columnDefinition = "TEXT")
    private String rawMsg;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 20)
    private String interval;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "lastupdated")
    private LocalDateTime lastUpdated;
}
