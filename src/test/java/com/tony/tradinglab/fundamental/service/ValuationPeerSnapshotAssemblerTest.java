package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValuationPeerSnapshotAssemblerTest {

    private ValuationPeerSnapshotFactory factory;

    private ValuationPeerSnapshotAssembler assembler;


    @BeforeEach
    void setUp() {

        factory =
                mock(
                        ValuationPeerSnapshotFactory.class
                );


        assembler =
                new ValuationPeerSnapshotAssembler(
                        factory
                );
    }


    @Test
    void assembleAvailablePeerSnapshots() {

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationMetrics valuation1 =
                mock(
                        ValuationMetrics.class
                );

        ValuationMetrics valuation2 =
                mock(
                        ValuationMetrics.class
                );

        ValuationMetrics valuation3 =
                mock(
                        ValuationMetrics.class
                );


        GrowthTrendAnalysis growth1 =
                mock(
                        GrowthTrendAnalysis.class
                );

        GrowthTrendAnalysis growth2 =
                mock(
                        GrowthTrendAnalysis.class
                );

        GrowthTrendAnalysis growth3 =
                mock(
                        GrowthTrendAnalysis.class
                );


        ProfitabilityTrendAnalysis profitability1 =
                mock(
                        ProfitabilityTrendAnalysis.class
                );

        ProfitabilityTrendAnalysis profitability2 =
                mock(
                        ProfitabilityTrendAnalysis.class
                );

        ProfitabilityTrendAnalysis profitability3 =
                mock(
                        ProfitabilityTrendAnalysis.class
                );


        ValuationPeerSnapshotInput input1 =
                new ValuationPeerSnapshotInput(

                        1L,
                        "AAA",

                        observationDate,

                        valuation1,
                        growth1,
                        profitability1
                );


        ValuationPeerSnapshotInput input2 =
                new ValuationPeerSnapshotInput(

                        2L,
                        "BBB",

                        observationDate,

                        valuation2,
                        growth2,
                        profitability2
                );


        ValuationPeerSnapshotInput input3 =
                new ValuationPeerSnapshotInput(

                        3L,
                        "CCC",

                        observationDate,

                        valuation3,
                        growth3,
                        profitability3
                );


        ValuationPeerSnapshot snapshot1 =
                new ValuationPeerSnapshot(

                        1L,
                        "AAA",

                        "Technology",
                        "Semiconductors",

                        valuation1,
                        growth1,
                        profitability1
                );


        ValuationPeerSnapshot snapshot3 =
                new ValuationPeerSnapshot(

                        3L,
                        "CCC",

                        "Technology",
                        "Software",

                        valuation3,
                        growth3,
                        profitability3
                );


        when(
                factory.create(

                        1L,
                        "AAA",
                        observationDate,

                        valuation1,
                        growth1,
                        profitability1
                )
        )
                .thenReturn(
                        Optional.of(
                                snapshot1
                        )
                );


        /*
         * BBB는 해당 observation date의
         * classification이 없다고 가정
         */
        when(
                factory.create(

                        2L,
                        "BBB",
                        observationDate,

                        valuation2,
                        growth2,
                        profitability2
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        when(
                factory.create(

                        3L,
                        "CCC",
                        observationDate,

                        valuation3,
                        growth3,
                        profitability3
                )
        )
                .thenReturn(
                        Optional.of(
                                snapshot3
                        )
                );


        List<ValuationPeerSnapshot> result =
                assembler.assemble(

                        List.of(
                                input1,
                                input2,
                                input3
                        )
                );


        assertThat(
                result
        ).hasSize(2);


        assertThat(
                result
                        .stream()
                        .map(
                                ValuationPeerSnapshot::symbol
                        )
        )
                .containsExactly(
                        "AAA",
                        "CCC"
                );
    }


    @Test
    void returnEmptyWhenInputIsEmpty() {

        assertThat(
                assembler.assemble(
                        List.of()
                )
        ).isEmpty();
    }


    @Test
    void returnEmptyWhenInputIsNull() {

        assertThat(
                assembler.assemble(
                        null
                )
        ).isEmpty();
    }
}