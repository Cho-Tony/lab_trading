package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationBacktestStabilityAnalyzerTest {

    private final ValuationBacktestAnalyzer backtestAnalyzer =
            new ValuationBacktestAnalyzer();

    private final ValuationBacktestStabilityAnalyzer analyzer =
            new ValuationBacktestStabilityAnalyzer(
                    backtestAnalyzer
            );


    @Test
    void analyzeYearByYearStability() {

        List<ValuationBacktestObservation> observations =
                new ArrayList<>();


        /*
         * 2023
         * 싸수록 수익률 높음
         */
        addYear(
                observations,
                2023
        );


        /*
         * 2024도 동일한 관계
         */
        addYear(
                observations,
                2024
        );


        /*
         * 2025도 동일
         */
        addYear(
                observations,
                2025
        );


        ValuationBacktestStabilitySummary result =
                analyzer.analyze(

                        observations,

                        ValuationBacktestMethod
                                .PERCENTILE_PS,

                        BacktestHorizon
                                .DAYS_252
                );


        System.out.println(
                "Analyzed Years = "
                        + result.analyzedYearCount()
        );


        result.yearlySummaries()
                .forEach(
                        yearly -> {

                            System.out.println(
                                    yearly.year()
                                            + " | Spearman="
                                            + yearly.summary()
                                            .spearmanCorrelation()
                                            + " | Spread="
                                            + yearly.summary()
                                            .quintileSpreadPctPoint()
                            );
                        }
                );


        System.out.println(
                "Negative Correlation Years = "
                        + result.negativeCorrelationYearCount()
        );

        System.out.println(
                "Positive Spread Years = "
                        + result.positiveSpreadYearCount()
        );

        System.out.println(
                "Average Spearman = "
                        + result.averageSpearmanCorrelation()
        );

        System.out.println(
                "Average Spread = "
                        + result.averageQuintileSpreadPctPoint()
        );

        System.out.println(
                "Worst Spread = "
                        + result.worstQuintileSpreadPctPoint()
        );


        assertThat(
                result.analyzedYearCount()
        ).isEqualTo(3);


        assertThat(
                result.negativeCorrelationYearCount()
        ).isEqualTo(3);


        assertThat(
                result.positiveSpreadYearCount()
        ).isEqualTo(3);


        assertThat(
                result.averageSpearmanCorrelation()
        ).isNegative();


        assertThat(
                result.averageQuintileSpreadPctPoint()
        ).isPositive();


        assertThat(
                result.worstQuintileSpreadPctPoint()
        ).isPositive();
    }


    private void addYear(
            List<ValuationBacktestObservation> observations,
            int year
    ) {

        for (int i = 1;
             i <= 10;
             i++) {

            BigDecimal valuationScore =
                    new BigDecimal(
                            i * 10
                    );


            BigDecimal excessReturn =
                    new BigDecimal(
                            55 - i * 5
                    );


            observations.add(
                    observation(

                            (long) year * 100 + i,

                            year,

                            valuationScore,

                            excessReturn
                    )
            );
        }
    }


    private ValuationBacktestObservation observation(
            Long stockId,
            int year,
            BigDecimal valuationScore,
            BigDecimal excessReturn
    ) {

        String symbol =
                "TEST" + stockId;


        PercentileValuationAssessment percentile =
                new PercentileValuationAssessment(

                        symbol,

                        null,

                        valuationScore,

                        null,

                        0,
                        20,
                        0
                );


        ValuationComparisonResult comparison =
                new ValuationComparisonResult(

                        stockId,
                        symbol,

                        LocalDate.of(
                                year,
                                1,
                                2
                        ),

                        PeerSelectionLevel
                                .INDUSTRY_AND_SIZE,

                        20,

                        percentile,

                        null,
                        null
                );


        ExcessReturnMetrics excessReturns =
                new ExcessReturnMetrics(

                        "QQQ",

                        excessReturn,
                        BigDecimal.ZERO,
                        excessReturn,

                        excessReturn,
                        BigDecimal.ZERO,
                        excessReturn,

                        excessReturn,
                        BigDecimal.ZERO,
                        excessReturn
                );


        return new ValuationBacktestObservation(

                stockId,
                symbol,

                LocalDate.of(
                        year,
                        1,
                        2
                ),

                comparison,

                null,

                excessReturns,

                null
        );
    }
}