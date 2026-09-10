package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PointInTimeFundamentalContext;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import com.tony.tradinglab.fundamental.domain.TtmFinancials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PointInTimeTtmFinancialsAnalyzerTest {

    private TtmFinancialsCalculator calculator;

    private PointInTimeTtmFinancialsAnalyzer analyzer;


    @BeforeEach
    void setUp() {

        calculator =
                mock(
                        TtmFinancialsCalculator.class
                );


        analyzer =
                new PointInTimeTtmFinancialsAnalyzer(
                        calculator
                );
    }


    @Test
    void returnLatestAvailableTtmFinancials() {

        List<QuarterlyFinancials> quarterlyFinancials =
                List.of(
                        mock(QuarterlyFinancials.class),
                        mock(QuarterlyFinancials.class),
                        mock(QuarterlyFinancials.class),
                        mock(QuarterlyFinancials.class),
                        mock(QuarterlyFinancials.class)
                );


        PointInTimeFundamentalContext context =
                new PointInTimeFundamentalContext(

                        1L,

                        "CRDO",

                        LocalDate.of(
                                2025,
                                7,
                                15
                        ),

                        quarterlyFinancials
                );


        TtmFinancials older =
                mock(
                        TtmFinancials.class
                );


        when(
                older.fiscalYear()
        )
                .thenReturn(
                        2024
                );


        when(
                older.fiscalQuarter()
        )
                .thenReturn(
                        "Q4"
                );


        TtmFinancials latest =
                mock(
                        TtmFinancials.class
                );


        when(
                latest.fiscalYear()
        )
                .thenReturn(
                        2025
                );


        when(
                latest.fiscalQuarter()
        )
                .thenReturn(
                        "Q1"
                );


        when(
                calculator.calculate(
                        quarterlyFinancials
                )
        )
                .thenReturn(
                        List.of(
                                older,
                                latest
                        )
                );


        TtmFinancials result =
                analyzer.analyze(
                                context
                        )
                        .orElseThrow();


        assertThat(
                result
        ).isSameAs(
                latest
        );


        verify(
                calculator
        )
                .calculate(
                        quarterlyFinancials
                );
    }


    @Test
    void returnEmptyWhenTtmCannotBeCalculated() {

        List<QuarterlyFinancials> quarterlyFinancials =
                List.of(
                        mock(QuarterlyFinancials.class),
                        mock(QuarterlyFinancials.class)
                );


        PointInTimeFundamentalContext context =
                new PointInTimeFundamentalContext(

                        1L,

                        "CRDO",

                        LocalDate.of(
                                2025,
                                7,
                                15
                        ),

                        quarterlyFinancials
                );


        /*
         * 예:
         * 아직 연속 4개 분기가 없어서
         * TTM을 만들 수 없는 상황.
         */
        when(
                calculator.calculate(
                        quarterlyFinancials
                )
        )
                .thenReturn(
                        List.of()
                );


        assertThat(
                analyzer.analyze(
                        context
                )
        ).isEmpty();
    }


    @Test
    void returnEmptyWhenContextIsNull() {

        assertThat(
                analyzer.analyze(
                        null
                )
        ).isEmpty();


        verifyNoInteractions(
                calculator
        );
    }
}