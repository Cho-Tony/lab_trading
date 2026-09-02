package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;

public record FinancialHealthAnalysis(

        DebtBurdenLevel debtBurdenLevel,
        DebtContextSignal debtContextSignal,

        BigDecimal netDebt,
        BigDecimal netDebtToTtmFcfRatio,

        boolean positiveTtmFcf,
        boolean debtIncreasing,
        boolean capexExpanding,
        boolean revenueAccelerating,
        boolean profitabilityImproving

) {
}