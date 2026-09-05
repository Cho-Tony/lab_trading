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
                new ArrayList<>();


        peers.add(
                snapshot(
                        2L,
                        "A",
                        "10",
                        "2",
                        "15",
                        date
                )
        );

        peers.add(
                snapshot(
                        3L,
                        "B",
                        "15",
                        "3",
                        "20",
                        date
                )
        );

        peers.add(
                snapshot(
                        4L,
                        "C",
                        "20",
                        "4",
                        "25",
                        date
                )
        );

        peers.add(
                snapshot(
                        5L,
                        "D",
                        "25",
                        "6",
                        "30",
                        date
                )
        );

        peers.add(
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


        PercentileValuationAssessment result =
                evaluator.evaluate(
                        universe
                );


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
         * lower = 10,15     → 2
         * equal = 20        → 1
         *
         * (2 + 0.5) / 5
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
         * lower = 2,3,4
         *
         * 3 / 5
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
         * lower = 15,20
         * equal = 25
         *
         * (2 + 0.5) / 5
         * = 50%
         */
        assertThat(
                result.priceToFcfPercentile()
        ).isEqualByComparingTo(
                "50.00"
        );
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