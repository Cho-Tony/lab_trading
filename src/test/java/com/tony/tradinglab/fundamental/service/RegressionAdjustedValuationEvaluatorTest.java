package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegressionAdjustedValuationEvaluatorTest {

    private final RegressionAdjustedValuationEvaluator evaluator =
            new RegressionAdjustedValuationEvaluator();


    @Test
    void evaluateRegressionAdjustedPsValuation() {

        LocalDate date =
                LocalDate.of(
                        2025,
                        11,
                        5
                );


        RegressionValuationFeature target =
                feature(
                        1L,
                        "TARGET",
                        "8",
                        "40",
                        "20",
                        date
                );


        /*
         * 일부러 Growth와 Margin을
         * 완벽한 선형관계로 만들지 않는다.
         */
        List<RegressionValuationFeature> peers =
                List.of(

                        feature(
                                2L,
                                "A",
                                "3.5",
                                "10",
                                "8",
                                date
                        ),

                        feature(
                                3L,
                                "B",
                                "5.0",
                                "20",
                                "9",
                                date
                        ),

                        feature(
                                4L,
                                "C",
                                "6.5",
                                "25",
                                "15",
                                date
                        ),

                        feature(
                                5L,
                                "D",
                                "8.0",
                                "35",
                                "17",
                                date
                        ),

                        feature(
                                6L,
                                "E",
                                "10.0",
                                "45",
                                "21",
                                date
                        ),

                        feature(
                                7L,
                                "F",
                                "13.0",
                                "55",
                                "24",
                                date
                        ),

                        feature(
                                8L,
                                "G",
                                "15.0",
                                "65",
                                "23",
                                date
                        ),

                        feature(
                                9L,
                                "H",
                                "18.0",
                                "70",
                                "30",
                                date
                        )
                );


        RegressionValuationDataset dataset =
                new RegressionValuationDataset(

                        RegressionValuationMetric.PS,

                        target,

                        peers
                );


        RegressionAdjustedValuationAssessment result =
                evaluator.evaluate(
                                dataset
                        )
                        .orElseThrow();


        System.out.println(
                "Actual P/S = "
                        + result.actualMultiple()
        );

        System.out.println(
                "Predicted P/S = "
                        + result.predictedMultiple()
        );

        System.out.println(
                "Residual = "
                        + result.residual()
        );

        System.out.println(
                "Relative Deviation = "
                        + result.relativeDeviationPct()
                        + "%"
        );

        System.out.println(
                "Growth Coefficient = "
                        + result.revenueGrowthCoefficient()
        );

        System.out.println(
                "Margin Coefficient = "
                        + result.operatingMarginCoefficient()
        );

        System.out.println(
                "R² = "
                        + result.rSquared()
        );


        assertThat(
                result.metric()
        ).isEqualTo(
                RegressionValuationMetric.PS
        );


        assertThat(
                result.actualMultiple()
        ).isEqualByComparingTo(
                "8.0000"
        );


        assertThat(
                result.predictedMultiple()
        ).isPositive();


        assertThat(
                result.peerCount()
        ).isEqualTo(
                8
        );


        assertThat(
                result.rSquared()
        ).isNotNull();
    }


    private RegressionValuationFeature feature(
            Long stockId,
            String symbol,
            String valuationMultiple,
            String revenueGrowthPct,
            String operatingMarginPct,
            LocalDate date
    ) {

        return new RegressionValuationFeature(

                stockId,

                symbol,

                date,

                RegressionValuationMetric.PS,

                new BigDecimal(
                        valuationMultiple
                ),

                new BigDecimal(
                        revenueGrowthPct
                ),

                new BigDecimal(
                        operatingMarginPct
                )
        );
    }
}