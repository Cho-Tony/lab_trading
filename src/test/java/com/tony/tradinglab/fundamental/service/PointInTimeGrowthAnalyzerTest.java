package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PointInTimeGrowthAnalyzerTest {

    private RevenueGrowthCalculator revenueGrowthCalculator;
    private GrowthAccelerationCalculator growthAccelerationCalculator;
    private GrowthTrendAnalyzer growthTrendAnalyzer;

    private PointInTimeGrowthAnalyzer analyzer;


    @BeforeEach
    void setUp() {

        revenueGrowthCalculator =
                mock(
                        RevenueGrowthCalculator.class
                );


        growthAccelerationCalculator =
                mock(
                        GrowthAccelerationCalculator.class
                );


        growthTrendAnalyzer =
                mock(
                        GrowthTrendAnalyzer.class
                );


        analyzer =
                new PointInTimeGrowthAnalyzer(

                        revenueGrowthCalculator,
                        growthAccelerationCalculator,
                        growthTrendAnalyzer
                );
    }


    @Test
    void analyzeUsingOnlyRevenueFactsAvailableAtAsOfDate() {

        LocalDate asOfDate =
                LocalDate.of(
                        2025,
                        7,
                        15
                );


        QuarterlyFact q4 =
                revenueFact(
                        2024,
                        "Q4",
                        LocalDate.of(
                                2025,
                                2,
                                1
                        )
                );


        QuarterlyFact q1 =
                revenueFact(
                        2025,
                        "Q1",
                        LocalDate.of(
                                2025,
                                5,
                                1
                        )
                );


        /*
         * 미래 공시
         */
        QuarterlyFact q2 =
                revenueFact(
                        2025,
                        "Q2",
                        LocalDate.of(
                                2025,
                                8,
                                5
                        )
                );


        RevenueGrowth growth =
                mock(
                        RevenueGrowth.class
                );


        GrowthAcceleration acceleration =
                mock(
                        GrowthAcceleration.class
                );


        GrowthTrendAnalysis expected =
                mock(
                        GrowthTrendAnalysis.class
                );


        /*
         * Q2는 미래 공시이므로
         * Calculator에 전달되면 안 된다.
         */
        when(
                revenueGrowthCalculator.calculate(
                        List.of(
                                q4,
                                q1
                        )
                )
        )
                .thenReturn(
                        List.of(
                                growth
                        )
                );


        when(
                growthAccelerationCalculator.calculate(
                        List.of(
                                growth
                        )
                )
        )
                .thenReturn(
                        List.of(
                                acceleration
                        )
                );


        when(
                growthTrendAnalyzer.analyze(
                        List.of(
                                acceleration
                        )
                )
        )
                .thenReturn(
                        expected
                );


        GrowthTrendAnalysis result =
                analyzer.analyze(

                                List.of(
                                        q4,
                                        q1,
                                        q2
                                ),

                                asOfDate
                        )

                        .orElseThrow();


        assertThat(
                result
        ).isSameAs(
                expected
        );


        verify(
                revenueGrowthCalculator
        )
                .calculate(
                        List.of(
                                q4,
                                q1
                        )
                );


        verify(
                growthAccelerationCalculator
        )
                .calculate(
                        List.of(
                                growth
                        )
                );


        verify(
                growthTrendAnalyzer
        )
                .analyze(
                        List.of(
                                acceleration
                        )
                );
    }


    private QuarterlyFact revenueFact(
            int fiscalYear,
            String fiscalQuarter,
            LocalDate filedDate
    ) {

        QuarterlyFact fact =
                mock(
                        QuarterlyFact.class
                );


        when(
                fact.fiscalYear()
        )
                .thenReturn(
                        fiscalYear
                );


        when(
                fact.fiscalQuarter()
        )
                .thenReturn(
                        fiscalQuarter
                );


        when(
                fact.filedDate()
        )
                .thenReturn(
                        filedDate
                );


        return fact;
    }
}