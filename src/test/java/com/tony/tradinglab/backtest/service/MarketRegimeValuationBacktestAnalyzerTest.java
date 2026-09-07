package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.*;
import com.tony.tradinglab.market.domain.MarketRegime;
import com.tony.tradinglab.market.domain.MarketRegimeSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarketRegimeValuationBacktestAnalyzerTest {

    private final ValuationBacktestAnalyzer backtestAnalyzer =
            new ValuationBacktestAnalyzer();

    private final MarketRegimeValuationBacktestAnalyzer analyzer =
            new MarketRegimeValuationBacktestAnalyzer(
                    backtestAnalyzer
            );


    @Test
    void analyzeValuationPerformanceByMarketRegime() {

        List<ValuationBacktestObservation> observations =
                new ArrayList<>();


        addRegime(
                observations,
                MarketRegime.BULL,
                2023
        );


        addRegime(
                observations,
                MarketRegime.NORMAL,
                2024
        );


        addRegime(
                observations,
                MarketRegime.BEAR,
                2025
        );


        MarketRegimeValuationBacktestReport report =
                analyzer.analyze(
                        observations
                );


        /*
         * Regime 3
         * × Method 3
         * × Horizon 3
         *
         * = 27
         */
        assertThat(
                report.summaries()
        ).hasSize(27);


        MarketRegimeValuationBacktestSummary bullRegression =
                report.find(

                                MarketRegime.BULL,

                                ValuationBacktestMethod
                                        .REGRESSION_ADJUSTED_PS,

                                BacktestHorizon
                                        .DAYS_252
                        )

                        .orElseThrow();


        System.out.println(
                "BULL Regression 252D"
        );

        System.out.println(
                "Count = "
                        + bullRegression
                        .summary()
                        .observationCount()
        );

        System.out.println(
                "Spearman = "
                        + bullRegression
                        .summary()
                        .spearmanCorrelation()
        );

        System.out.println(
                "Spread = "
                        + bullRegression
                        .summary()
                        .quintileSpreadPctPoint()
        );


        assertThat(
                bullRegression
                        .summary()
                        .observationCount()
        ).isEqualTo(10);


        assertThat(
                bullRegression
                        .summary()
                        .spearmanCorrelation()
        ).isNegative();


        assertThat(
                bullRegression
                        .summary()
                        .quintileSpreadPctPoint()
        ).isPositive();


        MarketRegimeValuationBacktestSummary bearPercentile =
                report.find(

                                MarketRegime.BEAR,

                                ValuationBacktestMethod
                                        .PERCENTILE_PS,

                                BacktestHorizon
                                        .DAYS_252
                        )

                        .orElseThrow();


        assertThat(
                bearPercentile
                        .summary()
                        .observationCount()
        ).isEqualTo(10);
    }


    private void addRegime(
            List<ValuationBacktestObservation> observations,
            MarketRegime regime,
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


            /*
             * valuation이 낮을수록
             * 미래 excess return이 높게 설정
             */
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

                            excessReturn,

                            regime
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

            BigDecimal excessReturn,

            MarketRegime regime
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


        LocalDate date =
                LocalDate.of(
                        year,
                        6,
                        30
                );


        ValuationComparisonResult comparison =
                new ValuationComparisonResult(

                        stockId,

                        symbol,

                        date,

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


        MarketRegimeSnapshot regimeSnapshot =
                new MarketRegimeSnapshot(

                        "QQQ",

                        date,
                        date,

                        new BigDecimal("400"),

                        new BigDecimal("380"),

                        new BigDecimal("10"),

                        regime
                );


        return new ValuationBacktestObservation(

                stockId,

                symbol,

                date,

                comparison,

                null,

                excessReturns,

                regimeSnapshot
        );
    }
}