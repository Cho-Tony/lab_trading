package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PointInTimeQuarterlyFinancialsSelectorTest {

    private final PointInTimeQuarterlyFinancialsSelector selector =
            new PointInTimeQuarterlyFinancialsSelector();


    @Test
    void selectOnlyFinancialsFiledByAsOfDate() {

        QuarterlyFinancials q1 =
                financial(
                        2025,
                        "Q1",
                        "100",
                        LocalDate.of(
                                2025,
                                5,
                                1
                        )
                );


        QuarterlyFinancials q2 =
                financial(
                        2025,
                        "Q2",
                        "130",
                        LocalDate.of(
                                2025,
                                8,
                                5
                        )
                );


        List<QuarterlyFinancials> result =
                selector.select(

                        List.of(
                                q1,
                                q2
                        ),

                        LocalDate.of(
                                2025,
                                7,
                                15
                        )
                );


        /*
         * Q2는 기간 자체는 끝났지만
         * 아직 공시되지 않았으므로 사용할 수 없다.
         */
        assertThat(
                result
        ).containsExactly(
                q1
        );
    }


    @Test
    void useLatestAvailableFilingForSameQuarter() {

        QuarterlyFinancials original =
                financial(
                        2024,
                        "Q2",
                        "100",
                        LocalDate.of(
                                2024,
                                8,
                                1
                        )
                );


        QuarterlyFinancials amended =
                financial(
                        2024,
                        "Q2",
                        "120",
                        LocalDate.of(
                                2024,
                                9,
                                10
                        )
                );


        /*
         * Amendment가 공개되기 전
         */
        List<QuarterlyFinancials> beforeAmendment =
                selector.select(

                        List.of(
                                original,
                                amended
                        ),

                        LocalDate.of(
                                2024,
                                8,
                                20
                        )
                );


        assertThat(
                beforeAmendment
        )
                .containsExactly(
                        original
                );


        /*
         * Amendment가 공개된 후
         */
        List<QuarterlyFinancials> afterAmendment =
                selector.select(

                        List.of(
                                original,
                                amended
                        ),

                        LocalDate.of(
                                2024,
                                10,
                                1
                        )
                );


        assertThat(
                afterAmendment
        )
                .containsExactly(
                        amended
                );


        assertThat(
                afterAmendment.get(0)
                        .revenue()
        )
                .isEqualByComparingTo(
                        "120"
                );
    }


    @Test
    void sortFinancialsByFiscalYearAndQuarter() {

        QuarterlyFinancials q3 =
                financial(
                        2024,
                        "Q3",
                        "130",
                        LocalDate.of(
                                2024,
                                11,
                                1
                        )
                );


        QuarterlyFinancials q1 =
                financial(
                        2024,
                        "Q1",
                        "100",
                        LocalDate.of(
                                2024,
                                5,
                                1
                        )
                );


        QuarterlyFinancials q2 =
                financial(
                        2024,
                        "Q2",
                        "115",
                        LocalDate.of(
                                2024,
                                8,
                                1
                        )
                );


        List<QuarterlyFinancials> result =
                selector.select(

                        List.of(
                                q3,
                                q1,
                                q2
                        ),

                        LocalDate.of(
                                2024,
                                12,
                                31
                        )
                );


        assertThat(
                result
        )
                .containsExactly(
                        q1,
                        q2,
                        q3
                );
    }


    private QuarterlyFinancials financial(
            int fiscalYear,
            String  quarter,
            String revenue,
            LocalDate filedDate
    ) {

        return new QuarterlyFinancials(

                fiscalYear,
                quarter,

                new BigDecimal(
                        revenue
                ),

                new BigDecimal("20"),
                new BigDecimal("10"),

                filedDate
        );
    }
}