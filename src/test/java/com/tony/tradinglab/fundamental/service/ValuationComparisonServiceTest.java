package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
                twentyFourPeers(date);

        assertThat(peers).extracting(ValuationPeerSnapshot::stockId)
                .doesNotHaveDuplicates().doesNotContain(target.stockId());


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
        ).isEqualTo(24);


        /*
         * Target P/S = 8
         *
         * Peer:
         * Original 8 peers plus 7 below 8 and 9 above 8.
         *
         * lower = 10
         * equal = 1
         *
         * (10 + 0.5) / 24
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

        assertThat(result.percentile().validPePeerCount()).isEqualTo(24);
        assertThat(result.percentile().validPsPeerCount()).isEqualTo(24);
        assertThat(result.percentile().validPriceToFcfPeerCount()).isEqualTo(24);
        assertThat(result.robustZScore().ps().validPeerCount()).isEqualTo(24);
        // Sorted PS positions 12/13 are 9/10; absolute-deviation positions 12/13 are 4.5/5.5.
        assertThat(result.robustZScore().ps().median()).isEqualByComparingTo("9.5");
        assertThat(result.robustZScore().ps().mad()).isEqualByComparingTo("5");
        assertThat(result.robustZScore().ps().robustZScore()).isEqualByComparingTo("-0.2023");


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

        assertThat(result.regressionAdjusted().peerCount()).isEqualTo(24);
        assertThat(result.regressionAdjusted().predictedMultiple()).isPositive();
        assertThat(result.regressionAdjusted().rSquared()).isNotNull();
    }

    @Test
    void rejectPercentileAndRobustStatisticsWithNineValidPeers() {
        LocalDate date = LocalDate.of(2025, 11, 5);
        ValuationComparisonResult result = service.compare(
                new PeerUniverse(target(date), twentyFourPeers(date).subList(0, 9),
                        PeerSelectionLevel.INDUSTRY_AND_SIZE), RegressionValuationMetric.PS);

        assertThat(result.peerCount()).isEqualTo(9);
        assertThat(result.percentile().validPsPeerCount()).isEqualTo(9);
        assertThat(result.percentile().psPercentile()).isNull();
        assertThat(result.robustZScore().ps().validPeerCount()).isEqualTo(9);
        assertThat(result.robustZScore().ps().median()).isNull();
        assertThat(result.robustZScore().ps().mad()).isNull();
        assertThat(result.robustZScore().ps().robustZScore()).isNull();
        assertThat(result.regressionAdjusted()).isNull();
    }

    @Test
    void rejectOnlyRegressionWithNineteenValidPeers() {
        LocalDate date = LocalDate.of(2025, 11, 5);
        ValuationComparisonResult result = service.compare(
                new PeerUniverse(target(date), twentyFourPeers(date).subList(0, 19),
                        PeerSelectionLevel.INDUSTRY_AND_SIZE), RegressionValuationMetric.PS);

        assertThat(result.peerCount()).isEqualTo(19);
        // Ten PS values below 8 and one equal: (10 + 0.5) / 19 * 100 = 55.26.
        assertThat(result.percentile().psPercentile()).isEqualByComparingTo("55.26");
        assertThat(result.robustZScore().ps().robustZScore()).isNotNull();
        assertThat(result.regressionAdjusted()).isNull();
    }

    @Test
    void acceptAllMethodsWithExactlyTwentyValidPeers() {
        LocalDate date = LocalDate.of(2025, 11, 5);
        ValuationComparisonResult result = service.compare(
                new PeerUniverse(target(date), twentyFourPeers(date).subList(0, 20),
                        PeerSelectionLevel.INDUSTRY_AND_SIZE), RegressionValuationMetric.PS);

        assertThat(result.peerCount()).isEqualTo(20);
        // (10 + 0.5) / 20 * 100 = 52.50.
        assertThat(result.percentile().psPercentile()).isEqualByComparingTo("52.50");
        assertThat(result.robustZScore().ps().robustZScore()).isNotNull();
        assertThat(result.regressionAdjusted()).isNotNull();
        assertThat(result.regressionAdjusted().peerCount()).isEqualTo(20);
        assertThat(result.regressionAdjusted().predictedMultiple()).isPositive();
        assertThat(result.regressionAdjusted().rSquared()).isNotNull();
    }

    private ValuationPeerSnapshot target(LocalDate date) {
        return snapshot(1L, "TARGET", "20", "8", "25", "40", "20", date);
    }

    private List<ValuationPeerSnapshot> twentyFourPeers(LocalDate date) {
        String[][] original = {
                {"12", "3.5", "15", "10", "8"}, {"15", "5", "18", "20", "9"},
                {"18", "6.5", "20", "25", "15"}, {"20", "8", "25", "35", "17"},
                {"25", "10", "30", "45", "21"}, {"30", "13", "35", "55", "24"},
                {"35", "15", "40", "65", "23"}, {"40", "18", "45", "70", "30"}
        };
        List<ValuationPeerSnapshot> peers = new ArrayList<>();
        for (int i = 0; i < original.length; i++) {
            String[] row = original[i];
            peers.add(snapshot(i + 2L, "PEER_" + i, row[0], row[1], row[2], row[3], row[4], date));
        }
        String[] extraPs = {"1", "2", "3", "4", "5.5", "6", "7",
                "9", "11", "12", "14", "16", "17", "19", "20", "22"};
        // A 4 x 4 growth/margin grid ensures the regression design matrix has full column rank.
        for (int i = 0; i < extraPs.length; i++) {
            peers.add(snapshot(i + 10L, "EXTRA_" + i,
                    Integer.toString(12 + i), extraPs[i], Integer.toString(15 + i),
                    Integer.toString(12 + 10 * (i / 4)), Integer.toString(5 + 5 * (i % 4)), date));
        }
        return peers;
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
