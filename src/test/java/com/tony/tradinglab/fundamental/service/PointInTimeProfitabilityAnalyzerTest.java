package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PointInTimeProfitabilityAnalyzerTest {

    private ProfitabilityCalculator profitabilityCalculator;
    private ProfitabilityTrendCalculator profitabilityTrendCalculator;
    private ProfitabilityTrendAnalyzer profitabilityTrendAnalyzer;

    private PointInTimeProfitabilityAnalyzer analyzer;


    @BeforeEach
    void setUp() {

        profitabilityCalculator =
                mock(
                        ProfitabilityCalculator.class
                );


        profitabilityTrendCalculator =
                mock(
                        ProfitabilityTrendCalculator.class
                );


        profitabilityTrendAnalyzer =
                mock(
                        ProfitabilityTrendAnalyzer.class
                );


        analyzer =
                new PointInTimeProfitabilityAnalyzer(

                        profitabilityCalculator,
                        profitabilityTrendCalculator,
                        profitabilityTrendAnalyzer
                );
    }


    @Test
    void analyzeProfitabilityUsingPointInTimeFinancials() {

        QuarterlyFinancials q3 =
                mock(
                        QuarterlyFinancials.class
                );

        QuarterlyFinancials q4 =
                mock(
                        QuarterlyFinancials.class
                );

        QuarterlyFinancials q1 =
                mock(
                        QuarterlyFinancials.class
                );


        List<QuarterlyFinancials> financials =
                List.of(
                        q3,
                        q4,
                        q1
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

                        financials
                );


        Profitability profitability1 =
                mock(
                        Profitability.class
                );

        Profitability profitability2 =
                mock(
                        Profitability.class
                );

        Profitability profitability3 =
                mock(
                        Profitability.class
                );


        List<Profitability> profitabilities =
                List.of(
                        profitability1,
                        profitability2,
                        profitability3
                );


        ProfitabilityTrend trend1 =
                mock(
                        ProfitabilityTrend.class
                );

        ProfitabilityTrend trend2 =
                mock(
                        ProfitabilityTrend.class
                );


        List<ProfitabilityTrend> trends =
                List.of(
                        trend1,
                        trend2
                );


        ProfitabilityTrendAnalysis expected =
                mock(
                        ProfitabilityTrendAnalysis.class
                );


        when(
                profitabilityCalculator.calculate(
                        financials
                )
        )
                .thenReturn(
                        profitabilities
                );


        when(
                profitabilityTrendCalculator.calculate(
                        profitabilities
                )
        )
                .thenReturn(
                        trends
                );


        when(
                profitabilityTrendAnalyzer.analyze(
                        trends
                )
        )
                .thenReturn(
                        expected
                );


        ProfitabilityTrendAnalysis result =
                analyzer.analyze(
                                context
                        )
                        .orElseThrow();


        assertThat(
                result
        ).isSameAs(
                expected
        );


        verify(
                profitabilityCalculator
        )
                .calculate(
                        financials
                );


        verify(
                profitabilityTrendCalculator
        )
                .calculate(
                        profitabilities
                );


        verify(
                profitabilityTrendAnalyzer
        )
                .analyze(
                        trends
                );
    }


    @Test
    void returnEmptyWhenProfitabilityCannotBeCalculated() {

        List<QuarterlyFinancials> financials =
                List.of(
                        mock(
                                QuarterlyFinancials.class
                        )
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

                        financials
                );


        when(
                profitabilityCalculator.calculate(
                        financials
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


        verifyNoInteractions(
                profitabilityTrendCalculator,
                profitabilityTrendAnalyzer
        );
    }


    @Test
    void returnEmptyWhenTrendCannotBeCalculated() {

        List<QuarterlyFinancials> financials =
                List.of(
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

                        financials
                );


        List<Profitability> profitabilities =
                List.of(
                        mock(
                                Profitability.class
                        )
                );


        when(
                profitabilityCalculator.calculate(
                        financials
                )
        )
                .thenReturn(
                        profitabilities
                );


        when(
                profitabilityTrendCalculator.calculate(
                        profitabilities
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


        verifyNoInteractions(
                profitabilityTrendAnalyzer
        );
    }
}