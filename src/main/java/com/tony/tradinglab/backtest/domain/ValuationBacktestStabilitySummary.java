package com.tony.tradinglab.backtest.domain;

import java.math.BigDecimal;
import java.util.List;

public record ValuationBacktestStabilitySummary(

        ValuationBacktestMethod method,
        BacktestHorizon horizon,

        List<YearlyValuationBacktestSummary> yearlySummaries,

        int analyzedYearCount,

        int negativeCorrelationYearCount,
        int positiveSpreadYearCount,

        BigDecimal averageSpearmanCorrelation,
        BigDecimal averageQuintileSpreadPctPoint,

        BigDecimal worstQuintileSpreadPctPoint,
        BigDecimal bestQuintileSpreadPctPoint

) {
}