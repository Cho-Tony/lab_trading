package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyCashFlow;
import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TtmCashFlowCalculatorTest {

    private final TtmCashFlowCalculator calculator =
            new TtmCashFlowCalculator();

    @Test
    void calculateTtmCashFlow() {

        List<QuarterlyCashFlow> cashFlows =
                List.of(

                        createCashFlow(
                                2025,
                                "Q1",
                                "39895000000",
                                "2392000000"
                        ),

                        createCashFlow(
                                2025,
                                "Q2",
                                "22690000000",
                                "1996000000"
                        ),

                        createCashFlow(
                                2025,
                                "Q3",
                                "28858000000",
                                "2151000000"
                        ),

                        createCashFlow(
                                2025,
                                "Q4",
                                "19100000000",
                                "4420000000"
                        )
                );

        List<TtmCashFlow> result =
                calculator.calculate(
                        cashFlows
                );

        assertThat(result)
                .hasSize(1);

        TtmCashFlow ttm =
                result.get(0);

        System.out.println(
                "TTM OCF = "
                        + ttm.operatingCashFlow()
        );

        System.out.println(
                "TTM CapEx = "
                        + ttm.capitalExpenditure()
        );

        System.out.println(
                "TTM FCF = "
                        + ttm.freeCashFlow()
        );


        assertThat(
                ttm.operatingCashFlow()
        ).isEqualByComparingTo(
                "110543000000"
        );


        assertThat(
                ttm.capitalExpenditure()
        ).isEqualByComparingTo(
                "10959000000"
        );


        assertThat(
                ttm.freeCashFlow()
        ).isEqualByComparingTo(
                "99584000000"
        );
    }

    private QuarterlyCashFlow createCashFlow(
            int fiscalYear,
            String fiscalQuarter,
            String operatingCashFlow,
            String capex
    ) {

        BigDecimal ocf =
                new BigDecimal(
                        operatingCashFlow
                );

        BigDecimal capitalExpenditure =
                new BigDecimal(
                        capex
                );

        return new QuarterlyCashFlow(

                fiscalYear,
                fiscalQuarter,

                ocf,
                capitalExpenditure,

                ocf.subtract(
                        capitalExpenditure
                ),

                LocalDate.of(
                        2025,
                        10,
                        31
                )
        );
    }
}