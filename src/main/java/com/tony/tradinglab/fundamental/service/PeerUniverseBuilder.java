package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerSelectionLevel;
import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PeerUniverseBuilder {

    private static final BigDecimal MIN_MARKET_CAP_MULTIPLE =
            new BigDecimal("0.25");

    private static final BigDecimal MAX_MARKET_CAP_MULTIPLE =
            new BigDecimal("4.00");

    private static final int MIN_PEER_COUNT = 10;


    public PeerUniverse build(
            ValuationPeerSnapshot target,
            List<ValuationPeerSnapshot> candidates
    ) {

        validateTarget(target);


        List<ValuationPeerSnapshot> eligibleCandidates =
                candidates == null
                        ? List.of()
                        : candidates.stream()
                        .filter(
                                candidate ->
                                        isEligibleBaseCandidate(
                                                target,
                                                candidate
                                        )
                        )
                        .toList();


        List<ValuationPeerSnapshot> industryAndSize =
                eligibleCandidates.stream()
                        .filter(
                                candidate ->
                                        sameIndustry(
                                                target,
                                                candidate
                                        )
                        )
                        .filter(
                                candidate ->
                                        similarMarketCap(
                                                target,
                                                candidate
                                        )
                        )
                        .toList();


        if (industryAndSize.size()
                >= MIN_PEER_COUNT) {

            return new PeerUniverse(
                    target,
                    industryAndSize,
                    PeerSelectionLevel.INDUSTRY_AND_SIZE
            );
        }


        List<ValuationPeerSnapshot> sectorAndSize =
                eligibleCandidates.stream()
                        .filter(
                                candidate ->
                                        sameSector(
                                                target,
                                                candidate
                                        )
                        )
                        .filter(
                                candidate ->
                                        similarMarketCap(
                                                target,
                                                candidate
                                        )
                        )
                        .toList();


        if (sectorAndSize.size()
                >= MIN_PEER_COUNT) {

            return new PeerUniverse(
                    target,
                    sectorAndSize,
                    PeerSelectionLevel.SECTOR_AND_SIZE
            );
        }


        List<ValuationPeerSnapshot> sectorPeers =
                eligibleCandidates.stream()
                        .filter(
                                candidate ->
                                        sameSector(
                                                target,
                                                candidate
                                        )
                        )
                        .toList();


        return new PeerUniverse(
                target,
                sectorPeers,
                PeerSelectionLevel.SECTOR_ONLY
        );
    }


    private boolean isEligibleBaseCandidate(
            ValuationPeerSnapshot target,
            ValuationPeerSnapshot candidate
    ) {

        if (candidate == null
                || candidate.valuation() == null) {

            return false;
        }


        if (target.stockId() != null
                && target.stockId()
                .equals(candidate.stockId())) {

            return false;
        }


        if (target.valuation().priceDate() == null
                || candidate.valuation().priceDate() == null) {

            return false;
        }


        return target.valuation()
                .priceDate()
                .equals(
                        candidate.valuation()
                                .priceDate()
                );
    }


    private boolean sameSector(
            ValuationPeerSnapshot target,
            ValuationPeerSnapshot candidate
    ) {

        return equalsIgnoreCase(
                target.sector(),
                candidate.sector()
        );
    }


    private boolean sameIndustry(
            ValuationPeerSnapshot target,
            ValuationPeerSnapshot candidate
    ) {

        return sameSector(
                target,
                candidate
        )
                && equalsIgnoreCase(
                target.industry(),
                candidate.industry()
        );
    }


    private boolean similarMarketCap(
            ValuationPeerSnapshot target,
            ValuationPeerSnapshot candidate
    ) {

        BigDecimal targetMarketCap =
                target.valuation()
                        .marketCap();

        BigDecimal candidateMarketCap =
                candidate.valuation()
                        .marketCap();


        if (targetMarketCap == null
                || candidateMarketCap == null
                || targetMarketCap.compareTo(BigDecimal.ZERO) <= 0
                || candidateMarketCap.compareTo(BigDecimal.ZERO) <= 0) {

            return false;
        }


        BigDecimal minimum =
                targetMarketCap.multiply(
                        MIN_MARKET_CAP_MULTIPLE
                );

        BigDecimal maximum =
                targetMarketCap.multiply(
                        MAX_MARKET_CAP_MULTIPLE
                );


        return candidateMarketCap.compareTo(minimum) >= 0
                && candidateMarketCap.compareTo(maximum) <= 0;
    }


    private boolean equalsIgnoreCase(
            String first,
            String second
    ) {

        return first != null
                && second != null
                && first.equalsIgnoreCase(second);
    }


    private void validateTarget(
            ValuationPeerSnapshot target
    ) {

        if (target == null
                || target.valuation() == null
                || target.valuation().marketCap() == null) {

            throw new IllegalArgumentException(
                    "Target valuation must contain market cap."
            );
        }
    }
}