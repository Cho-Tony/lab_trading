package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValuationPeerUniverseServiceTest {

    private ValuationPeerSnapshotAssembler snapshotAssembler;
    private PeerUniverseBuilder peerUniverseBuilder;

    private ValuationPeerUniverseService service;


    @BeforeEach
    void setUp() {

        snapshotAssembler =
                mock(
                        ValuationPeerSnapshotAssembler.class
                );


        peerUniverseBuilder =
                mock(
                        PeerUniverseBuilder.class
                );


        service =
                new ValuationPeerUniverseService(

                        snapshotAssembler,
                        peerUniverseBuilder
                );
    }


    @Test
    void buildPeerUniverseForTargetStock() {

        Long targetStockId =
                1L;


        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationPeerSnapshotInput targetInput =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        ValuationPeerSnapshotInput peerInput =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        when(
                targetInput.observationDate()
        )
                .thenReturn(
                        observationDate
                );


        when(
                peerInput.observationDate()
        )
                .thenReturn(
                        observationDate
                );


        List<ValuationPeerSnapshotInput> inputs =
                List.of(
                        targetInput,
                        peerInput
                );


        ValuationPeerSnapshot targetSnapshot =
                mock(
                        ValuationPeerSnapshot.class
                );


        ValuationPeerSnapshot peerSnapshot =
                mock(
                        ValuationPeerSnapshot.class
                );


        when(
                targetSnapshot.stockId()
        )
                .thenReturn(
                        targetStockId
                );


        when(
                peerSnapshot.stockId()
        )
                .thenReturn(
                        2L
                );


        List<ValuationPeerSnapshot> snapshots =
                List.of(
                        targetSnapshot,
                        peerSnapshot
                );


        when(
                snapshotAssembler.assemble(
                        inputs
                )
        )
                .thenReturn(
                        snapshots
                );


        PeerUniverse universe =
                mock(
                        PeerUniverse.class
                );


        when(
                peerUniverseBuilder.build(
                        targetSnapshot,
                        snapshots
                )
        )
                .thenReturn(
                        universe
                );


        PeerUniverse result =
                service.build(

                                targetStockId,
                                observationDate,
                                inputs
                        )

                        .orElseThrow();


        assertThat(
                result
        ).isSameAs(
                universe
        );


        verify(
                snapshotAssembler
        )
                .assemble(
                        inputs
                );


        verify(
                peerUniverseBuilder
        )
                .build(
                        targetSnapshot,
                        snapshots
                );
    }


    @Test
    void excludeInputsFromDifferentObservationDate() {

        Long targetStockId =
                1L;


        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationPeerSnapshotInput currentInput =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        ValuationPeerSnapshotInput futureInput =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        when(
                currentInput.observationDate()
        )
                .thenReturn(
                        observationDate
                );


        when(
                futureInput.observationDate()
        )
                .thenReturn(
                        LocalDate.of(
                                2025,
                                9,
                                30
                        )
                );


        ValuationPeerSnapshot targetSnapshot =
                mock(
                        ValuationPeerSnapshot.class
                );


        when(
                targetSnapshot.stockId()
        )
                .thenReturn(
                        targetStockId
                );


        List<ValuationPeerSnapshotInput> sameDateInputs =
                List.of(
                        currentInput
                );


        List<ValuationPeerSnapshot> snapshots =
                List.of(
                        targetSnapshot
                );


        when(
                snapshotAssembler.assemble(
                        sameDateInputs
                )
        )
                .thenReturn(
                        snapshots
                );


        PeerUniverse universe =
                mock(
                        PeerUniverse.class
                );


        when(
                peerUniverseBuilder.build(
                        targetSnapshot,
                        snapshots
                )
        )
                .thenReturn(
                        universe
                );


        assertThat(
                service.build(

                        targetStockId,
                        observationDate,

                        List.of(
                                currentInput,
                                futureInput
                        )
                )
        ).contains(
                universe
        );


        verify(
                snapshotAssembler
        )
                .assemble(
                        sameDateInputs
                );
    }


    @Test
    void returnEmptyWhenTargetSnapshotIsMissing() {

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


        when(
                input.observationDate()
        )
                .thenReturn(
                        observationDate
                );


        ValuationPeerSnapshot otherSnapshot =
                mock(
                        ValuationPeerSnapshot.class
                );


        when(
                otherSnapshot.stockId()
        )
                .thenReturn(
                        2L
                );


        when(
                snapshotAssembler.assemble(
                        List.of(input)
                )
        )
                .thenReturn(
                        List.of(
                                otherSnapshot
                        )
                );


        assertThat(
                service.build(

                        targetStockId,
                        observationDate,

                        List.of(
                                input
                        )
                )
        ).isEmpty();


        verifyNoInteractions(
                peerUniverseBuilder
        );
    }
}