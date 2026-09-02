package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FinancialHealthMetrics;
import com.tony.tradinglab.fundamental.domain.FinancialHealthTrend;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialHealthTrendCalculatorTest {

    private final FinancialHealthTrendCalculator calculator =
            new FinancialHealthTrendCalculator();

    @Test
    void calculateFinancialHealthTrend() {

        List<FinancialHealthMetrics> metrics =
                List.of(

                        createMetrics(
                                2025,
                                "Q1",
                                "80000000000",
                                "1.50",
                                "0.40"
                        ),

                        createMetrics(
                                2025,
                                "Q2",
                                "70000000000",
                                "1.30",
                                "0.50"
                        ),

                        createMetrics(
                                2025,
                                "Q3",
                                "55000000000",
                                "1.10",
                                "0.70"
                        )
                );

        List<FinancialHealthTrend> result =
                calculator.calculate(metrics);

        result.forEach(trend -> {

            System.out.println(
                    "FY"
                            + trend.fiscalYear()
                            + " "
                            + trend.fiscalQuarter()

                            + " | NetDebtChange="
                            + trend.netDebtChange()

                            + " | DebtEquityChange="
                            + trend.debtToEquityChange()

                            + " | CashDebtChange="
                            + trend.cashToDebtChange()
            );
        });


        assertThat(result)
                .hasSize(2);


        FinancialHealthTrend q2 =
                result.get(0);

        assertThat(
                q2.netDebtChange()
        ).isEqualByComparingTo(
                "-10000000000"
        );

        assertThat(
                q2.debtToEquityChange()
        ).isEqualByComparingTo(
                "-0.20"
        );

        assertThat(
                q2.cashToDebtChange()
        ).isEqualByComparingTo(
                "0.10"
        );


        FinancialHealthTrend q3 =
                result.get(1);

        assertThat(
                q3.netDebtChange()
        ).isEqualByComparingTo(
                "-15000000000"
        );

        assertThat(
                q3.debtToEquityChange()
        ).isEqualByComparingTo(
                "-0.20"
        );

        assertThat(
                q3.cashToDebtChange()
        ).isEqualByComparingTo(
                "0.20"
        );
    }


    private FinancialHealthMetrics createMetrics(
            int fiscalYear,
            String fiscalQuarter,
            String netDebt,
            String debtToEquity,
            String cashToDebt
    ) {

        return new FinancialHealthMetrics(

                fiscalYear,
                fiscalQuarter,

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100000000000"),
                BigDecimal.ZERO,

                new BigDecimal(netDebt),

                new BigDecimal(debtToEquity),
                new BigDecimal(cashToDebt),

                LocalDate.of(
                        2025,
                        1,
                        1
                )
        );
    }
}