package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationSnapshotAnalysisReport;
import com.tony.tradinglab.price.domain.StockPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValuationBacktestDatasetBuilderTest {

    private ValuationBacktestSnapshotService snapshotService;

    private ValuationBacktestDatasetBuilder builder;


    @BeforeEach
    void setUp() {

        snapshotService =
                mock(
                        ValuationBacktestSnapshotService.class
                );


        builder =
                new ValuationBacktestDatasetBuilder(
                        snapshotService
                );
    }


    @Test
    void buildDatasetFromMultipleSnapshots() {

        ValuationSnapshotAnalysisReport valuationReport1 =
                mock(
                        ValuationSnapshotAnalysisReport.class
                );


        ValuationSnapshotAnalysisReport valuationReport2 =
                mock(
                        ValuationSnapshotAnalysisReport.class
                );


        Map<Long, List<StockPrice>> prices1 =
                Map.of();

        Map<Long, List<StockPrice>> prices2 =
                Map.of();


        List<StockPrice> benchmarkPrices =
                List.of();


        ValuationBacktestSnapshotRequest request1 =
                new ValuationBacktestSnapshotRequest(

                        valuationReport1,

                        prices1,

                        "QQQ",

                        benchmarkPrices
                );


        ValuationBacktestSnapshotRequest request2 =
                new ValuationBacktestSnapshotRequest(

                        valuationReport2,

                        prices2,

                        "QQQ",

                        benchmarkPrices
                );


        ValuationBacktestObservation observation1 =
                mock(
                        ValuationBacktestObservation.class
                );


        ValuationBacktestObservation observation2 =
                mock(
                        ValuationBacktestObservation.class
                );


        ValuationBacktestObservation observation3 =
                mock(
                        ValuationBacktestObservation.class
                );


        ValuationBacktestSnapshotReport snapshotReport1 =
                new ValuationBacktestSnapshotReport(

                        LocalDate.of(
                                2024,
                                3,
                                31
                        ),

                        2,

                        2,

                        List.of(
                                observation1,
                                observation2
                        )
                );


        ValuationBacktestSnapshotReport snapshotReport2 =
                new ValuationBacktestSnapshotReport(

                        LocalDate.of(
                                2024,
                                6,
                                30
                        ),

                        1,

                        1,

                        List.of(
                                observation3
                        )
                );


        when(
                snapshotService.create(

                        valuationReport1,

                        prices1,

                        "QQQ",

                        benchmarkPrices
                )
        )
                .thenReturn(
                        snapshotReport1
                );


        when(
                snapshotService.create(

                        valuationReport2,

                        prices2,

                        "QQQ",

                        benchmarkPrices
                )
        )
                .thenReturn(
                        snapshotReport2
                );


        ValuationBacktestDataset result =
                builder.build(

                        List.of(
                                request1,
                                request2
                        )
                );


        assertThat(
                result.requestedSnapshotCount()
        ).isEqualTo(
                2
        );


        assertThat(
                result.processedSnapshotCount()
        ).isEqualTo(
                2
        );


        assertThat(
                result.observationCount()
        ).isEqualTo(
                3
        );


        assertThat(
                result.observations()
        )
                .containsExactly(
                        observation1,
                        observation2,
                        observation3
                );


        verify(
                snapshotService,
                times(2)
        )
                .create(
                        any(),
                        any(),
                        eq("QQQ"),
                        any()
                );
    }


    @Test
    void returnEmptyDatasetWhenRequestsAreEmpty() {

        ValuationBacktestDataset result =
                builder.build(
                        List.of()
                );


        assertThat(
                result.requestedSnapshotCount()
        ).isZero();


        assertThat(
                result.processedSnapshotCount()
        ).isZero();


        assertThat(
                result.observationCount()
        ).isZero();


        assertThat(
                result.observations()
        ).isEmpty();


        verifyNoInteractions(
                snapshotService
        );
    }
}