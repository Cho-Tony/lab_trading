package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;

public record GrowthTrendAnalysis(

        GrowthTrend trend,

        BigDecimal latestYoyGrowthPct,
        BigDecimal averageAccelerationPctPoint,

        int positiveAccelerationCount,
        int negativeAccelerationCount,

        int analyzedQuarterCount

) {
}