package com.kraj.tradeapp.core.model.persistance;

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
        @Index(name = "idx_candle_type", columnList = "candle_type"),
        @Index(name = "idx_trade_signal_process_status", columnList = "trade_signal_process_status"),
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

    @Column(nullable = false, name = "indicator_display_name")
    private String indicatorDisplayName;

    @Column(nullable = false)
    private String direction;

    @Column(nullable = false, name = "indicator_sub_category")
    private String indicatorSubCategory;

    @Column(nullable = false, name = "indicator_sub_category_display_name")
    private String indicatorSubCategoryDisplayName;

    @Column(name = "raw_alert_msg", nullable = false)
    private String rawAlertMsg;

    @Column(name = "raw_payload", nullable = false)
    private String rawPayload;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String interval;

    @Column(nullable = false, name = "candle_type")
    private String candleType;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime created;

    @Column(name = "lastupdated_ts", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(nullable = false, name = "trade_signal_process_status")
    private String tradeSignalProcessStatus;

    @Column(nullable = false)
    private BigDecimal score;

    @Column(nullable = false, name = "min_score")
    private BigDecimal minScore;

    @Column(nullable = false, name = "max_score")
    private BigDecimal maxScore;

    @Column(nullable = false, name = "score_percent")
    private BigDecimal scorePercent;

    @Column(name = "is_strategy", nullable = false)
    private boolean isStrategy;

    @Column(name = "strategy_name")
    private String strategyName;

    @Column(nullable = false, name = "strategy_process_status")
    private String strategyProcessStatus;

    @Column(name = "strategy_processed_at")
    private LocalDateTime strategyProcessedAt;

    @Column(name = "strategy_process_msg")
    private String strategyProcessMsg;

    @Column(name = "is_alertable", nullable = false)
    private boolean isAlertable;
}
