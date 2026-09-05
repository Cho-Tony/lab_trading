package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;

public record RegressionAdjustedValuationAssessment(

        String symbol,

        RegressionValuationMetric metric,

        BigDecimal actualMultiple,
        BigDecimal predictedMultiple,

        BigDecimal actualLogMultiple,
        BigDecimal predictedLogMultiple,

        BigDecimal residual,

        BigDecimal relativeDeviationPct,

        BigDecimal intercept,
        BigDecimal revenueGrowthCoefficient,
        BigDecimal operatingMarginCoefficient,

        BigDecimal rSquared,

        int peerCount

) {
}