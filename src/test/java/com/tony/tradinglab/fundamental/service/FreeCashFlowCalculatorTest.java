package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyCashFlow;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FreeCashFlowCalculatorTest {

    private final FreeCashFlowCalculator calculator =
            new FreeCashFlowCalculator();

    @Test
    void calculateFreeCashFlow() {

        List<QuarterlyFact> operatingCashFlows =
                List.of(
                        createFact(
                                2025,
                                "Q1",
                                "39895000000"
                        ),
                        createFact(
                                2025,
                                "Q2",
                                "22690000000"
                        )
                );

        List<QuarterlyFact> capitalExpenditures =
                List.of(
                        createFact(
                                2025,
                                "Q1",
                                "2392000000"
                        ),
                        createFact(
                                2025,
                                "Q2",
                                "1996000000"
                        )
                );

        List<QuarterlyCashFlow> result =
                calculator.calculate(
                        operatingCashFlows,
                        capitalExpenditures
                );

        result.forEach(cashFlow ->
                System.out.println(
                        "FY"
                                + cashFlow.fiscalYear()
                                + " "
                                + cashFlow.fiscalQuarter()

                                + " | OCF="
                                + cashFlow.operatingCashFlow()

                                + " | CapEx="
                                + cashFlow.capitalExpenditure()

                                + " | FCF="
                                + cashFlow.freeCashFlow()
                )
        );

        assertThat(result)
                .hasSize(2);

        assertThat(
                result.get(0)
                        .freeCashFlow()
        ).isEqualByComparingTo(
                "37503000000"
        );

        assertThat(
                result.get(1)
                        .freeCashFlow()
        ).isEqualByComparingTo(
                "20694000000"
        );
    }

    private QuarterlyFact createFact(
            int fiscalYear,
            String fiscalQuarter,
            String value
    ) {

        return new QuarterlyFact(
                "TEST",
                new BigDecimal(value),

                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 31),
                LocalDate.of(2025, 5, 1),

                fiscalYear,
                fiscalQuarter,

                false
        );
    }
}