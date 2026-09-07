package com.tony.tradinglab.backtest.domain;

import com.tony.tradinglab.market.domain.MarketRegime;

public record MarketRegimeValuationBacktestSummary(

        MarketRegime regime,

        ValuationBacktestMethod method,

        BacktestHorizon horizon,

        ValuationMethodBacktestSummary summary

) {
}