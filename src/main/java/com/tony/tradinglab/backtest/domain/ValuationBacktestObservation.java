package com.tony.tradinglab.backtest.domain;

import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;

import java.time.LocalDate;

public record ValuationBacktestObservation(

        Long stockId,

        String symbol,

        LocalDate observationDate,

        ValuationComparisonResult valuationComparison,

        ForwardReturnMetrics forwardReturns

) {
}