package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;

public record FreeCashFlowTrendAnalysis(

        FreeCashFlowDirection direction,

        BigDecimal latestFreeCashFlow,
        BigDecimal latestFreeCashFlowMarginPct,
        BigDecimal latestGrowthYoYPct,

        int positiveGrowthCount,
        int negativeGrowthCount,
        int negativeFcfCount,

        int analyzedQuarterCount

) {
}