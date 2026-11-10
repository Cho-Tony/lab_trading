package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PercentileValuationEvaluatorTest {

    private final PercentileValuationEvaluator evaluator =
            new PercentileValuationEvaluator();


    @Test
    void calculateValuationPercentiles() {

        LocalDate date =
                LocalDate.of(
                        2025,
                        11,
                        5
                );


        ValuationPeerSnapshot target =
                snapshot(
                        1L,
                        "TARGET",
                        "20",
                        "5",
                        "25",
                        date
                );


        List<ValuationPeerSnapshot> peers =
                tenPeers(date);

        assertThat(peers).extracting(ValuationPeerSnapshot::stockId)
                .doesNotHaveDuplicates().doesNotContain(target.stockId());


        PeerUniverse universe =
                new PeerUniverse(
                        target,
                        peers,
                        PeerSelectionLevel
                                .INDUSTRY_AND_SIZE
                );


        PercentileValuationAssessment result =
                evaluator.evaluate(
                        universe
                );

        assertThat(result.validPePeerCount()).isEqualTo(10);
        assertThat(result.validPsPeerCount()).isEqualTo(10);
        assertThat(result.validPriceToFcfPeerCount()).isEqualTo(10);


        System.out.println(
                "P/E Percentile = "
                        + result.pePercentile()
        );

        System.out.println(
                "P/S Percentile = "
                        + result.psPercentile()
        );

        System.out.println(
                "P/FCF Percentile = "
                        + result.priceToFcfPercentile()
        );


        /*
         * P/E
         *
         * Target = 20
         *
         * lower = 10,15 (각 2개) → 4
         * equal = 20 (2개)       → 2
         *
         * (4 + 2 / 2) / 10
         * = 50%
         */
        assertThat(
                result.pePercentile()
        ).isEqualByComparingTo(
                "50.00"
        );


        /*
         * P/S
         *
         * Target = 5
         *
         * lower = 2,3,4 (각 2개) → 6
         *
         * 6 / 10
         * = 60%
         */
        assertThat(
                result.psPercentile()
        ).isEqualByComparingTo(
                "60.00"
        );


        /*
         * P/FCF
         *
         * Target = 25
         *
         * lower = 15,20 (각 2개) → 4
         * equal = 25 (2개)       → 2
         *
         * (4 + 2 / 2) / 10
         * = 50%
         */
        assertThat(
                result.priceToFcfPercentile()
        ).isEqualByComparingTo(
                "50.00"
        );
    }

    @Test
    void rejectMetricsWithNineValidPeersEvenWhenUniverseHasTen() {
        LocalDate date = LocalDate.of(2025, 11, 5);
        ValuationPeerSnapshot target = snapshot(1L, "TARGET", "20", "5", "25", date);
        List<ValuationPeerSnapshot> peers = tenPeers(date);
        peers.set(9, snapshot(11L, "INVALID", "0", "8", "-1", date));

        PercentileValuationAssessment result = evaluator.evaluate(
                new PeerUniverse(target, peers, PeerSelectionLevel.INDUSTRY_AND_SIZE));

        assertThat(peers).hasSize(10);
        assertThat(result.validPePeerCount()).isEqualTo(9);
        assertThat(result.validPsPeerCount()).isEqualTo(10);
        assertThat(result.validPriceToFcfPeerCount()).isEqualTo(9);
        assertThat(result.pePercentile()).isNull();
        assertThat(result.priceToFcfPercentile()).isNull();
        assertThat(result.psPercentile()).isEqualByComparingTo("60.00");
    }

    private List<ValuationPeerSnapshot> tenPeers(LocalDate date) {
        // Two distinct securities per original tuple preserve the mid-rank percentiles.
        String[][] multiples = {
                {"10", "2", "15"}, {"15", "3", "20"}, {"20", "4", "25"},
                {"25", "6", "30"}, {"30", "8", "35"}
        };
        List<ValuationPeerSnapshot> peers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String[] values = multiples[i % multiples.length];
            peers.add(snapshot(i + 2L, "PEER_" + i, values[0], values[1], values[2], date));
        }
        return peers;
    }


    private ValuationPeerSnapshot snapshot(
            Long stockId,
            String symbol,
            String pe,
            String ps,
            String priceToFcf,
            LocalDate date
    ) {

        ValuationMetrics valuation =
                new ValuationMetrics(

                        2025,
                        "Q3",

                        date,

                        new BigDecimal("100"),
                        new BigDecimal("100000000"),

                        new BigDecimal(
                                "10000000000"
                        ),

                        new BigDecimal(
                                "5000000000"
                        ),

                        new BigDecimal(
                                "500000000"
                        ),

                        new BigDecimal(
                                "400000000"
                        ),

                        new BigDecimal(pe),
                        new BigDecimal(ps),
                        new BigDecimal(priceToFcf),

                        LocalDate.of(
                                2025,
                                10,
                                30
                        )
                );


        return new ValuationPeerSnapshot(

                stockId,
                symbol,

                "Technology",
                "Semiconductors",

                valuation,

                null,
                null
        );
    }
}
