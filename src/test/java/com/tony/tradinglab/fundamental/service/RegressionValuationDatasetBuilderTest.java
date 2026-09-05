package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegressionValuationDatasetBuilderTest {

    private final RegressionValuationFeatureFactory featureFactory =
            new RegressionValuationFeatureFactory();

    private final RegressionValuationDatasetBuilder builder =
            new RegressionValuationDatasetBuilder(
                    featureFactory
            );


    @Test
    void buildPsRegressionDataset() {

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
                        "10",
                        "40",
                        "20",
                        date
                );


        List<ValuationPeerSnapshot> peers =
                List.of(

                        snapshot(
                                2L,
                                "A",
                                "4",
                                "10",
                                "5",
                                date
                        ),

                        snapshot(
                                3L,
                                "B",
                                "6",
                                "20",
                                "10",
                                date
                        ),

                        snapshot(
                                4L,
                                "C",
                                "8",
                                "30",
                                "15",
                                date
                        ),

                        snapshot(
                                5L,
                                "D",
                                "12",
                                "50",
                                "25",
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


        RegressionValuationDataset dataset =
                builder.build(
                                universe,
                                RegressionValuationMetric.PS
                        )
                        .orElseThrow();


        System.out.println(
                "Target = "
                        + dataset.target().symbol()
        );

        System.out.println(
                "Target P/S = "
                        + dataset.target()
                        .valuationMultiple()
        );

        System.out.println(
                "Peer Count = "
                        + dataset.peerCount()
        );


        dataset.peerFeatures()
                .forEach(
                        feature ->
                                System.out.println(
                                        feature.symbol()
                                                + " | PS="
                                                + feature.valuationMultiple()
                                                + " | Growth="
                                                + feature.revenueGrowthPct()
                                                + " | Margin="
                                                + feature.operatingMarginPct()
                                )
                );


        assertThat(
                dataset.metric()
        ).isEqualTo(
                RegressionValuationMetric.PS
        );


        assertThat(
                dataset.target().symbol()
        ).isEqualTo(
                "TARGET"
        );


        assertThat(
                dataset.peerFeatures()
        ).hasSize(4);


        assertThat(
                dataset.target()
                        .revenueGrowthPct()
        ).isEqualByComparingTo(
                "40"
        );
    }


    private ValuationPeerSnapshot snapshot(
            Long stockId,
            String symbol,
            String ps,
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

                        new BigDecimal("10000000000"),

                        new BigDecimal("1000000000"),
                        new BigDecimal("200000000"),
                        new BigDecimal("150000000"),

                        new BigDecimal("50"),

                        new BigDecimal(ps),

                        new BigDecimal("66.67"),

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