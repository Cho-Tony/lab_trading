package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValuationPeerSnapshotInputFactoryTest {

    private final ValuationPeerSnapshotInputFactory factory =
            new ValuationPeerSnapshotInputFactory();


    @Test
    void createPointInTimeInput() {

        Long stockId =
                1L;

        String symbol =
                "CRDO";

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationMetrics valuation =
                mock(
                        ValuationMetrics.class
                );


        when(
                valuation.priceDate()
        )
                .thenReturn(
                        observationDate
                );


        when(
                valuation.filedDate()
        )
                .thenReturn(
                        LocalDate.of(
                                2025,
                                5,
                                20
                        )
                );


        GrowthTrendAnalysis growth =
                mock(
                        GrowthTrendAnalysis.class
                );


        ProfitabilityTrendAnalysis profitability =
                mock(
                        ProfitabilityTrendAnalysis.class
                );


        ValuationPeerSnapshotInput result =
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
                symbol
        );


        assertThat(
                result.observationDate()
        ).isEqualTo(
                observationDate
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
    void rejectValuationFromDifferentPriceDate() {

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationMetrics valuation =
                mock(
                        ValuationMetrics.class
                );


        when(
                valuation.priceDate()
        )
                .thenReturn(
                        LocalDate.of(
                                2025,
                                7,
                                1
                        )
                );


        assertThat(
                factory.create(

                        1L,
                        "CRDO",

                        observationDate,

                        valuation,

                        null,
                        null
                )
        ).isEmpty();
    }


    @Test
    void rejectFinancialInformationFiledAfterObservationDate() {

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationMetrics valuation =
                mock(
                        ValuationMetrics.class
                );


        when(
                valuation.priceDate()
        )
                .thenReturn(
                        observationDate
                );


        /*
         * 6/30 백테스트인데
         * 8/1에 공시된 재무정보를 사용했다고 가정.
         */
        when(
                valuation.filedDate()
        )
                .thenReturn(
                        LocalDate.of(
                                2025,
                                8,
                                1
                        )
                );


        assertThat(
                factory.create(

                        1L,
                        "CRDO",

                        observationDate,

                        valuation,

                        null,
                        null
                )
        ).isEmpty();
    }
}