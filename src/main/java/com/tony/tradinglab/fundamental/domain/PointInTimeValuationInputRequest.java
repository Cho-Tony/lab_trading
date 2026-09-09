package com.tony.tradinglab.fundamental.domain;

import java.time.LocalDate;

public record PointInTimeValuationInputRequest(

        Long stockId,
        String symbol,

        LocalDate observationDate,

        ValuationMetrics valuation,

        GrowthTrendAnalysis growth,

        ProfitabilityTrendAnalysis profitability

) {
}