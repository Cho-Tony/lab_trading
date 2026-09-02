package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.CandidateQualityAssessment;
import com.tony.tradinglab.discovery.domain.CandidateQualityDecision;
import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateQualityGateTest {

    private final CandidateQualityGate gate =
            new CandidateQualityGate();


    @Test
    void passHealthyGrowingCompany() {

        CandidateQualityAssessment result =
                gate.evaluate(

                        acceleratingRevenue(),

                        improvingProfitability(),

                        improvingFcf(),

                        healthyFinancialHealth(),

                        healthyLiquidity(),

                        normalCapex()
                );


        System.out.println(
                "Decision = "
                        + result.decision()
        );

        System.out.println(
                "Reasons = "
                        + result.reasons()
        );


        assertThat(
                result.decision()
        ).isEqualTo(
                CandidateQualityDecision.PASS
        );
    }


    @Test
    void reviewGrowthInvestmentCompany() {

        CandidateQualityAssessment result =
                gate.evaluate(

                        acceleratingRevenue(),

                        improvingProfitability(),

                        persistentNegativeFcf(),

                        growthInvestmentFinancialHealth(),

                        investmentLiquidity(),

                        expandingCapex()
                );


        System.out.println(
                "Decision = "
                        + result.decision()
        );

        System.out.println(
                "Reasons = "
                        + result.reasons()
        );


        assertThat(
                result.decision()
        ).isEqualTo(
                CandidateQualityDecision.REVIEW
        );
    }


    @Test
    void rejectClearlyDeterioratingCompany() {

        CandidateQualityAssessment result =
                gate.evaluate(

                        deceleratingRevenue(),

                        deterioratingProfitability(),

                        deterioratingFcf(),

                        riskyFinancialHealth(),

                        dangerousLiquidity(),

                        normalCapex()
                );


        System.out.println(
                "Decision = "
                        + result.decision()
        );

        System.out.println(
                "Reasons = "
                        + result.reasons()
        );


        assertThat(
                result.decision()
        ).isEqualTo(
                CandidateQualityDecision.REJECT
        );
    }


    // ---------------------------
    // Test Fixtures
    // ---------------------------


    private GrowthTrendAnalysis acceleratingRevenue() {

        return new GrowthTrendAnalysis(
                GrowthTrend.ACCELERATING,
                new BigDecimal("30"),
                new BigDecimal("8"),
                2,
                0,
                3
        );
    }


    private GrowthTrendAnalysis deceleratingRevenue() {

        return new GrowthTrendAnalysis(
                GrowthTrend.DECELERATING,
                new BigDecimal("-10"),
                new BigDecimal("-8"),
                0,
                2,
                3
        );
    }


    private ProfitabilityTrendAnalysis improvingProfitability() {

        return new ProfitabilityTrendAnalysis(

                ProfitabilityDirection.IMPROVING,
                ProfitabilityDirection.IMPROVING,

                new BigDecimal("20"),
                new BigDecimal("15"),

                new BigDecimal("2"),
                new BigDecimal("2"),

                3
        );
    }


    private ProfitabilityTrendAnalysis deterioratingProfitability() {

        return new ProfitabilityTrendAnalysis(

                ProfitabilityDirection.DETERIORATING,
                ProfitabilityDirection.DETERIORATING,

                new BigDecimal("2"),
                new BigDecimal("-3"),

                new BigDecimal("-4"),
                new BigDecimal("-5"),

                3
        );
    }


    private FreeCashFlowTrendAnalysis improvingFcf() {

        return new FreeCashFlowTrendAnalysis(

                FreeCashFlowDirection.IMPROVING,

                new BigDecimal("5000000000"),
                new BigDecimal("15"),
                new BigDecimal("30"),

                3,
                0,
                0,
                3
        );
    }


    private FreeCashFlowTrendAnalysis persistentNegativeFcf() {

        return new FreeCashFlowTrendAnalysis(

                FreeCashFlowDirection.PERSISTENT_NEGATIVE,

                new BigDecimal("-2000000000"),
                new BigDecimal("-8"),
                null,

                0,
                0,
                3,
                3
        );
    }


    private FreeCashFlowTrendAnalysis deterioratingFcf() {

        return new FreeCashFlowTrendAnalysis(

                FreeCashFlowDirection.DETERIORATING,

                new BigDecimal("-3000000000"),
                new BigDecimal("-12"),
                new BigDecimal("-35"),

                0,
                3,
                2,
                3
        );
    }


    private FinancialHealthAnalysis healthyFinancialHealth() {

        return new FinancialHealthAnalysis(

                DebtBurdenLevel.LOW,
                DebtContextSignal.NO_DEBT_PRESSURE,

                new BigDecimal("10000000000"),
                new BigDecimal("0.80"),

                true,
                false,
                false,
                true,
                true
        );
    }


    private FinancialHealthAnalysis growthInvestmentFinancialHealth() {

        return new FinancialHealthAnalysis(

                DebtBurdenLevel.MODERATE,
                DebtContextSignal.GROWTH_INVESTMENT_CANDIDATE,

                new BigDecimal("30000000000"),
                new BigDecimal("1.80"),

                false,
                true,
                true,
                true,
                true
        );
    }


    private FinancialHealthAnalysis riskyFinancialHealth() {

        return new FinancialHealthAnalysis(

                DebtBurdenLevel.VERY_HIGH,
                DebtContextSignal.RISK_WARNING,

                new BigDecimal("100000000000"),
                null,

                false,
                true,
                false,
                false,
                false
        );
    }


    private LiquidityMetrics healthyLiquidity() {

        return new LiquidityMetrics(

                2025,
                "Q4",

                new BigDecimal("20000000000"),

                new BigDecimal("10000000000"),
                new BigDecimal("5000000000"),

                null,
                null,
                null,

                true,
                true,
                false,

                LocalDate.of(2025, 10, 31)
        );
    }


    private LiquidityMetrics investmentLiquidity() {

        return new LiquidityMetrics(

                2025,
                "Q4",

                new BigDecimal("12000000000"),

                new BigDecimal("2000000000"),
                new BigDecimal("-4000000000"),

                new BigDecimal("4000000000"),
                new BigDecimal("3"),
                new BigDecimal("12"),

                true,
                false,
                true,

                LocalDate.of(2025, 10, 31)
        );
    }


    private LiquidityMetrics dangerousLiquidity() {

        return new LiquidityMetrics(

                2025,
                "Q4",

                new BigDecimal("500000000"),

                new BigDecimal("-1000000000"),
                new BigDecimal("-1500000000"),

                new BigDecimal("1500000000"),
                new BigDecimal("0.33"),
                new BigDecimal("1.32"),

                false,
                false,
                false,

                LocalDate.of(2025, 10, 31)
        );
    }


    private CapitalInvestmentMetrics expandingCapex() {

        return new CapitalInvestmentMetrics(

                2025,
                "Q4",

                new BigDecimal("50000000000"),

                new BigDecimal("10000000000"),
                new BigDecimal("4000000000"),

                new BigDecimal("150"),
                new BigDecimal("20"),

                LocalDate.of(2025, 10, 31)
        );
    }


    private CapitalInvestmentMetrics normalCapex() {

        return new CapitalInvestmentMetrics(

                2025,
                "Q4",

                new BigDecimal("50000000000"),

                new BigDecimal("2000000000"),
                new BigDecimal("1900000000"),

                new BigDecimal("5"),
                new BigDecimal("4"),

                LocalDate.of(2025, 10, 31)
        );
    }
}