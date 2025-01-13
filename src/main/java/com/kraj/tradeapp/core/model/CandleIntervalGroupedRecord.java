package com.kraj.tradeapp.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandleIntervalGroupedRecord {

    private String symbol;

    private String key; // symbol + candleType + interval

    private String candleType;

    private String interval;

    private String direction;

    private String lastMsg;

    private String lastMsgDateTime;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    private BigDecimal score;

    private BigDecimal scorePercentage;

    private LocalDateTime dateTime;

    private List<IndicatorScoreRecord> indicatorScoreRecords;

    public static String getKeyFor(String symbol, String candleType, String interval) {
        return StringUtils.upperCase(StringUtils.joinWith("_", symbol, candleType, interval));
    }
}
