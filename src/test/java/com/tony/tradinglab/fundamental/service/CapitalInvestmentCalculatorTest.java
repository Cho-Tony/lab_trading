package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.CapitalInvestmentMetrics;
import com.tony.tradinglab.fundamental.domain.QuarterlyCashFlow;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CapitalInvestmentCalculatorTest {

    private final CapitalInvestmentCalculator calculator =
            new CapitalInvestmentCalculator();

    @Test
    void calculateCapitalInvestmentMetrics() {

        List<QuarterlyCashFlow> cashFlows =
                List.of(

                        createCashFlow(
                                2024,
                                "Q2",
                                "10000000000",
                                "2000000000"
                        ),

                        createCashFlow(
                                2025,
                                "Q2",
                                "12000000000",
                                "3500000000"
                        )
                );


        List<QuarterlyFinancials> financials =
                List.of(

                        createFinancial(
                                2024,
                                "Q2",
                                "100000000000"
                        ),

                        createFinancial(
                                2025,
                                "Q2",
                                "130000000000"
                        )
                );


        List<CapitalInvestmentMetrics> result =
                calculator.calculate(
                        cashFlows,
                        financials
                );


        assertThat(result)
                .hasSize(2);


        CapitalInvestmentMetrics current =
                result.get(1);


        System.out.println(
                "CapEx = "
                        + current.capitalExpenditure()
        );

        System.out.println(
                "Previous Year CapEx = "
                        + current.previousYearCapitalExpenditure()
        );

        System.out.println(
                "CapEx YoY = "
                        + current.capexGrowthYoYPct()
                        + "%"
        );

        System.out.println(
                "CapEx / Revenue = "
                        + current.capexToRevenuePct()
                        + "%"
        );


        assertThat(
                current.capexGrowthYoYPct()
        ).isEqualByComparingTo(
                "75.00"
        );

        assertThat(
                current.capexToRevenuePct()
        ).isEqualByComparingTo(
                "2.69"
        );
    }


    private QuarterlyCashFlow createCashFlow(
            int fiscalYear,
            String fiscalQuarter,
            String ocf,
            String capex
    ) {

        BigDecimal operatingCashFlow =
                new BigDecimal(ocf);

        BigDecimal capitalExpenditure =
                new BigDecimal(capex);

        return new QuarterlyCashFlow(

                fiscalYear,
                fiscalQuarter,

                operatingCashFlow,
                capitalExpenditure,

                operatingCashFlow.subtract(
                        capitalExpenditure
                ),

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