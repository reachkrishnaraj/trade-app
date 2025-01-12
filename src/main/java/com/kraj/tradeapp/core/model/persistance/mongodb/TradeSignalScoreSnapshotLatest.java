package com.kraj.tradeapp.core.model.persistance.mongodb;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "trade_signal_score_snapshot_latest")
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignalScoreSnapshotLatest {

    @Id
    private String symbol;

    private LocalDateTime lastUpdated;

    private String latestRecordId;
}
