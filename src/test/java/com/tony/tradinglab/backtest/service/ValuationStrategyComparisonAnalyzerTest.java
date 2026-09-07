package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationStrategyComparisonAnalyzerTest {

    private final ValuationBacktestAnalyzer backtestAnalyzer =
            new ValuationBacktestAnalyzer();

    private final ValuationStrategyComparisonAnalyzer analyzer =
            new ValuationStrategyComparisonAnalyzer(
                    backtestAnalyzer
            );


    @Test
    void analyzeAllMethodsAndHorizons() {

        List<ValuationBacktestObservation> observations =
                new ArrayList<>();


        /*
         * valuation이 낮을수록
         * 미래 excess return이 높아지는
         * 단순 테스트 데이터.
         */
        for (int i = 1; i <= 10; i++) {

            observations.add(
                    observation(
                            (long) i,

                            new BigDecimal(
                                    i * 10
                            ),

                            new BigDecimal(
                                    i - 6
                            ),

                            new BigDecimal(
                                    i * 5 - 30
                            ),

                            new BigDecimal(
                                    55 - i * 5
                            )
                    )
            );
        }


        ValuationStrategyComparisonReport report =
                analyzer.analyze(
                        observations
                );


        /*
         * 방법 3개 × 기간 3개
         */
        assertThat(
                report.summaries()
        ).hasSize(9);


        ValuationMethodBacktestSummary percentile252 =
                report.find(

                                ValuationBacktestMethod
                                        .PERCENTILE_PS,

                                BacktestHorizon
                                        .DAYS_252
                        )
                        .orElseThrow();


        System.out.println(
                "Percentile 252D Spearman = "
                        + percentile252
                        .spearmanCorrelation()
        );

        System.out.println(
                "Percentile 252D Spread = "
                        + percentile252
                        .quintileSpreadPctPoint()
        );


        ValuationMethodBacktestSummary regression252 =
                report.find(

                                ValuationBacktestMethod
                                        .REGRESSION_ADJUSTED_PS,

                                BacktestHorizon
                                        .DAYS_252
                        )
                        .orElseThrow();


        System.out.println(
                "Regression 252D Spearman = "
                        + regression252
                        .spearmanCorrelation()
        );

        System.out.println(
                "Regression 252D Spread = "
                        + regression252
                        .quintileSpreadPctPoint()
        );


        assertThat(
                percentile252.observationCount()
        ).isEqualTo(10);


        assertThat(
                regression252.observationCount()
        ).isEqualTo(10);
    }


    private ValuationBacktestObservation observation(
            Long stockId,

            BigDecimal percentileScore,
            BigDecimal robustZScore,
            BigDecimal regressionDeviation,

            BigDecimal excessReturn
    ) {

        String symbol =
                "TEST" + stockId;


        /*
         * Percentile
         */
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


        /*
         * Robust Z
         */
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


        /*
         * Regression-adjusted
         */
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
                                2025,
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


        /*
         * 테스트 편의를 위해
         * 63 / 126 / 252 모두 같은 excess return 사용.
         */
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
                        2025,
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