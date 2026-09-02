package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class QuantFinancialHealthAnalyzerTest {

    private final QuantFinancialHealthAnalyzer analyzer =
            new QuantFinancialHealthAnalyzer();

    @Test
    void detectGrowthInvestmentCandidate() {

        DebtCapacityMetrics debtCapacity =
                new DebtCapacityMetrics(

                        2025,
                        "Q4",

                        new BigDecimal("120000000000"),
                        new BigDecimal("40000000000"),
                        new BigDecimal("80000000000"),

                        new BigDecimal("100000000000"),
                        new BigDecimal("66666666667"),

                        new BigDecimal("1.20"),
                        new BigDecimal("1.80"),

                        false,

                        LocalDate.of(
                                2025,
                                10,
                                31
                        )
                );


        FinancialHealthTrend healthTrend =
                new FinancialHealthTrend(

                        2025,
                        "Q4",

                        new BigDecimal("80000000000"),
                        new BigDecimal("70000000000"),
                        new BigDecimal("10000000000"),

                        new BigDecimal("1.50"),
                        new BigDecimal("1.30"),
                        new BigDecimal("0.20"),

                        new BigDecimal("0.33"),
                        new BigDecimal("0.40"),
                        new BigDecimal("-0.07"),

                        LocalDate.of(
                                2025,
                                10,
                                31
                        )
                );


        CapitalInvestmentMetrics capitalInvestment =
                new CapitalInvestmentMetrics(

                        2025,
                        "Q4",

                        new BigDecimal("150000000000"),

                        new BigDecimal("15000000000"),
                        new BigDecimal("8000000000"),

                        new BigDecimal("87.50"),
                        new BigDecimal("10.00"),

                        LocalDate.of(
                                2025,
                                10,
                                31
                        )
                );


        GrowthTrendAnalysis revenueGrowth =
                new GrowthTrendAnalysis(

                        GrowthTrend.ACCELERATING,

                        new BigDecimal("30.00"),
                        new BigDecimal("8.00"),

                        2,
                        0,
                        3
                );


        ProfitabilityTrendAnalysis profitability =
                new ProfitabilityTrendAnalysis(

                        ProfitabilityDirection.IMPROVING,
                        ProfitabilityDirection.IMPROVING,

                        new BigDecimal("20.00"),
                        new BigDecimal("15.00"),

                        new BigDecimal("2.50"),
                        new BigDecimal("2.00"),

                        3
                );


        FinancialHealthAnalysis result =
                analyzer.analyze(
                        debtCapacity,
                        healthTrend,
                        capitalInvestment,
                        revenueGrowth,
                        profitability
                );


        System.out.println(
                "Debt Burden = "
                        + result.debtBurdenLevel()
        );

        System.out.println(
                "Debt Context = "
                        + result.debtContextSignal()
        );

        System.out.println(
                "Debt Increasing = "
                        + result.debtIncreasing()
        );

        System.out.println(
                "CapEx Expanding = "
                        + result.capexExpanding()
        );


        assertThat(
                result.debtBurdenLevel()
        ).isEqualTo(
                DebtBurdenLevel.MODERATE
        );

        assertThat(
                result.debtContextSignal()
        ).isEqualTo(
                DebtContextSignal
                        .GROWTH_INVESTMENT_CANDIDATE
        );

        assertThat(
                result.debtIncreasing()
        ).isTrue();

        assertThat(
                result.capexExpanding()
        ).isTrue();
    }
}