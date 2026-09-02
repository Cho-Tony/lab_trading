package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class QuantFinancialHealthAnalyzer {

    private static final BigDecimal LOW_DEBT_THRESHOLD =
            new BigDecimal("1.00");

    private static final BigDecimal MODERATE_DEBT_THRESHOLD =
            new BigDecimal("2.50");

    private static final BigDecimal HIGH_DEBT_THRESHOLD =
            new BigDecimal("4.00");

    private static final BigDecimal CAPEX_EXPANSION_THRESHOLD =
            new BigDecimal("20.00");

    public FinancialHealthAnalysis analyze(
            DebtCapacityMetrics debtCapacity,
            FinancialHealthTrend healthTrend,
            CapitalInvestmentMetrics capitalInvestment,
            GrowthTrendAnalysis revenueGrowth,
            ProfitabilityTrendAnalysis profitability
    ) {

        boolean positiveTtmFcf =
                debtCapacity.ttmFreeCashFlow() != null
                        && debtCapacity.ttmFreeCashFlow()
                        .compareTo(BigDecimal.ZERO) > 0;

        boolean debtIncreasing =
                healthTrend != null
                        && healthTrend.netDebtChange() != null
                        && healthTrend.netDebtChange()
                        .compareTo(BigDecimal.ZERO) > 0;

        boolean capexExpanding =
                capitalInvestment != null
                        && capitalInvestment.capexGrowthYoYPct() != null
                        && capitalInvestment.capexGrowthYoYPct()
                        .compareTo(
                                CAPEX_EXPANSION_THRESHOLD
                        ) >= 0;

        boolean revenueAccelerating =
                revenueGrowth != null
                        && revenueGrowth.trend()
                        == GrowthTrend.ACCELERATING;

        boolean profitabilityImproving =
                isProfitabilityImproving(
                        profitability
                );

        DebtBurdenLevel debtBurdenLevel =
                determineDebtBurden(
                        debtCapacity,
                        positiveTtmFcf
                );

        DebtContextSignal debtContextSignal =
                determineDebtContext(
                        debtBurdenLevel,
                        positiveTtmFcf,
                        debtIncreasing,
                        capexExpanding,
                        revenueAccelerating,
                        profitabilityImproving
                );

        return new FinancialHealthAnalysis(

                debtBurdenLevel,
                debtContextSignal,

                debtCapacity.netDebt(),
                debtCapacity.netDebtToTtmFcfRatio(),

                positiveTtmFcf,
                debtIncreasing,
                capexExpanding,
                revenueAccelerating,
                profitabilityImproving
        );
    }

    private DebtBurdenLevel determineDebtBurden(
            DebtCapacityMetrics debtCapacity,
            boolean positiveTtmFcf
    ) {

        if (debtCapacity.netCash()) {
            return DebtBurdenLevel.NET_CASH;
        }

        if (!positiveTtmFcf
                || debtCapacity.netDebtToTtmFcfRatio() == null) {

            return DebtBurdenLevel.UNASSESSABLE;
        }

        BigDecimal ratio =
                debtCapacity.netDebtToTtmFcfRatio();

        if (ratio.compareTo(
                LOW_DEBT_THRESHOLD
        ) <= 0) {

            return DebtBurdenLevel.LOW;
        }

        if (ratio.compareTo(
                MODERATE_DEBT_THRESHOLD
        ) <= 0) {

            return DebtBurdenLevel.MODERATE;
        }

        if (ratio.compareTo(
                HIGH_DEBT_THRESHOLD
        ) <= 0) {

            return DebtBurdenLevel.HIGH;
        }

        return DebtBurdenLevel.VERY_HIGH;
    }

    private DebtContextSignal determineDebtContext(
            DebtBurdenLevel debtBurdenLevel,
            boolean positiveTtmFcf,
            boolean debtIncreasing,
            boolean capexExpanding,
            boolean revenueAccelerating,
            boolean profitabilityImproving
    ) {

        /*
         * 순현금이고 부채도 증가하지 않는다면
         * 특별한 부채 위험 신호가 없음.
         */
        if (debtBurdenLevel == DebtBurdenLevel.NET_CASH
                && !debtIncreasing) {

            return DebtContextSignal.NO_DEBT_PRESSURE;
        }

        /*
         * FCF가 적자인 상태에서
         * 순부채까지 증가한다면 위험 신호.
         */
        if (!positiveTtmFcf
                && debtIncreasing) {

            return DebtContextSignal.RISK_WARNING;
        }

        /*
         * 이미 부채 부담이 큰데
         * 부채가 계속 늘어나면 위험.
         */
        if (debtIncreasing
                && (
                debtBurdenLevel == DebtBurdenLevel.HIGH
                        || debtBurdenLevel
                        == DebtBurdenLevel.VERY_HIGH
        )) {

            return DebtContextSignal.RISK_WARNING;
        }

        /*
         * 부채 증가와 CapEx 확대가 동시에 발생하고,
         * 현금창출력이 양수이며,
         * 사업 성장 신호도 존재한다면
         *
         * 성장투자일 가능성이 있으므로
         * AI가 이유를 조사하도록 후보로 분류.
         */
        if (debtIncreasing
                && capexExpanding
                && positiveTtmFcf
                && (
                revenueAccelerating
                        || profitabilityImproving
        )) {

            return DebtContextSignal
                    .GROWTH_INVESTMENT_CANDIDATE;
        }

        /*
         * 숫자만으로 이유를 판단하기 어려움.
         */
        if (debtIncreasing) {

            return DebtContextSignal.REVIEW_REQUIRED;
        }

        return DebtContextSignal.NO_DEBT_PRESSURE;
    }

    private boolean isProfitabilityImproving(
            ProfitabilityTrendAnalysis profitability
    ) {

        if (profitability == null) {
            return false;
        }

        return profitability.operatingMarginDirection()
                == ProfitabilityDirection.IMPROVING

                || profitability.netMarginDirection()
                == ProfitabilityDirection.IMPROVING;
    }
}