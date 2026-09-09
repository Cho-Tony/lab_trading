package com.tony.tradinglab.backtest.domain;

public record ValuationBacktestRunReport(

        ValuationBacktestRequestPlan requestPlan,

        ValuationBacktestDataset dataset,

        ValuationStrategyComparisonReport overallComparison,

        ValuationStrategyStabilityComparisonReport stabilityComparison,

        MarketRegimeValuationBacktestReport marketRegimeComparison

) {
}