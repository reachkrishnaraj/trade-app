package com.kraj.tradeapp.core.model.persistance.mongodb;

import com.kraj.tradeapp.core.model.CandleIntervalGroupedRecord;
import com.kraj.tradeapp.core.model.IndicatorScoreRecord;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "trade_signal_score_snapshot")
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignalScoreSnapshot {

    @Id
    private String id;

    @Indexed
    private String symbol;

    @Indexed(expireAfterSeconds = 2 * 24 * 60 * 60) // 2 days
    private LocalDateTime dateTime;

    private List<CandleIntervalGroupedRecord> candleIntervalGroupedRecords;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    private BigDecimal score;

    private BigDecimal scorePercentage;

    private String direction;
}
