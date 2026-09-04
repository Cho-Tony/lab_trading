package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.DiscoverySource;
import com.tony.tradinglab.discovery.domain.StockCandidate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CandidateMerger {

    public List<StockCandidate> merge(
            List<StockCandidate> quantCandidates,
            List<StockCandidate> aiSearchCandidates
    ) {

        Map<String, StockCandidate> merged =
                new LinkedHashMap<>();


        /*
         * 1. Quant 후보 등록
         */
        for (StockCandidate candidate : quantCandidates) {

            merged.put(
                    normalizeSymbol(
                            candidate.symbol()
                    ),
                    candidate
            );
        }


        /*
         * 2. AI Search 후보 등록
         */
        for (StockCandidate aiCandidate : aiSearchCandidates) {

            String symbol =
                    normalizeSymbol(
                            aiCandidate.symbol()
                    );

            StockCandidate existing =
                    merged.get(symbol);

            /*
             * Quant에서도 이미 발견한 종목
             */
            if (existing != null) {

                merged.put(
                        symbol,
                        mergeBoth(
                                existing,
                                aiCandidate
                        )
                );

            } else {

                merged.put(
                        symbol,
                        aiCandidate
                );
            }
        }

        return List.copyOf(
                merged.values()
        );
    }

    private StockCandidate mergeBoth(
            StockCandidate first,
            StockCandidate second
    ) {

        LocalDateTime discoveredAt =
                earliest(
                        first.discoveredAt(),
                        second.discoveredAt()
                );

        Long stockId =
                first.stockId() != null
                        ? first.stockId()
                        : second.stockId();

        return new StockCandidate(

                stockId,

                normalizeSymbol(
                        first.symbol()
                ),

                DiscoverySource.BOTH,

                discoveredAt
        );
    }

    private String normalizeSymbol(
            String symbol
    ) {

        return symbol
                .trim()
                .toUpperCase();
    }

    private LocalDateTime earliest(
            LocalDateTime first,
            LocalDateTime second
    ) {

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return first.isBefore(second)
                ? first
                : second;
    }
}