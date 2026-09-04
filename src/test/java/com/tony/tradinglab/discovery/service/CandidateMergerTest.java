package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.DiscoverySource;
import com.tony.tradinglab.discovery.domain.StockCandidate;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateMergerTest {

    private final CandidateMerger merger =
            new CandidateMerger();

    @Test
    void mergeQuantAndAiSearchCandidates() {

        LocalDateTime now =
                LocalDateTime.of(
                        2026,
                        9,
                        4,
                        20,
                        0
                );


        List<StockCandidate> quantCandidates =
                List.of(

                        new StockCandidate(
                                1L,
                                "AAPL",
                                DiscoverySource.QUANT,
                                now
                        ),

                        new StockCandidate(
                                2L,
                                "CRDO",
                                DiscoverySource.QUANT,
                                now
                        )
                );


        List<StockCandidate> aiCandidates =
                List.of(

                        new StockCandidate(
                                2L,
                                "CRDO",
                                DiscoverySource.AI_SEARCH,
                                now.plusMinutes(10)
                        ),

                        new StockCandidate(
                                3L,
                                "RKLB",
                                DiscoverySource.AI_SEARCH,
                                now.plusMinutes(20)
                        )
                );


        List<StockCandidate> result =
                merger.merge(
                        quantCandidates,
                        aiCandidates
                );


        result.forEach(candidate ->
                System.out.println(
                        candidate.symbol()
                                + " | "
                                + candidate.discoverySource()
                )
        );


        assertThat(result)
                .hasSize(3);


        StockCandidate crdo =
                result.stream()
                        .filter(
                                candidate ->
                                        candidate.symbol()
                                                .equals("CRDO")
                        )
                        .findFirst()
                        .orElseThrow();


        assertThat(
                crdo.discoverySource()
        ).isEqualTo(
                DiscoverySource.BOTH
        );
    }
}