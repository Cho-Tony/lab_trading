package com.tony.tradinglab.analysis.domain;

import com.tony.tradinglab.fundamental.domain.*;

public record QuantAnalysisSnapshot(

        GrowthTrendAnalysis revenueGrowth,

        ProfitabilityTrendAnalysis profitability,

        FreeCashFlowTrendAnalysis freeCashFlow,

        FinancialHealthAnalysis financialHealth,

        DebtCapacityMetrics debtCapacity,

        LiquidityMetrics liquidity,

        CapitalInvestmentMetrics capitalInvestment

) {
}