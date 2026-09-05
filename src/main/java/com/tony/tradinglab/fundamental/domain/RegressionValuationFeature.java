package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegressionValuationFeature(

        Long stockId,
        String symbol,

        LocalDate priceDate,

        RegressionValuationMetric metric,

        BigDecimal valuationMultiple,

        BigDecimal revenueGrowthPct,
        BigDecimal operatingMarginPct

) {
}