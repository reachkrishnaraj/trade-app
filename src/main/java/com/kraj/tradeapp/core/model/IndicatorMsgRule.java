package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndicatorMsgRule {

    private String indicatorName;

    private String indicatorDisplayName;

    private String description;

    private String matchType;

    private String alertMessage;

    private String interval;

    private String subCategory;

    private String indicatorSubCategoryDisplayName;

    @Nullable
    private String isSkipScoring;

    @Nullable
    private BigDecimal score;

    @Nullable
    private BigDecimal scoreRangeMin;

    @Nullable
    private BigDecimal scoreRangeMax;

    @Nullable
    private String scoreRange;

    private String direction;

    private boolean isAlertable;

    private List<String> textTimeframes;

    private List<String> callTimeframes;

    private List<String> announceTimeframes;
}
