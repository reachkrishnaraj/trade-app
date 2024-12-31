package com.kraj.tradeapp.core.model.persistance;

import com.kraj.tradeapp.core.model.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "notification_events",
    indexes = {
        @Index(name = "idx_event_datetime", columnList = "event_datetime"),
        @Index(name = "idx_symbol", columnList = "symbol"),
        @Index(name = "idx_interval", columnList = "interval"),
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_app_default_seq_gen")
    @SequenceGenerator(name = "trade_app_default_seq_gen", sequenceName = "trade_app_def_seq")
    private Long id;

    @Column(name = "event_datetime")
    private LocalDateTime datetime;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String indicator;

    @Column(name = "derived_value", nullable = false)
    private String derivedValue;

    @Column(nullable = false)
    private String direction;

    @Column(nullable = false)
    private String category;

    @Column(name = "raw_msg", nullable = false)
    private String rawMsg;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String interval;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime created;

    @Column(name = "lastupdated_ts", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "is_strategy", nullable = false)
    private boolean isStrategy;

    @Column(nullable = false)
    private String importance;

    @Column(nullable = false)
    private String tradeAction;
}
