package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FinancialHealthMetrics;
import com.tony.tradinglab.fundamental.domain.LiquidityMetrics;
import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LiquidityMetricsCalculatorTest {

    private final LiquidityMetricsCalculator calculator =
            new LiquidityMetricsCalculator();

    @Test
    void calculateCapexDrivenNegativeFcfLiquidity() {

        FinancialHealthMetrics health =
                new FinancialHealthMetrics(

                        2025,
                        "Q4",

                        new BigDecimal("12000000000"),
                        new BigDecimal("50000000000"),
                        new BigDecimal("20000000000"),
                        new BigDecimal("15000000000"),

                        new BigDecimal("3000000000"),

                        new BigDecimal("0.75"),
                        new BigDecimal("0.80"),

                        LocalDate.of(
                                2025,
                                10,
                                31
                        )
                );


        TtmCashFlow cashFlow =
                new TtmCashFlow(

                        2025,
                        "Q4",

                        new BigDecimal("2000000000"),

                        new BigDecimal("6000000000"),

                        new BigDecimal("-4000000000"),

                        LocalDate.of(
                                2025,
                                10,
                                31
                        )
                );


        List<LiquidityMetrics> result =
                calculator.calculate(
                        List.of(health),
                        List.of(cashFlow)
                );


        assertThat(result)
                .hasSize(1);


        LiquidityMetrics metrics =
                result.get(0);


        System.out.println(
                "Cash = "
                        + metrics.cash()
        );

        System.out.println(
                "TTM OCF = "
                        + metrics.ttmOperatingCashFlow()
        );

        System.out.println(
                "TTM FCF = "
                        + metrics.ttmFreeCashFlow()
        );

        System.out.println(
                "Annual Cash Burn = "
                        + metrics.annualCashBurn()
        );

        System.out.println(
                "Cash Runway Years = "
                        + metrics.cashRunwayYears()
        );

        System.out.println(
                "Cash Runway Quarters = "
                        + metrics.cashRunwayQuarters()
        );

        System.out.println(
                "CapEx Driven Negative FCF = "
                        + metrics.capexDrivenNegativeFcf()
        );


        assertThat(
                metrics.annualCashBurn()
        ).isEqualByComparingTo(
                "4000000000"
        );

        assertThat(
                metrics.cashRunwayYears()
        ).isEqualByComparingTo(
                "3.00"
        );

        assertThat(
                metrics.cashRunwayQuarters()
        ).isEqualByComparingTo(
                "12.00"
        );

        assertThat(
                metrics.operatingCashFlowPositive()
        ).isTrue();

        assertThat(
                metrics.freeCashFlowPositive()
        ).isFalse();

        assertThat(
                metrics.capexDrivenNegativeFcf()
        ).isTrue();
    }
}