package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public enum Indicator {
    Q_LINE,
    Q_CLOUD,
    Q_BANDS,
    Q_WAVE,
    Q_MOMENTUM,
    Q_MONEY_BALL,
    Q_GRID,
    Q_SMC_TREND_RIBBON,
    Q_SMC_2ND_TREND_RIBBON,
    Q_ELITE,
    BJ_KEY_LEVELS,
    SR_CHANNEL_V2,
    SMC_CONCEPTS,
    BPR_DETECTOR,
    CONSOLIDATION_ZONE_TRACKER,
    LIQUIDITY_SWEEP_DETECTOR,
    SMT_DIVERGENCE_DETECTOR,
    DELTA_TURNAROUND,
    SPEED_OF_TAPE,
    BIG_TRADES,
    FVG_DETECTOR,
    ABSORPTION,
    EXHAUSTION,
    STACKED_IMBALANCE,
    SMC_BREAKOUT,
    PRICE_DROP_DETECTOR,
    IFVG_DETECTOR,
    FVG_REJECTION_DETECTOR,
    Q_ORACLE_SQUEEZER,
    DISPLACEMENT_DETECTOR,
    UNKNOWN;

    public static Indicator fromString(@Nullable String indicator) {
        for (Indicator i : Indicator.values()) {
            if (StringUtils.equalsAnyIgnoreCase(i.name(), indicator)) {
                return i;
            }
        }
        return Indicator.UNKNOWN;
    }
}
