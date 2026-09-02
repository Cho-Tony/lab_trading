package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;

public record ProfitabilityTrendAnalysis(

        ProfitabilityDirection operatingMarginDirection,
        ProfitabilityDirection netMarginDirection,

        BigDecimal latestOperatingMarginPct,
        BigDecimal latestNetMarginPct,

        BigDecimal averageOperatingMarginChangePctPoint,
        BigDecimal averageNetMarginChangePctPoint,

        int analyzedQuarterCount

) {
}