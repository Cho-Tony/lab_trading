package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import com.tony.tradinglab.fundamental.domain.TtmFinancials;
import com.tony.tradinglab.fundamental.domain.ValuationMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationMetricsCalculatorTest {

    private final ValuationMetricsCalculator calculator =
            new ValuationMetricsCalculator();


    @Test
    void calculateValuationMetrics() {

        TtmFinancials financials =
                new TtmFinancials(

                        2025,
                        "Q4",

                        new BigDecimal("100000000000"),
                        new BigDecimal("20000000000"),
                        new BigDecimal("10000000000"),

                        LocalDate.of(
                                2025,
                                10,
                                30
                        )
                );


        TtmCashFlow cashFlow =
                new TtmCashFlow(

                        2025,
                        "Q4",

                        new BigDecimal("15000000000"),
                        new BigDecimal("5000000000"),
                        new BigDecimal("10000000000"),

                        LocalDate.of(
                                2025,
                                10,
                                30
                        )
                );


        ValuationMetrics result =
                calculator.calculate(

                        financials,
                        cashFlow,

                        LocalDate.of(
                                2025,
                                10,
                                31
                        ),

                        new BigDecimal("50"),

                        new BigDecimal("4000000000")
                );


        System.out.println(
                "Market Cap = "
                        + result.marketCap()
        );

        System.out.println(
                "P/E = "
                        + result.peRatio()
        );

        System.out.println(
                "P/S = "
                        + result.psRatio()
        );

        System.out.println(
                "P/FCF = "
                        + result.priceToFcfRatio()
        );


        assertThat(
                result.marketCap()
        ).isEqualByComparingTo(
                "200000000000"
        );


        assertThat(
                result.peRatio()
        ).isEqualByComparingTo(
                "20.00"
        );


        assertThat(
                result.psRatio()
        ).isEqualByComparingTo(
                "2.00"
        );


        assertThat(
                result.priceToFcfRatio()
        ).isEqualByComparingTo(
                "20.00"
        );
    }


    @Test
    void returnNullPeAndPriceToFcfWhenCompanyIsUnprofitable() {

        TtmFinancials financials =
                new TtmFinancials(

                        2025,
                        "Q4",

                        new BigDecimal("10000000000"),
                        new BigDecimal("-1000000000"),
                        new BigDecimal("-2000000000"),

                        LocalDate.of(
                                2025,
                                10,
                                30
                        )
                );


        TtmCashFlow cashFlow =
                new TtmCashFlow(

                        2025,
                        "Q4",

                        new BigDecimal("500000000"),
                        new BigDecimal("1500000000"),
                        new BigDecimal("-1000000000"),

                        LocalDate.of(
                                2025,
                                10,
                                30
                        )
                );


        ValuationMetrics result =
                calculator.calculate(

                        financials,
                        cashFlow,

                        LocalDate.of(
                                2025,
                                10,
                                31
                        ),

                        new BigDecimal("20"),

                        new BigDecimal("500000000")
                );


        assertThat(
                result.peRatio()
        ).isNull();


        assertThat(
                result.priceToFcfRatio()
        ).isNull();


        assertThat(
                result.psRatio()
        ).isNotNull();
    }
}