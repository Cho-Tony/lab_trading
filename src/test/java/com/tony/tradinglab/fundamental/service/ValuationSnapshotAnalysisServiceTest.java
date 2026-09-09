package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValuationSnapshotAnalysisServiceTest {

    private ValuationPeerSnapshotAssembler snapshotAssembler;
    private PeerUniverseBuilder peerUniverseBuilder;
    private ValuationComparisonService comparisonService;

    private ValuationSnapshotAnalysisService service;


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


        comparisonService =
                mock(
                        ValuationComparisonService.class
                );


        service =
                new ValuationSnapshotAnalysisService(

                        snapshotAssembler,
                        peerUniverseBuilder,
                        comparisonService
                );
    }


    @Test
    void analyzeAllStocksAtSameObservationDate() {

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationPeerSnapshotInput input1 =
                mock(
                        ValuationPeerSnapshotInput.class
                );

        ValuationPeerSnapshotInput input2 =
                mock(
                        ValuationPeerSnapshotInput.class
                );

        ValuationPeerSnapshotInput input3 =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        when(
                input1.observationDate()
        ).thenReturn(
                observationDate
        );

        when(
                input2.observationDate()
        ).thenReturn(
                observationDate
        );

        when(
                input3.observationDate()
        ).thenReturn(
                observationDate
        );


        List<ValuationPeerSnapshotInput> inputs =
                List.of(
                        input1,
                        input2,
                        input3
                );


        /*
         * input은 3개지만
         * classification 등의 문제로
         * 실제 snapshot은 2개만 생성됐다고 가정.
         */
        ValuationPeerSnapshot snapshot1 =
                mock(
                        ValuationPeerSnapshot.class
                );

        ValuationPeerSnapshot snapshot2 =
                mock(
                        ValuationPeerSnapshot.class
                );


        List<ValuationPeerSnapshot> snapshots =
                List.of(
                        snapshot1,
                        snapshot2
                );


        when(
                snapshotAssembler.assemble(
                        inputs
                )
        )
                .thenReturn(
                        snapshots
                );


        PeerUniverse universe1 =
                mock(
                        PeerUniverse.class
                );

        PeerUniverse universe2 =
                mock(
                        PeerUniverse.class
                );


        when(
                peerUniverseBuilder.build(
                        snapshot1,
                        snapshots
                )
        )
                .thenReturn(
                        universe1
                );


        when(
                peerUniverseBuilder.build(
                        snapshot2,
                        snapshots
                )
        )
                .thenReturn(
                        universe2
                );


        ValuationComparisonResult result1 =
                mock(
                        ValuationComparisonResult.class
                );

        ValuationComparisonResult result2 =
                mock(
                        ValuationComparisonResult.class
                );


        when(
                comparisonService.compare(

                        universe1,
                        RegressionValuationMetric.PS
                )
        )
                .thenReturn(
                        result1
                );


        when(
                comparisonService.compare(

                        universe2,
                        RegressionValuationMetric.PS
                )
        )
                .thenReturn(
                        result2
                );


        ValuationSnapshotAnalysisReport report =
                service.analyze(

                        observationDate,
                        inputs
                );


        assertThat(
                report.observationDate()
        ).isEqualTo(
                observationDate
        );


        assertThat(
                report.requestedStockCount()
        ).isEqualTo(
                3
        );


        assertThat(
                report.analyzableStockCount()
        ).isEqualTo(
                2
        );


        assertThat(
                report.results()
        )
                .containsExactly(
                        result1,
                        result2
                );


        /*
         * 핵심:
         * Snapshot Assembler는 전체 batch에서 1번만 호출.
         */
        verify(
                snapshotAssembler,
                times(1)
        )
                .assemble(
                        inputs
                );


        verify(
                comparisonService
        )
                .compare(
                        universe1,
                        RegressionValuationMetric.PS
                );


        verify(
                comparisonService
        )
                .compare(
                        universe2,
                        RegressionValuationMetric.PS
                );
    }
}