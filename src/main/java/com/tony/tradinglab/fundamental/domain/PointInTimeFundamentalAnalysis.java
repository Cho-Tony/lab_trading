package com.tony.tradinglab.fundamental.domain;

import java.time.LocalDate;

public record PointInTimeFundamentalAnalysis(

        Long stockId,
        String symbol,

        LocalDate asOfDate,

        TtmFinancials ttmFinancials,

        GrowthTrendAnalysis growth,

        ProfitabilityTrendAnalysis profitability

) {
}