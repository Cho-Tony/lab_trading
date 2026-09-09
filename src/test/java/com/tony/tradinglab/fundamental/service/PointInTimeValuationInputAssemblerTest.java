package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PointInTimeValuationInputAssemblerTest {

    private ValuationPeerSnapshotInputFactory inputFactory;

    private PointInTimeValuationInputAssembler assembler;


    @BeforeEach
    void setUp() {

        inputFactory =
                mock(
                        ValuationPeerSnapshotInputFactory.class
                );


        assembler =
                new PointInTimeValuationInputAssembler(
                        inputFactory
                );
    }


    @Test
    void assemblePointInTimeValuationInput() {

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


        GrowthTrendAnalysis growth =
                mock(
                        GrowthTrendAnalysis.class
                );


        ProfitabilityTrendAnalysis profitability =
                mock(
                        ProfitabilityTrendAnalysis.class
                );


        PointInTimeValuationInputRequest request =
                new PointInTimeValuationInputRequest(

                        stockId,
                        symbol,

                        observationDate,

                        valuation,
                        growth,
                        profitability
                );


        ValuationPeerSnapshotInput expected =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        when(
                inputFactory.create(

                        stockId,
                        symbol,

                        observationDate,

                        valuation,
                        growth,
                        profitability
                )
        )
                .thenReturn(
                        Optional.of(
                                expected
                        )
                );


        ValuationPeerSnapshotInput result =
                assembler.assemble(
                                request
                        )
                        .orElseThrow();


        assertThat(
                result
        ).isSameAs(
                expected
        );


        verify(
                inputFactory
        )
                .create(

                        stockId,
                        symbol,

                        observationDate,

                        valuation,
                        growth,
                        profitability
                );
    }


    @Test
    void returnEmptyWhenFactoryRejectsInput() {

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


        PointInTimeValuationInputRequest request =
                new PointInTimeValuationInputRequest(

                        1L,
                        "CRDO",

                        observationDate,

                        valuation,

                        null,
                        null
                );


        when(
                inputFactory.create(

                        1L,
                        "CRDO",

                        observationDate,

                        valuation,

                        null,
                        null
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        assertThat(
                assembler.assemble(
                        request
                )
        ).isEmpty();
    }


    @Test
    void returnEmptyWhenRequestIsNull() {

        assertThat(
                assembler.assemble(
                        null
                )
        ).isEmpty();


        verifyNoInteractions(
                inputFactory
        );
    }
}