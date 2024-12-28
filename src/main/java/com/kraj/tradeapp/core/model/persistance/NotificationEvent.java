package com.kraj.tradeapp.core.model.persistance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime datetime;

    @Column
    private String source;

    @Column
    private String indicator;

    @Column
    private String derivedValue;

    @Column
    private String direction;

    @Column
    private String category;

    @Column(columnDefinition = "TEXT")
    private String rawMsg;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(length = 20)
    private String interval;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "lastupdated")
    private LocalDateTime lastUpdated;

    @Column
    private boolean isStrategy;
}
