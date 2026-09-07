package com.tony.tradinglab.backtest.domain;

import java.util.List;
import java.util.Optional;

public record ValuationStrategyStabilityComparisonReport(

        List<ValuationBacktestStabilitySummary> summaries

) {

    public Optional<ValuationBacktestStabilitySummary> find(
            ValuationBacktestMethod method,
            BacktestHorizon horizon
    ) {

        return summaries.stream()
                .filter(
                        summary ->
                                summary.method() == method
                                        && summary.horizon() == horizon
                )
                .findFirst();
    }
}