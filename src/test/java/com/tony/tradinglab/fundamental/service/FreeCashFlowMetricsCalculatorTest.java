package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FreeCashFlowMetrics;
import com.tony.tradinglab.fundamental.domain.QuarterlyCashFlow;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FreeCashFlowMetricsCalculatorTest {

    private final FreeCashFlowMetricsCalculator calculator =
            new FreeCashFlowMetricsCalculator();

    @Test
    void calculateFreeCashFlowMetrics() {

        List<QuarterlyCashFlow> cashFlows =
                List.of(

                        createCashFlow(
                                2024,
                                "Q2",
                                "18000000000",
                                "2000000000",
                                "16000000000"
                        ),

                        createCashFlow(
                                2025,
                                "Q2",
                                "22690000000",
                                "1996000000",
                                "20694000000"
                        )
                );

        List<QuarterlyFinancials> financials =
                List.of(

                        createFinancial(
                                2024,
                                "Q2",
                                "90753000000"
                        ),

                        createFinancial(
                                2025,
                                "Q2",
                                "95359000000"
                        )
                );

        List<FreeCashFlowMetrics> result =
                calculator.calculate(
                        cashFlows,
                        financials
                );

        result.forEach(metrics -> {

            System.out.println(
                    "FY"
                            + metrics.fiscalYear()
                            + " "
                            + metrics.fiscalQuarter()

                            + " | FCF="
                            + metrics.freeCashFlow()

                            + " | FCF Margin="
                            + metrics.freeCashFlowMarginPct()
                            + "%"

                            + " | YoY="
                            + metrics.freeCashFlowGrowthYoYPct()
                            + "%"

                            + " | Turnaround="
                            + metrics.turnaround()
            );
        });

        FreeCashFlowMetrics current =
                result.get(1);

        assertThat(
                current.freeCashFlowMarginPct()
        ).isEqualByComparingTo("21.70");

        assertThat(
                current.freeCashFlowGrowthYoYPct()
        ).isEqualByComparingTo("29.34");

        assertThat(
                current.turnaround()
        ).isFalse();
    }

    private QuarterlyCashFlow createCashFlow(
            int fiscalYear,
            String fiscalQuarter,
            String ocf,
            String capex,
            String fcf
    ) {

        return new QuarterlyCashFlow(

                fiscalYear,
                fiscalQuarter,

                new BigDecimal(ocf),
                new BigDecimal(capex),
                new BigDecimal(fcf),

                LocalDate.of(
                        2025,
                        5,
                        1
                )
        );
    }

    private QuarterlyFinancials createFinancial(
            int fiscalYear,
            String fiscalQuarter,
            String revenue
    ) {

        return new QuarterlyFinancials(

                fiscalYear,
                fiscalQuarter,

                new BigDecimal(revenue),

                BigDecimal.ZERO,
                BigDecimal.ZERO,

                LocalDate.of(
                        2025,
                        5,
                        1
                )
        );
    }
}