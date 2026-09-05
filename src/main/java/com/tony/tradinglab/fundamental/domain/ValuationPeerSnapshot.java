package com.tony.tradinglab.fundamental.domain;

public record ValuationPeerSnapshot(

        Long stockId,
        String symbol,

        String sector,
        String industry,

        ValuationMetrics valuation,

        GrowthTrendAnalysis growth,

        ProfitabilityTrendAnalysis profitability

) {
}