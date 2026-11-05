package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PointInTimeValuationInputAssemblerTest {

    private ValuationPeerSnapshotInputFactory inputFactory;

    private PointInTimeValuationAssembler valuationAssembler;

    private PointInTimeValuationInputService service;


    @BeforeEach
    void setUp() {

        inputFactory =
                mock(
                        ValuationPeerSnapshotInputFactory.class
                );


        valuationAssembler = mock(PointInTimeValuationAssembler.class);

        service =
                new PointInTimeValuationInputService(
                        valuationAssembler,
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


        PointInTimeValuationBuildRequest request =
                buildRequest(

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
                service.create(
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


        PointInTimeValuationBuildRequest request =
                buildRequest(

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
                service.create(
                        request
                )
        ).isEmpty();

        verify(inputFactory).create(1L, "CRDO", observationDate, valuation, null, null);
    }


    @Test
    void returnEmptyWhenRequestIsNull() {

        assertThat(
                service.create(
                        null
                )
        ).isEmpty();


        verifyNoInteractions(
                valuationAssembler,
                inputFactory
        );
    }

    private PointInTimeValuationBuildRequest buildRequest(
            Long stockId,
            String symbol,
            LocalDate observationDate,
            ValuationMetrics valuation,
            GrowthTrendAnalysis growth,
            ProfitabilityTrendAnalysis profitability
    ) {
        TtmFinancials ttm = mock(TtmFinancials.class);
        PointInTimeFundamentalAnalysis fundamental = new PointInTimeFundamentalAnalysis(
                stockId, symbol, observationDate, ttm, growth, profitability);
        PointInTimeValuationBuildRequest request = new PointInTimeValuationBuildRequest(
                fundamental, BigDecimal.ONE, List.of(), List.of());

        when(valuationAssembler.assemble(observationDate, request.sharePrice(), List.of(ttm),
                request.cashFlows(), request.sharesFacts())).thenReturn(Optional.of(valuation));
        return request;
    }
}
