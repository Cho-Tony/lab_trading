package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.domain.StockClassification;
import com.tony.tradinglab.stock.classification.service.StockClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValuationPeerSnapshotFactoryTest {

    private StockClassificationService classificationService;

    private ValuationPeerSnapshotFactory factory;


    @BeforeEach
    void setUp() {

        classificationService =
                mock(
                        StockClassificationService.class
                );


        factory =
                new ValuationPeerSnapshotFactory(
                        classificationService
                );
    }


    @Test
    void createSnapshotUsingPointInTimeClassification() {

        Long stockId =
                1L;

        String symbol =
                "TEST";

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        StockClassification classification =
                new StockClassification(

                        stockId,

                        "Technology",
                        "Semiconductors",

                        LocalDate.of(
                                2024,
                                1,
                                1
                        ),

                        LocalDate.of(
                                2025,
                                7,
                                1
                        ),

                        ClassificationSource.PROVIDER
                );


        when(
                classificationService.findAsOf(
                        stockId,
                        observationDate
                )
        )
                .thenReturn(
                        Optional.of(
                                classification
                        )
                );


        ValuationMetrics valuation =
                mock(
                        ValuationMetrics.class
                );


        GrowthTrendAnalysis growth =
                mock(
                        GrowthTrendAnalysis.class
                );


        ProfitabilityTrendAnalysis profitability =
                mock(
                        ProfitabilityTrendAnalysis.class
                );


        ValuationPeerSnapshot result =
                factory.create(

                                stockId,
                                symbol,
                                observationDate,

                                valuation,
                                growth,
                                profitability
                        )

                        .orElseThrow();


        assertThat(
                result.stockId()
        ).isEqualTo(
                stockId
        );


        assertThat(
                result.symbol()
        ).isEqualTo(
                "TEST"
        );


        assertThat(
                result.sector()
        ).isEqualTo(
                "Technology"
        );


        assertThat(
                result.industry()
        ).isEqualTo(
                "Semiconductors"
        );


        assertThat(
                result.valuation()
        ).isSameAs(
                valuation
        );


        assertThat(
                result.growth()
        ).isSameAs(
                growth
        );


        assertThat(
                result.profitability()
        ).isSameAs(
                profitability
        );
    }


    @Test
    void returnEmptyWhenClassificationIsNotAvailable() {

        Long stockId =
                1L;

        LocalDate observationDate =
                LocalDate.of(
                        2020,
                        1,
                        1
                );


        when(
                classificationService.findAsOf(
                        stockId,
                        observationDate
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        Optional<ValuationPeerSnapshot> result =
                factory.create(

                        stockId,

                        "TEST",

                        observationDate,

                        mock(
                                ValuationMetrics.class
                        ),

                        null,
                        null
                );


        assertThat(
                result
        ).isEmpty();
    }
}