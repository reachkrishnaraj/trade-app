package com.kraj.tradeapp.core.model;

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum Indicator {
    Q_LINE(EventImportance.HIGH, false),
    Q_CLOUD(EventImportance.HIGH, false),
    Q_BANDS(EventImportance.HIGH, false),
    Q_WAVE(EventImportance.HIGH, false),
    Q_MOMENTUM(EventImportance.HIGH, false),
    Q_MONEY_BALL(EventImportance.HIGH, false),
    Q_GRID(EventImportance.HIGH, false),
    Q_SMC_TREND_RIBBON(EventImportance.HIGH, false),
    Q_SMC_2ND_TREND_RIBBON(EventImportance.MEDIUM, false),
    Q_ORACLE_SQUEEZER(EventImportance.HIGH, false),
    BJ_KEY_LEVELS(EventImportance.HIGH, false),
    SR_CHANNEL_V2(EventImportance.CRITICAL, false),
    SMC_CONCEPTS(EventImportance.HIGH, false),
    BPR_DETECTOR(EventImportance.CRITICAL, false),
    CONSOLIDATION_ZONE_TRACKER(EventImportance.CRITICAL, false),
    LIQUIDITY_SWEEP_DETECTOR(EventImportance.CRITICAL, false),
    SMT_DIVERGENCE_DETECTOR(EventImportance.CRITICAL, false),
    DELTA_TURNAROUND(EventImportance.HIGH, false),
    SPEED_OF_TAPE(EventImportance.CRITICAL, false),
    BIG_TRADES(EventImportance.CRITICAL, false),
    FVG_DETECTOR(EventImportance.HIGH, false),
    ABSORPTION(EventImportance.MEDIUM, false),
    EXHAUSTION(EventImportance.MEDIUM, false),
    STACKED_IMBALANCES(EventImportance.MEDIUM, false),
    SMC_BREAKOUT(EventImportance.HIGH, false),
    ALGO_ALPHA_ST(EventImportance.CRITICAL, false),
    SUPERTREND(EventImportance.HIGH, false),
    PIVOT_POINT_ST(EventImportance.HIGH, false),
    UT_BOT(EventImportance.HIGH, false),
    PRICE_DROP_DETECTOR(EventImportance.CRITICAL, false),
    IFVG_DETECTOR(EventImportance.CRITICAL, false),
    FVG_REJECTION_DETECTOR(EventImportance.CRITICAL, false),
    DISPLACEMENT_DETECTOR(EventImportance.CRITICAL, false),
    QUANTVUE_PROV2(EventImportance.CRITICAL, false),
    QUANTVUE_MOMENTUM_V2(EventImportance.CRITICAL, false),
    QUANTVUE_QGRID(EventImportance.CRITICAL, false),
    QUANTVUE_ORACLE_SQUEEZE(EventImportance.CRITICAL, false),
    QUANTVUE_QCVD(EventImportance.CRITICAL, false),
    UNKNOWN(EventImportance.TRIVIAL, false),

    //below are strategies, but adding here for simplicity
    QUANTVUE_QKRONOS(EventImportance.CRITICAL, true),
    QSUQUANTVUE_QSUMOMO(EventImportance.CRITICAL, true),
    QUANTVUE_QGRID_ELITE(EventImportance.CRITICAL, true),
    QUANTVUE_QCLOUD_TREND_TRACER(EventImportance.CRITICAL, true),
    QUANTVUE_QELITE(EventImportance.CRITICAL, true),
    QUANTVUE_QSCALPER(EventImportance.CRITICAL, true),

    LUXALGO_OSCILLATOR_MATRIX(EventImportance.CRITICAL, false),
    LUXALGO_PAC(EventImportance.CRITICAL, false),
    LUXALGO_SO(EventImportance.CRITICAL, false),
    LUXALGO_IFVG(EventImportance.CRITICAL, false),
    TFO_IFVG(EventImportance.CRITICAL, false),
    TIMELESS_IFVG_PRO_KIT(EventImportance.CRITICAL, false),
    DEALING_RANGE(EventImportance.CRITICAL, false);

    private final EventImportance defaultImportance;

    private final boolean isStrategy;

    private Indicator(EventImportance defaultImportance, boolean isStrategy) {
        this.defaultImportance = defaultImportance;
        this.isStrategy = isStrategy;
    }

    public static Indicator fromString(@Nullable String indicator) {
        for (Indicator i : Indicator.values()) {
            if (
                StringUtils.equalsAnyIgnoreCase(i.name(), indicator) ||
                StringUtils.equalsAnyIgnoreCase(i.name().replace("_", ""), indicator)
            ) {
                return i;
            }
        }
        return Indicator.UNKNOWN;
    }
}
