package com.tony.tradinglab.backtest.domain;

import com.tony.tradinglab.market.domain.MarketRegime;

import java.util.List;
import java.util.Optional;

public record MarketRegimeValuationBacktestReport(

        List<MarketRegimeValuationBacktestSummary> summaries

) {

    public Optional<MarketRegimeValuationBacktestSummary> find(
            MarketRegime regime,
            ValuationBacktestMethod method,
            BacktestHorizon horizon
    ) {

        return summaries.stream()

                .filter(
                        summary ->
                                summary.regime() == regime
                                        && summary.method() == method
                                        && summary.horizon() == horizon
                )

                .findFirst();
    }
}