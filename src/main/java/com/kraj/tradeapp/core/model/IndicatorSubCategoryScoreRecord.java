package com.kraj.tradeapp.core.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndicatorSubCategoryScoreRecord {

    private String key; // symbol + candleType + interval + indicatorName + categoryName. e.g: NQ_CLASSIC_1M_RSI_OVERBOUGHT

    private String symbol;

    private String candleType;

    private String interval;

    private String indicatorName;

    private String indicatorDisplayName;

    private String name;

    private String displayName;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    private BigDecimal score;

    private String direction;

    private String lastMsg;

    private String lastMsgDateTime;

    private boolean isStrategy;

    private String strategyName;

    public static String getKeyFor(String symbol, String candleType, String interval, String indicatorName, String categoryName) {
        return StringUtils.upperCase(StringUtils.joinWith("_", symbol, candleType, interval, indicatorName, categoryName));
    }
}
