package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.RegressionValuationMetric;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PointInTimeValuationComparisonServiceTest {

    private ValuationPeerUniverseService peerUniverseService;
    private ValuationComparisonService comparisonService;

    private PointInTimeValuationComparisonService service;


    @BeforeEach
    void setUp() {

        peerUniverseService =
                mock(
                        ValuationPeerUniverseService.class
                );


        comparisonService =
                mock(
                        ValuationComparisonService.class
                );


        service =
                new PointInTimeValuationComparisonService(

                        peerUniverseService,
                        comparisonService
                );
    }


    @Test
    void compareUsingPointInTimePeerUniverse() {

        Long targetStockId =
                1L;


        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationPeerSnapshotInput input =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        List<ValuationPeerSnapshotInput> inputs =
                List.of(
                        input
                );


        PeerUniverse universe =
                mock(
                        PeerUniverse.class
                );


        ValuationComparisonResult comparison =
                mock(
                        ValuationComparisonResult.class
                );


        when(
                peerUniverseService.build(

                        targetStockId,
                        observationDate,
                        inputs
                )
        )
                .thenReturn(
                        Optional.of(
                                universe
                        )
                );


        when(
                comparisonService.compare(
                        universe,
                        RegressionValuationMetric.PS
                )
        )
                .thenReturn(
                        comparison
                );


        ValuationComparisonResult result =
                service.compare(

                                targetStockId,
                                observationDate,
                                inputs
                        )

                        .orElseThrow();


        assertThat(
                result
        ).isSameAs(
                comparison
        );


        verify(
                peerUniverseService
        )
                .build(

                        targetStockId,
                        observationDate,
                        inputs
                );


        verify(
                comparisonService
        )
                .compare(
                        universe,
                        RegressionValuationMetric.PS
                );
    }


    @Test
    void returnEmptyWhenPeerUniverseCannotBeBuilt() {

        Long targetStockId =
                1L;


        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationPeerSnapshotInput input =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        List<ValuationPeerSnapshotInput> inputs =
                List.of(
                        input
                );


        when(
                peerUniverseService.build(

                        targetStockId,
                        observationDate,
                        inputs
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        assertThat(
                service.compare(

                        targetStockId,
                        observationDate,
                        inputs
                )
        ).isEmpty();


        /*
         * Peer Universe 자체가 없으므로
         * valuation 계산까지 가면 안 된다.
         */
        verifyNoInteractions(
                comparisonService
        );
    }


    @Test
    void returnEmptyWhenInputIsInvalid() {

        assertThat(
                service.compare(

                        1L,
                        LocalDate.of(
                                2025,
                                6,
                                30
                        ),
                        List.of()
                )
        ).isEmpty();


        verifyNoInteractions(
                peerUniverseService,
                comparisonService
        );
    }
}