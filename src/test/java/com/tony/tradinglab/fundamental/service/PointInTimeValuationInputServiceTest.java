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

class PointInTimeValuationInputServiceTest {

    private PointInTimeValuationAssembler valuationAssembler;

    private ValuationPeerSnapshotInputFactory inputFactory;

    private PointInTimeValuationInputService service;


    @BeforeEach
    void setUp() {

        valuationAssembler =
                mock(
                        PointInTimeValuationAssembler.class
                );


        inputFactory =
                mock(
                        ValuationPeerSnapshotInputFactory.class
                );


        service =
                new PointInTimeValuationInputService(

                        valuationAssembler,
                        inputFactory
                );
    }


    @Test
    void createPointInTimeValuationPeerInput() {

        LocalDate asOfDate =
                LocalDate.of(
                        2025,
                        7,
                        15
                );


        TtmFinancials ttm =
                mock(
                        TtmFinancials.class
                );


        GrowthTrendAnalysis growth =
                mock(
                        GrowthTrendAnalysis.class
                );


        ProfitabilityTrendAnalysis profitability =
                mock(
                        ProfitabilityTrendAnalysis.class
                );


        PointInTimeFundamentalAnalysis fundamental =
                new PointInTimeFundamentalAnalysis(

                        1L,
                        "CRDO",

                        asOfDate,

                        ttm,

                        growth,

                        profitability
                );


        BigDecimal sharePrice =
                new BigDecimal("85.50");


        TtmCashFlow cashFlow =
                mock(
                        TtmCashFlow.class
                );


        SecFactPoint sharesFact =
                mock(
                        SecFactPoint.class
                );


        List<TtmCashFlow> cashFlows =
                List.of(
                        cashFlow
                );


        List<SecFactPoint> sharesFacts =
                List.of(
                        sharesFact
                );


        ValuationMetrics valuation =
                mock(
                        ValuationMetrics.class
                );


        when(
                valuationAssembler.assemble(

                        asOfDate,

                        sharePrice,

                        List.of(ttm),

                        cashFlows,

                        sharesFacts
                )
        )
                .thenReturn(
                        Optional.of(
                                valuation
                        )
                );


        ValuationPeerSnapshotInput expected =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        when(
                inputFactory.create(

                        1L,
                        "CRDO",

                        asOfDate,

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


        PointInTimeValuationBuildRequest request =
                new PointInTimeValuationBuildRequest(

                        fundamental,

                        sharePrice,

                        cashFlows,

                        sharesFacts
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
                valuationAssembler
        )
                .assemble(

                        asOfDate,

                        sharePrice,

                        List.of(ttm),

                        cashFlows,

                        sharesFacts
                );


        verify(
                inputFactory
        )
                .create(

                        1L,
                        "CRDO",

                        asOfDate,

                        valuation,

                        growth,

                        profitability
                );
    }


    @Test
    void returnEmptyWhenValuationCannotBeCalculated() {

        LocalDate asOfDate =
                LocalDate.of(
                        2025,
                        7,
                        15
                );


        TtmFinancials ttm =
                mock(
                        TtmFinancials.class
                );


        PointInTimeFundamentalAnalysis fundamental =
                new PointInTimeFundamentalAnalysis(

                        1L,
                        "CRDO",

                        asOfDate,

                        ttm,

                        null,
                        null
                );


        BigDecimal sharePrice =
                new BigDecimal("85.50");


        List<TtmCashFlow> cashFlows =
                List.of();


        List<SecFactPoint> sharesFacts =
                List.of(
                        mock(
                                SecFactPoint.class
                        )
                );


        when(
                valuationAssembler.assemble(

                        asOfDate,

                        sharePrice,

                        List.of(ttm),

                        cashFlows,

                        sharesFacts
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        PointInTimeValuationBuildRequest request =
                new PointInTimeValuationBuildRequest(

                        fundamental,

                        sharePrice,

                        cashFlows,

                        sharesFacts
                );


        assertThat(
                service.create(
                        request
                )
        ).isEmpty();


        /*
         * valuation 자체를 못 만들었으므로
         * Peer Input 생성까지 가면 안 된다.
         */
        verifyNoInteractions(
                inputFactory
        );
    }


    @Test
    void returnEmptyWhenSharePriceIsInvalid() {

        PointInTimeFundamentalAnalysis fundamental =
                new PointInTimeFundamentalAnalysis(

                        1L,

                        "CRDO",

                        LocalDate.of(
                                2025,
                                7,
                                15
                        ),

                        mock(
                                TtmFinancials.class
                        ),

                        null,
                        null
                );


        PointInTimeValuationBuildRequest request =
                new PointInTimeValuationBuildRequest(

                        fundamental,

                        BigDecimal.ZERO,

                        List.of(),

                        List.of()
                );


        assertThat(
                service.create(
                        request
                )
        ).isEmpty();


        verifyNoInteractions(
                valuationAssembler,
                inputFactory
        );
    }
}