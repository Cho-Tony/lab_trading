package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.DebtCapacityMetrics;
import com.tony.tradinglab.fundamental.domain.FinancialHealthMetrics;
import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DebtCapacityCalculatorTest {

    private final DebtCapacityCalculator calculator =
            new DebtCapacityCalculator();

    @Test
    void calculateDebtCapacity() {

        FinancialHealthMetrics health =
                new FinancialHealthMetrics(

                        2025,
                        "Q4",

                        new BigDecimal("35934000000"),
                        new BigDecimal("359241000000"),
                        new BigDecimal("73733000000"),
                        new BigDecimal("98657000000"),

                        new BigDecimal("62723000000"),

                        new BigDecimal("1.34"),
                        new BigDecimal("0.36"),

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

                        new BigDecimal("110543000000"),
                        new BigDecimal("10959000000"),
                        new BigDecimal("99584000000"),

                        LocalDate.of(
                                2025,
                                10,
                                31
                        )
                );


        List<DebtCapacityMetrics> result =
                calculator.calculate(
                        List.of(health),
                        List.of(cashFlow)
                );


        assertThat(result)
                .hasSize(1);


        DebtCapacityMetrics metrics =
                result.get(0);


        System.out.println(
                "Net Debt = "
                        + metrics.netDebt()
        );

        System.out.println(
                "TTM FCF = "
                        + metrics.ttmFreeCashFlow()
        );

        System.out.println(
                "Net Debt / TTM FCF = "
                        + metrics.netDebtToTtmFcfRatio()
                        + "x"
        );

        System.out.println(
                "Debt / TTM FCF = "
                        + metrics.debtToTtmFcfRatio()
                        + "x"
        );

        System.out.println(
                "Net Cash = "
                        + metrics.netCash()
        );


        assertThat(
                metrics.netDebtToTtmFcfRatio()
        ).isEqualByComparingTo(
                "0.63"
        );

        assertThat(
                metrics.debtToTtmFcfRatio()
        ).isEqualByComparingTo(
                "0.99"
        );

        assertThat(
                metrics.netCash()
        ).isFalse();
    }
}