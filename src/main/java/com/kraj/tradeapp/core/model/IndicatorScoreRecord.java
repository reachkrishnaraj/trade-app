package com.kraj.tradeapp.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
public class IndicatorScoreRecord {

    private String symbol;

    private String key; // symbol + candleType + interval + name. e.g: NQ_CLASSIC_1M_RSI

    private String candleType;

    private String interval;

    private String name;

    private String displayName;

    private ZonedDateTime dateTime;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    private BigDecimal score;

    private BigDecimal scorePercentage;

    private String direction;

    private String lastMsg;

    private List<IndicatorSubCategoryScoreRecord> subCategoryScores;

    public static String getKeyFor(String symbol, String candleType, String interval, String name) {
        return StringUtils.upperCase(StringUtils.joinWith("_", symbol, candleType, interval, name));
    }
}
