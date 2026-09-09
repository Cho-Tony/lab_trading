package com.tony.tradinglab.backtest.domain;

import com.tony.tradinglab.fundamental.domain.ValuationSnapshotAnalysisReport;
import com.tony.tradinglab.price.domain.StockPrice;

import java.util.List;
import java.util.Map;

public record ValuationBacktestSnapshotRequest(

        ValuationSnapshotAnalysisReport valuationReport,

        Map<Long, List<StockPrice>> stockPricesByStockId,

        String benchmarkSymbol,

        List<StockPrice> benchmarkPrices

) {
}