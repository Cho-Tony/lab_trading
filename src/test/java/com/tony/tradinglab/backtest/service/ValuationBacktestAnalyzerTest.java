package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationBacktestAnalyzerTest {

    private final ValuationBacktestAnalyzer analyzer =
            new ValuationBacktestAnalyzer();


    @Test
    void cheaperValuationShouldProducePositiveQuintileSpread() {

        List<ValuationBacktestObservation> observations =
                new ArrayList<>();


        for (int i = 1;
             i <= 10;
             i++) {

            /*
             * valuation:
             * 10, 20, ... 100
             *
             * excess return:
             * 50, 45, ... 5
             */
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
                            (long) i,
                            valuationScore,
                            excessReturn
                    )
            );
        }


        ValuationMethodBacktestSummary result =
                analyzer.analyze(

                        observations,

                        ValuationBacktestMethod
                                .PERCENTILE_PS,

                        BacktestHorizon
                                .DAYS_252
                );


        System.out.println(
                "Observation Count = "
                        + result.observationCount()
        );

        System.out.println(
                "Spearman = "
                        + result.spearmanCorrelation()
        );


        result.quintiles()
                .forEach(
                        quintile ->
                                System.out.println(
                                        "Q"
                                                + quintile.quintile()
                                                + " | Avg Excess = "
                                                + quintile.averageExcessReturnPct()
                                )
                );


        System.out.println(
                "Q1 - Q5 Spread = "
                        + result.quintileSpreadPctPoint()
        );


        assertThat(
                result.observationCount()
        ).isEqualTo(10);


        /*
         * valuation은 증가하고
         * return은 정확히 감소하므로
         * Spearman = -1
         */
        assertThat(
                result.spearmanCorrelation()
        ).isEqualByComparingTo(
                "-1.0000"
        );


        assertThat(
                result.cheapestQuintileAverageReturnPct()
        ).isGreaterThan(
                result.mostExpensiveQuintileAverageReturnPct()
        );


        assertThat(
                result.quintileSpreadPctPoint()
        ).isPositive();
    }


    private ValuationBacktestObservation observation(
            Long stockId,
            BigDecimal percentileScore,
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

                        null,

                        null
                );


        ExcessReturnMetrics excessReturns =
                new ExcessReturnMetrics(

                        "QQQ",

                        null,
                        null,
                        null,

                        null,
                        null,
                        null,

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