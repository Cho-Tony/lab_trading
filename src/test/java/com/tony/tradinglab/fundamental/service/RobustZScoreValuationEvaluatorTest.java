package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
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
                List.of(

                        snapshot(
                                2L,
                                "A",
                                "10",
                                "2",
                                "15",
                                date
                        ),

                        snapshot(
                                3L,
                                "B",
                                "15",
                                "3",
                                "20",
                                date
                        ),

                        snapshot(
                                4L,
                                "C",
                                "20",
                                "4",
                                "25",
                                date
                        ),

                        snapshot(
                                5L,
                                "D",
                                "25",
                                "6",
                                "30",
                                date
                        ),

                        snapshot(
                                6L,
                                "E",
                                "30",
                                "8",
                                "35",
                                date
                        )
                );


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
         * 2,3,4,6,8
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
                List.of(

                        snapshot(
                                2L,
                                "A",
                                "20",
                                "3",
                                "20",
                                date
                        ),

                        snapshot(
                                3L,
                                "B",
                                "20",
                                "4",
                                "20",
                                date
                        ),

                        snapshot(
                                4L,
                                "C",
                                "20",
                                "5",
                                "20",
                                date
                        )
                );


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
        assertThat(
                result.pe().mad()
        ).isEqualByComparingTo(
                "0"
        );


        assertThat(
                result.pe().robustZScore()
        ).isNull();
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