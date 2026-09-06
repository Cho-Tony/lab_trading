package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationComparisonServiceTest {

    private final PercentileValuationEvaluator percentileEvaluator =
            new PercentileValuationEvaluator();

    private final RobustZScoreValuationEvaluator robustZScoreEvaluator =
            new RobustZScoreValuationEvaluator();

    private final RegressionValuationFeatureFactory featureFactory =
            new RegressionValuationFeatureFactory();

    private final RegressionValuationDatasetBuilder regressionDatasetBuilder =
            new RegressionValuationDatasetBuilder(
                    featureFactory
            );

    private final RegressionAdjustedValuationEvaluator regressionEvaluator =
            new RegressionAdjustedValuationEvaluator();


    private final ValuationComparisonService service =
            new ValuationComparisonService(

                    percentileEvaluator,
                    robustZScoreEvaluator,

                    regressionDatasetBuilder,
                    regressionEvaluator
            );


    @Test
    void compareAllValuationMethods() {

        LocalDate date =
                LocalDate.of(
                        2025,
                        11,
                        5
                );


        ValuationPeerSnapshot target =
                snapshot(
                        1L,
                        "TARGET",

                        "20",
                        "8",
                        "25",

                        "40",
                        "20",

                        date
                );


        List<ValuationPeerSnapshot> peers =
                List.of(

                        snapshot(
                                2L,
                                "A",
                                "12",
                                "3.5",
                                "15",
                                "10",
                                "8",
                                date
                        ),

                        snapshot(
                                3L,
                                "B",
                                "15",
                                "5",
                                "18",
                                "20",
                                "9",
                                date
                        ),

                        snapshot(
                                4L,
                                "C",
                                "18",
                                "6.5",
                                "20",
                                "25",
                                "15",
                                date
                        ),

                        snapshot(
                                5L,
                                "D",
                                "20",
                                "8",
                                "25",
                                "35",
                                "17",
                                date
                        ),

                        snapshot(
                                6L,
                                "E",
                                "25",
                                "10",
                                "30",
                                "45",
                                "21",
                                date
                        ),

                        snapshot(
                                7L,
                                "F",
                                "30",
                                "13",
                                "35",
                                "55",
                                "24",
                                date
                        ),

                        snapshot(
                                8L,
                                "G",
                                "35",
                                "15",
                                "40",
                                "65",
                                "23",
                                date
                        ),

                        snapshot(
                                9L,
                                "H",
                                "40",
                                "18",
                                "45",
                                "70",
                                "30",
                                date
                        )
                );


        PeerUniverse universe =
                new PeerUniverse(

                        target,
                        peers,

                        PeerSelectionLevel
                                .INDUSTRY_AND_SIZE
                );


        ValuationComparisonResult result =
                service.compare(

                        universe,

                        RegressionValuationMetric.PS
                );


        System.out.println(
                "Symbol = "
                        + result.symbol()
        );


        System.out.println(
                "P/S Percentile = "
                        + result.percentile()
                        .psPercentile()
        );


        System.out.println(
                "P/S Robust Z = "
                        + result.robustZScore()
                        .ps()
                        .robustZScore()
        );


        if (result.regressionAdjusted() != null) {

            System.out.println(
                    "Predicted P/S = "
                            + result.regressionAdjusted()
                            .predictedMultiple()
            );

            System.out.println(
                    "Regression Deviation = "
                            + result.regressionAdjusted()
                            .relativeDeviationPct()
                            + "%"
            );

            System.out.println(
                    "R² = "
                            + result.regressionAdjusted()
                            .rSquared()
            );
        }


        assertThat(
                result.symbol()
        ).isEqualTo(
                "TARGET"
        );


        assertThat(
                result.peerSelectionLevel()
        ).isEqualTo(
                PeerSelectionLevel
                        .INDUSTRY_AND_SIZE
        );


        assertThat(
                result.peerCount()
        ).isEqualTo(8);


        /*
         * Target P/S = 8
         *
         * Peer:
         * 3.5, 5, 6.5, 8, 10, 13, 15, 18
         *
         * lower = 3
         * equal = 1
         *
         * (3 + 0.5) / 8
         * = 43.75%
         */
        assertThat(
                result.percentile()
                        .psPercentile()
        ).isEqualByComparingTo(
                "43.75"
        );


        assertThat(
                result.robustZScore()
        ).isNotNull();


        /*
         * Regression 모델도 정상 생성되어야 함.
         */
        assertThat(
                result.regressionAdjusted()
        ).isNotNull();


        assertThat(
                result.regressionAdjusted()
                        .metric()
        ).isEqualTo(
                RegressionValuationMetric.PS
        );
    }


    private ValuationPeerSnapshot snapshot(
            Long stockId,
            String symbol,

            String pe,
            String ps,
            String priceToFcf,

            String revenueGrowth,
            String operatingMargin,

            LocalDate date
    ) {

        ValuationMetrics valuation =
                new ValuationMetrics(

                        2025,
                        "Q3",

                        date,

                        new BigDecimal("100"),
                        new BigDecimal("100000000"),

                        new BigDecimal(
                                "10000000000"
                        ),

                        new BigDecimal(
                                "1000000000"
                        ),

                        new BigDecimal(
                                "200000000"
                        ),

                        new BigDecimal(
                                "150000000"
                        ),

                        new BigDecimal(pe),
                        new BigDecimal(ps),
                        new BigDecimal(priceToFcf),

                        LocalDate.of(
                                2025,
                                10,
                                30
                        )
                );


        GrowthTrendAnalysis growth =
                new GrowthTrendAnalysis(

                        GrowthTrend.ACCELERATING,

                        new BigDecimal(
                                revenueGrowth
                        ),

                        new BigDecimal("5"),

                        2,
                        0,
                        3
                );


        ProfitabilityTrendAnalysis profitability =
                new ProfitabilityTrendAnalysis(

                        ProfitabilityDirection.IMPROVING,
                        ProfitabilityDirection.IMPROVING,

                        new BigDecimal(
                                operatingMargin
                        ),

                        new BigDecimal("10"),

                        new BigDecimal("2"),
                        new BigDecimal("1"),

                        3
                );


        return new ValuationPeerSnapshot(

                stockId,
                symbol,

                "Technology",
                "Semiconductors",

                valuation,

                growth,
                profitability
        );
    }
}