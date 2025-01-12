package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class IndicatorSubCategoryRange {

    private String subCategory;

    @Nullable
    private BigDecimal minScore;

    @Nullable
    private BigDecimal maxScore;
}
