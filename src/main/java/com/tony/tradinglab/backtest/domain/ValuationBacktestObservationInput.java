package com.tony.tradinglab.backtest.domain;

import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.price.domain.StockPrice;

import java.time.LocalDate;
import java.util.List;

public record ValuationBacktestObservationInput(

        Long stockId,
        String symbol,

        LocalDate observationDate,

        ValuationComparisonResult valuationComparison,

        List<StockPrice> stockPrices,

        String benchmarkSymbol,
        List<StockPrice> benchmarkPrices

) {
}