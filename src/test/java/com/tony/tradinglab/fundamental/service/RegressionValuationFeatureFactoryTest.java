package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RegressionValuationFeatureFactoryTest {

    private final RegressionValuationFeatureFactory factory =
            new RegressionValuationFeatureFactory();


    @Test
    void createPsRegressionFeature() {

        ValuationPeerSnapshot snapshot =
                createSnapshot();


        RegressionValuationFeature feature =
                factory.create(
                                snapshot,
                                RegressionValuationMetric.PS
                        )
                        .orElseThrow();


        System.out.println(
                "Symbol = "
                        + feature.symbol()
        );

        System.out.println(
                "P/S = "
                        + feature.valuationMultiple()
        );

        System.out.println(
                "Revenue Growth = "
                        + feature.revenueGrowthPct()
        );

        System.out.println(
                "Operating Margin = "
                        + feature.operatingMarginPct()
        );


        assertThat(
                feature.metric()
        ).isEqualTo(
                RegressionValuationMetric.PS
        );


        assertThat(
                feature.valuationMultiple()
        ).isEqualByComparingTo(
                "10.00"
        );


        assertThat(
                feature.revenueGrowthPct()
        ).isEqualByComparingTo(
                "40"
        );


        assertThat(
                feature.operatingMarginPct()
        ).isEqualByComparingTo(
                "20"
        );
    }


    @Test
    void returnEmptyWhenPeIsUnavailable() {

        ValuationPeerSnapshot original =
                createSnapshot();


        ValuationMetrics valuation =
                new ValuationMetrics(

                        original.valuation()
                                .fiscalYear(),

                        original.valuation()
                                .fiscalQuarter(),

                        original.valuation()
                                .priceDate(),

                        original.valuation()
                                .sharePrice(),

                        original.valuation()
                                .sharesOutstanding(),

                        original.valuation()
                                .marketCap(),

                        original.valuation()
                                .ttmRevenue(),

                        original.valuation()
                                .ttmNetIncome(),

                        original.valuation()
                                .ttmFreeCashFlow(),

                        null,

                        original.valuation()
                                .psRatio(),

                        original.valuation()
                                .priceToFcfRatio(),

                        original.valuation()
                                .filedDate()
                );


        ValuationPeerSnapshot snapshot =
                new ValuationPeerSnapshot(

                        original.stockId(),
                        original.symbol(),

                        original.sector(),
                        original.industry(),

                        valuation,

                        original.growth(),
                        original.profitability()
                );


        Optional<RegressionValuationFeature> result =
                factory.create(
                        snapshot,
                        RegressionValuationMetric.PE
                );


        assertThat(result)
                .isEmpty();
    }


    private ValuationPeerSnapshot createSnapshot() {

        ValuationMetrics valuation =
                new ValuationMetrics(

                        2025,
                        "Q3",

                        LocalDate.of(
                                2025,
                                11,
                                5
                        ),

                        new BigDecimal("100"),
                        new BigDecimal("100000000"),

                        new BigDecimal("10000000000"),

                        new BigDecimal("1000000000"),
                        new BigDecimal("200000000"),
                        new BigDecimal("150000000"),

                        new BigDecimal("50.00"),
                        new BigDecimal("10.00"),
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

                        new BigDecimal("40"),

                        new BigDecimal("5"),

                        3,
                        0,
                        3
                );


        ProfitabilityTrendAnalysis profitability =
                new ProfitabilityTrendAnalysis(

                        ProfitabilityDirection.IMPROVING,
                        ProfitabilityDirection.IMPROVING,

                        new BigDecimal("20"),
                        new BigDecimal("15"),

                        new BigDecimal("2"),
                        new BigDecimal("1"),

                        3
                );


        return new ValuationPeerSnapshot(

                1L,
                "TEST",

                "Technology",
                "Semiconductors",

                valuation,

                growth,
                profitability
        );
    }
}