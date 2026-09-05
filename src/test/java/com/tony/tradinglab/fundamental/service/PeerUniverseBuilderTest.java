package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeerUniverseBuilderTest {

    private final PeerUniverseBuilder builder =
            new PeerUniverseBuilder();


    @Test
    void preferSameIndustryAndSimilarSize() {

        LocalDate asOfDate =
                LocalDate.of(
                        2025,
                        11,
                        5
                );


        ValuationPeerSnapshot target =
                snapshot(
                        1L,
                        "TARGET",
                        "Technology",
                        "Semiconductors",
                        "10000000000",
                        asOfDate
                );


        List<ValuationPeerSnapshot> candidates =
                new ArrayList<>();


        /*
         * 같은 Industry + Size
         * 10개
         */
        for (int i = 0; i < 10; i++) {

            candidates.add(
                    snapshot(
                            (long) i + 10,
                            "SEM" + i,
                            "Technology",
                            "Semiconductors",
                            "12000000000",
                            asOfDate
                    )
            );
        }


        /*
         * 같은 Sector지만 다른 Industry
         */
        candidates.add(
                snapshot(
                        100L,
                        "SOFT",
                        "Technology",
                        "Software",
                        "10000000000",
                        asOfDate
                )
        );


        /*
         * 날짜가 다름
         * PIT 비교 대상에서 제외
         */
        candidates.add(
                snapshot(
                        101L,
                        "FUTURE",
                        "Technology",
                        "Semiconductors",
                        "10000000000",
                        asOfDate.plusDays(1)
                )
        );


        PeerUniverse result =
                builder.build(
                        target,
                        candidates
                );


        System.out.println(
                "Selection Level = "
                        + result.selectionLevel()
        );

        System.out.println(
                "Peer Count = "
                        + result.peers().size()
        );


        assertThat(
                result.selectionLevel()
        ).isEqualTo(
                PeerSelectionLevel
                        .INDUSTRY_AND_SIZE
        );


        assertThat(
                result.peers()
        ).hasSize(10);


        assertThat(
                result.peers()
        ).allMatch(
                peer ->
                        peer.industry()
                                .equals(
                                        "Semiconductors"
                                )
        );
    }


    private ValuationPeerSnapshot snapshot(
            Long stockId,
            String symbol,
            String sector,
            String industry,
            String marketCap,
            LocalDate priceDate
    ) {

        ValuationMetrics valuation =
                new ValuationMetrics(

                        2025,
                        "Q3",

                        priceDate,

                        new BigDecimal("100"),
                        new BigDecimal("100000000"),

                        new BigDecimal(marketCap),

                        new BigDecimal("5000000000"),
                        new BigDecimal("500000000"),
                        new BigDecimal("400000000"),

                        new BigDecimal("20"),
                        new BigDecimal("2"),
                        new BigDecimal("25"),

                        LocalDate.of(
                                2025,
                                10,
                                30
                        )
                );


        return new ValuationPeerSnapshot(

                stockId,
                symbol,

                sector,
                industry,

                valuation,

                null,
                null
        );
    }
}