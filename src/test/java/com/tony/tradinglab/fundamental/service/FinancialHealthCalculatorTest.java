package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FinancialHealthMetrics;
import com.tony.tradinglab.fundamental.domain.QuarterlyBalanceSheet;
import com.tony.tradinglab.fundamental.domain.QuarterlyDebt;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialHealthCalculatorTest {

    private final FinancialHealthCalculator calculator =
            new FinancialHealthCalculator();

    @Test
    void calculateFinancialHealth() {

        QuarterlyBalanceSheet balanceSheet =
                new QuarterlyBalanceSheet(

                        2025,
                        "Q4",

                        new BigDecimal("35934000000"),
                        new BigDecimal("359241000000"),
                        new BigDecimal("73733000000"),

                        LocalDate.of(
                                2025,
                                9,
                                27
                        ),

                        LocalDate.of(
                                2025,
                                10,
                                31
                        )
                );


        QuarterlyDebt debt =
                new QuarterlyDebt(

                        2025,
                        "Q4",

                        new BigDecimal("7979000000"),
                        new BigDecimal("12350000000"),
                        new BigDecimal("78328000000"),

                        new BigDecimal("98657000000"),

                        LocalDate.of(
                                2025,
                                10,
                                31
                        )
                );


        List<FinancialHealthMetrics> result =
                calculator.calculate(
                        List.of(balanceSheet),
                        List.of(debt)
                );


        FinancialHealthMetrics metrics =
                result.get(0);


        System.out.println(
                "Net Debt = "
                        + metrics.netDebt()
        );

        System.out.println(
                "Debt / Equity = "
                        + metrics.debtToEquityRatio()
                        + "x"
        );

        System.out.println(
                "Cash / Debt = "
                        + metrics.cashToDebtRatio()
                        + "x"
        );


        assertThat(
                metrics.netDebt()
        ).isEqualByComparingTo(
                "62723000000"
        );

        assertThat(
                metrics.debtToEquityRatio()
        ).isEqualByComparingTo(
                "1.34"
        );

        assertThat(
                metrics.cashToDebtRatio()
        ).isEqualByComparingTo(
                "0.36"
        );
    }
}