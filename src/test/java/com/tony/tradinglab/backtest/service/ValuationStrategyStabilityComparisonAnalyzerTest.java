package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationStrategyStabilityComparisonAnalyzerTest {

    private final ValuationBacktestAnalyzer backtestAnalyzer =
            new ValuationBacktestAnalyzer();

    private final ValuationBacktestStabilityAnalyzer stabilityAnalyzer =
            new ValuationBacktestStabilityAnalyzer(
                    backtestAnalyzer
            );

    private final ValuationStrategyStabilityComparisonAnalyzer analyzer =
            new ValuationStrategyStabilityComparisonAnalyzer(
                    stabilityAnalyzer
            );


    @Test
    void analyzeAllMethodsAndHorizonsByYear() {

        List<ValuationBacktestObservation> observations =
                new ArrayList<>();


        addYear(
                observations,
                2023
        );

        addYear(
                observations,
                2024
        );

        addYear(
                observations,
                2025
        );


        ValuationStrategyStabilityComparisonReport report =
                analyzer.analyze(
                        observations
                );


        /*
         * 방법 3개 × horizon 3개
         */
        assertThat(
                report.summaries()
        ).hasSize(9);


        ValuationBacktestStabilitySummary percentile252 =
                report.find(

                                ValuationBacktestMethod
                                        .PERCENTILE_PS,

                                BacktestHorizon
                                        .DAYS_252
                        )
                        .orElseThrow();


        System.out.println(
                "Percentile 252D"
        );

        System.out.println(
                "Years = "
                        + percentile252.analyzedYearCount()
        );

        System.out.println(
                "Negative Correlation Years = "
                        + percentile252
                        .negativeCorrelationYearCount()
        );

        System.out.println(
                "Positive Spread Years = "
                        + percentile252
                        .positiveSpreadYearCount()
        );

        System.out.println(
                "Average Spearman = "
                        + percentile252
                        .averageSpearmanCorrelation()
        );

        System.out.println(
                "Average Spread = "
                        + percentile252
                        .averageQuintileSpreadPctPoint()
        );


        ValuationBacktestStabilitySummary regression252 =
                report.find(

                                ValuationBacktestMethod
                                        .REGRESSION_ADJUSTED_PS,

                                BacktestHorizon
                                        .DAYS_252
                        )
                        .orElseThrow();


        System.out.println(
                "Regression 252D"
        );

        System.out.println(
                "Years = "
                        + regression252.analyzedYearCount()
        );

        System.out.println(
                "Average Spearman = "
                        + regression252
                        .averageSpearmanCorrelation()
        );

        System.out.println(
                "Average Spread = "
                        + regression252
                        .averageQuintileSpreadPctPoint()
        );


        assertThat(
                percentile252.analyzedYearCount()
        ).isEqualTo(3);


        assertThat(
                regression252.analyzedYearCount()
        ).isEqualTo(3);


        assertThat(
                percentile252
                        .averageSpearmanCorrelation()
        ).isNegative();


        assertThat(
                percentile252
                        .averageQuintileSpreadPctPoint()
        ).isPositive();
    }


    private void addYear(
            List<ValuationBacktestObservation> observations,
            int year
    ) {

        for (int i = 1; i <= 10; i++) {

            BigDecimal percentile =
                    new BigDecimal(
                            i * 10
                    );


            BigDecimal robustZ =
                    new BigDecimal(
                            i - 6
                    );


            BigDecimal regressionDeviation =
                    new BigDecimal(
                            i * 5 - 30
                    );


            BigDecimal excessReturn =
                    new BigDecimal(
                            55 - i * 5
                    );


            observations.add(
                    observation(

                            (long) year * 100 + i,

                            year,

                            percentile,
                            robustZ,
                            regressionDeviation,

                            excessReturn
                    )
            );
        }
    }


    private ValuationBacktestObservation observation(
            Long stockId,
            int year,

            BigDecimal percentileScore,
            BigDecimal robustZScore,
            BigDecimal regressionDeviation,

            BigDecimal excessReturn
    ) {

        String symbol =
                "TEST" + stockId;


        PercentileValuationAssessment percentile =
                new PercentileValuationAssessment(

                        symbol,

                        null,

                        percentileScore,

                        null,

                        0,
                        20,
                        0
                );


        RobustZScoreMetric psMetric =
                new RobustZScoreMetric(

                        new BigDecimal("5"),

                        new BigDecimal("5"),

                        new BigDecimal("1"),

                        robustZScore,

                        20
                );


        RobustZScoreValuationAssessment robustZ =
                new RobustZScoreValuationAssessment(

                        symbol,

                        null,

                        psMetric,

                        null
                );


        RegressionAdjustedValuationAssessment regression =
                new RegressionAdjustedValuationAssessment(

                        symbol,

                        RegressionValuationMetric.PS,

                        new BigDecimal("8"),

                        new BigDecimal("10"),

                        new BigDecimal("2.0794"),

                        new BigDecimal("2.3026"),

                        new BigDecimal("-0.2232"),

                        regressionDeviation,

                        new BigDecimal("1"),

                        new BigDecimal("0.02"),

                        new BigDecimal("0.03"),

                        new BigDecimal("0.70"),

                        20
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

                        robustZ,

                        regression
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