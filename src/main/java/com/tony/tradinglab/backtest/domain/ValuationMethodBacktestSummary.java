package com.tony.tradinglab.backtest.domain;

import java.math.BigDecimal;
import java.util.List;

public record ValuationMethodBacktestSummary(

        ValuationBacktestMethod method,

        BacktestHorizon horizon,

        int observationCount,

        BigDecimal spearmanCorrelation,

        List<QuintileReturnSummary> quintiles,

        BigDecimal cheapestQuintileAverageReturnPct,

        BigDecimal mostExpensiveQuintileAverageReturnPct,

        BigDecimal quintileSpreadPctPoint

) {
}