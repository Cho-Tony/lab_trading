package com.tony.tradinglab.fundamental.dto;

import java.math.BigDecimal;

public record FundamentalData(

        String symbol,

        // 기업 규모
        BigDecimal marketCap,

        // Valuation
        BigDecimal peRatio,
        BigDecimal forwardPeRatio,
        BigDecimal pegRatio,
        BigDecimal priceToSales,
        BigDecimal priceToBook,
        BigDecimal evToEbitda,

        // Growth
        BigDecimal revenueGrowthYoY,
        BigDecimal epsGrowthYoY,

        // Profitability
        BigDecimal grossMargin,
        BigDecimal operatingMargin,
        BigDecimal netMargin,
        BigDecimal roe,
        BigDecimal roic,

        // Financial health
        BigDecimal debtToEquity,

        // Cash generation
        BigDecimal freeCashFlow,
        BigDecimal freeCashFlowYield

) {
}