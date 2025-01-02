package com.kraj.tradeapp.core.model.persistance;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "signal_category_record")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignalCategoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_app_default_seq_gen")
    @SequenceGenerator(name = "trade_app_default_seq_gen", sequenceName = "trade_app_def_seq")
    private String id;

    @Column
    private String symbol;

    @Column
    private String category;

    @Column
    private String value;

    @Column
    private String interval;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column
    private LocalDateTime lastUpdated;

    @Column
    private LocalDateTime receivedDateTime;
}
