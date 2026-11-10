package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RobustZScoreValuationEvaluatorTest {

    private final RobustZScoreValuationEvaluator evaluator =
            new RobustZScoreValuationEvaluator();


    @Test
    void calculateRobustZScores() {

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

                        "12",
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


        RobustZScoreValuationAssessment result =
                evaluator.evaluate(
                        universe
                );

        assertThat(result.pe().validPeerCount()).isEqualTo(10);
        assertThat(result.ps().validPeerCount()).isEqualTo(10);
        assertThat(result.priceToFcf().validPeerCount()).isEqualTo(10);


        System.out.println(
                "P/E Median = "
                        + result.pe().median()
        );

        System.out.println(
                "P/E MAD = "
                        + result.pe().mad()
        );

        System.out.println(
                "P/E Robust Z = "
                        + result.pe().robustZScore()
        );


        System.out.println(
                "P/S Robust Z = "
                        + result.ps().robustZScore()
        );


        System.out.println(
                "P/FCF Robust Z = "
                        + result.priceToFcf()
                        .robustZScore()
        );


        /*
         * P/E
         *
         * Median = 20
         * MAD = 5
         *
         * 0.67448975 × (12 - 20) / 5
         *
         * = -1.0792
         */
        assertThat(
                result.pe().median()
        ).isEqualByComparingTo(
                "20"
        );


        assertThat(
                result.pe().mad()
        ).isEqualByComparingTo(
                "5"
        );


        assertThat(
                result.pe().robustZScore()
        ).isEqualByComparingTo(
                "-1.0792"
        );


        /*
         * P/S
         *
         * Peer:
         * 2,2,3,3,4,4,6,6,8,8
         *
         * Median = 4
         * MAD = 2
         *
         * Target = 5
         *
         * ≈ +0.3372
         */
        assertThat(
                result.ps().robustZScore()
        ).isEqualByComparingTo(
                "0.3372"
        );


        /*
         * P/FCF Target = Median
         */
        assertThat(
                result.priceToFcf()
                        .robustZScore()
        ).isEqualByComparingTo(
                "0.0000"
        );
    }


    @Test
    void returnNullWhenMadIsZero() {

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

                        "25",
                        "5",
                        "25",

                        date
                );


        List<ValuationPeerSnapshot> peers =
                new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            peers.add(snapshot(i + 2L, "ZERO_MAD_" + i, "20",
                    Integer.toString(3 + i % 3), "20", date));
        }

        assertThat(peers).extracting(ValuationPeerSnapshot::stockId)
                .doesNotHaveDuplicates().doesNotContain(target.stockId());


        PeerUniverse universe =
                new PeerUniverse(

                        target,
                        peers,

                        PeerSelectionLevel
                                .INDUSTRY_AND_SIZE
                );


        RobustZScoreValuationAssessment result =
                evaluator.evaluate(
                        universe
                );


        /*
         * 모든 Peer P/E가 20
         *
         * Median = 20
         * MAD = 0
         *
         * Robust Z 계산 불가능
         */
        assertThat(result.pe().validPeerCount()).isEqualTo(10);
        assertThat(result.pe().median()).isEqualByComparingTo("20");
        assertThat(
                result.pe().mad()
        ).isEqualByComparingTo(
                "0"
        );


        assertThat(
                result.pe().robustZScore()
        ).isNull();
    }

    @Test
    void rejectMetricsWithNineValidPeersEvenWhenUniverseHasTen() {
        LocalDate date = LocalDate.of(2025, 11, 5);
        ValuationPeerSnapshot target = snapshot(1L, "TARGET", "12", "5", "25", date);
        List<ValuationPeerSnapshot> peers = tenPeers(date);
        peers.set(9, snapshot(11L, "INVALID", "0", "8", "-1", date));

        RobustZScoreValuationAssessment result = evaluator.evaluate(
                new PeerUniverse(target, peers, PeerSelectionLevel.INDUSTRY_AND_SIZE));

        assertThat(peers).hasSize(10);
        assertThat(result.pe().validPeerCount()).isEqualTo(9);
        assertThat(result.pe().median()).isNull();
        assertThat(result.pe().mad()).isNull();
        assertThat(result.pe().robustZScore()).isNull();
        assertThat(result.priceToFcf().validPeerCount()).isEqualTo(9);
        assertThat(result.priceToFcf().median()).isNull();
        assertThat(result.priceToFcf().mad()).isNull();
        assertThat(result.priceToFcf().robustZScore()).isNull();
        assertThat(result.ps().validPeerCount()).isEqualTo(10);
        assertThat(result.ps().robustZScore()).isEqualByComparingTo("0.3372");
    }

    private List<ValuationPeerSnapshot> tenPeers(LocalDate date) {
        // Duplicating each value across distinct securities preserves Median and MAD.
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
