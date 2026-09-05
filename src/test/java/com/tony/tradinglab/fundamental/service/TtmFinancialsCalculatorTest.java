package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import com.tony.tradinglab.fundamental.domain.TtmFinancials;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TtmFinancialsCalculatorTest {

    private final TtmFinancialsCalculator calculator =
            new TtmFinancialsCalculator();


    @Test
    void calculateTtmFinancials() {

        List<QuarterlyFinancials> quarterly =
                List.of(

                        financials(
                                2024,
                                "Q4",
                                "100",
                                "20",
                                "15"
                        ),

                        financials(
                                2025,
                                "Q1",
                                "110",
                                "22",
                                "17"
                        ),

                        financials(
                                2025,
                                "Q2",
                                "120",
                                "25",
                                "19"
                        ),

                        financials(
                                2025,
                                "Q3",
                                "130",
                                "30",
                                "24"
                        ),

                        financials(
                                2025,
                                "Q4",
                                "140",
                                "35",
                                "28"
                        )
                );


        List<TtmFinancials> result =
                calculator.calculate(
                        quarterly
                );


        assertThat(result)
                .hasSize(2);


        TtmFinancials latest =
                result.get(1);


        System.out.println(
                "TTM Revenue = "
                        + latest.revenue()
        );

        System.out.println(
                "TTM Operating Income = "
                        + latest.operatingIncome()
        );

        System.out.println(
                "TTM Net Income = "
                        + latest.netIncome()
        );


        assertThat(
                latest.fiscalYear()
        ).isEqualTo(2025);


        assertThat(
                latest.fiscalQuarter()
        ).isEqualTo("Q4");


        assertThat(
                latest.revenue()
        ).isEqualByComparingTo(
                "500"
        );


        assertThat(
                latest.operatingIncome()
        ).isEqualByComparingTo(
                "112"
        );


        assertThat(
                latest.netIncome()
        ).isEqualByComparingTo(
                "88"
        );
    }


    private QuarterlyFinancials financials(
            int fiscalYear,
            String fiscalQuarter,
            String revenue,
            String operatingIncome,
            String netIncome
    ) {

        return new QuarterlyFinancials(

                fiscalYear,
                fiscalQuarter,

                new BigDecimal(revenue),
                new BigDecimal(operatingIncome),
                new BigDecimal(netIncome),

                LocalDate.of(
                        fiscalYear,
                        1,
                        31
                )
        );
    }
}