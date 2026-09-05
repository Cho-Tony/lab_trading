package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerSelectionLevel;
import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PeerUniverseBuilder {

    /*
     * v1 튜닝값
     *
     * Target Market Cap의
     * 0.25x ~ 4.00x 범위를 유사 규모로 본다.
     */
    private static final BigDecimal MIN_MARKET_CAP_MULTIPLE =
            new BigDecimal("0.25");

    private static final BigDecimal MAX_MARKET_CAP_MULTIPLE =
            new BigDecimal("4.00");

    /*
     * 최소 Peer 수
     */
    private static final int MIN_PEER_COUNT = 10;


    public PeerUniverse build(
            ValuationPeerSnapshot target,
            List<ValuationPeerSnapshot> candidates
    ) {

        validateTarget(target);


        /*
         * 1.
         * 동일 Industry
         * +
         * 유사 Market Cap
         */
        List<ValuationPeerSnapshot> industryAndSize =
                candidates.stream()

                        .filter(
                                candidate ->
                                        isEligibleBaseCandidate(
                                                target,
                                                candidate
                                        )
                        )

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

                    PeerSelectionLevel
                            .INDUSTRY_AND_SIZE
            );
        }


        /*
         * 2.
         * Industry 기준으로 부족하면
         *
         * 동일 Sector
         * +
         * 유사 Market Cap
         */
        List<ValuationPeerSnapshot> sectorAndSize =
                candidates.stream()

                        .filter(
                                candidate ->
                                        isEligibleBaseCandidate(
                                                target,
                                                candidate
                                        )
                        )

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

                    PeerSelectionLevel
                            .SECTOR_AND_SIZE
            );
        }


        /*
         * 3.
         * 그래도 부족하면
         * 같은 Sector 전체 사용
         */
        List<ValuationPeerSnapshot> sectorPeers =
                candidates.stream()

                        .filter(
                                candidate ->
                                        isEligibleBaseCandidate(
                                                target,
                                                candidate
                                        )
                        )

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


        /*
         * 자기 자신 제외
         */
        if (target.stockId() != null
                && target.stockId()
                .equals(candidate.stockId())) {

            return false;
        }


        /*
         * 동일 PIT 날짜의 데이터만 비교
         */
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
                || targetMarketCap.compareTo(
                BigDecimal.ZERO
        ) <= 0
                || candidateMarketCap.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

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


        return candidateMarketCap
                .compareTo(minimum) >= 0

                && candidateMarketCap
                .compareTo(maximum) <= 0;
    }


    private boolean equalsIgnoreCase(
            String first,
            String second
    ) {

        if (first == null
                || second == null) {

            return false;
        }

        return first.equalsIgnoreCase(
                second
        );
    }


    private void validateTarget(
            ValuationPeerSnapshot target
    ) {

        if (target == null
                || target.valuation() == null
                || target.valuation()
                .marketCap() == null) {

            throw new IllegalArgumentException(
                    "Target valuation must contain market cap."
            );
        }
    }
}