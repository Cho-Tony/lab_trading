package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.PercentileValuationAssessment;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

@Component
public class PercentileValuationEvaluator {

    private static final int SCALE = 2;

    public PercentileValuationAssessment evaluate(
            PeerUniverse universe
    ) {

        if (universe == null
                || universe.target() == null
                || universe.target().valuation() == null) {

            throw new IllegalArgumentException(
                    "PeerUniverse and target valuation must not be null."
            );
        }


        BigDecimal targetPe =
                universe.target()
                        .valuation()
                        .peRatio();

        BigDecimal targetPs =
                universe.target()
                        .valuation()
                        .psRatio();

        BigDecimal targetPriceToFcf =
                universe.target()
                        .valuation()
                        .priceToFcfRatio();


        List<BigDecimal> peerPeValues =
                extractValidValues(
                        universe.peers(),
                        peer ->
                                peer.valuation()
                                        .peRatio()
                );


        List<BigDecimal> peerPsValues =
                extractValidValues(
                        universe.peers(),
                        peer ->
                                peer.valuation()
                                        .psRatio()
                );


        List<BigDecimal> peerPriceToFcfValues =
                extractValidValues(
                        universe.peers(),
                        peer ->
                                peer.valuation()
                                        .priceToFcfRatio()
                );


        return new PercentileValuationAssessment(

                universe.target().symbol(),

                calculatePercentile(
                        targetPe,
                        peerPeValues
                ),

                calculatePercentile(
                        targetPs,
                        peerPsValues
                ),

                calculatePercentile(
                        targetPriceToFcf,
                        peerPriceToFcfValues
                ),

                peerPeValues.size(),
                peerPsValues.size(),
                peerPriceToFcfValues.size()
        );
    }


    private List<BigDecimal> extractValidValues(
            List<ValuationPeerSnapshot> peers,
            Function<ValuationPeerSnapshot, BigDecimal> extractor
    ) {

        return peers.stream()

                .filter(
                        peer ->
                                peer != null
                                        && peer.valuation() != null
                )

                .map(extractor)

                /*
                 * null 및 의미 없는 0/음수 valuation 제거
                 */
                .filter(
                        value ->
                                value != null
                                        && value.compareTo(
                                        BigDecimal.ZERO
                                ) > 0
                )

                .toList();
    }


    private BigDecimal calculatePercentile(
            BigDecimal target,
            List<BigDecimal> peerValues
    ) {

        if (target == null
                || target.compareTo(BigDecimal.ZERO) <= 0
                || peerValues.isEmpty()) {

            return null;
        }


        long lowerCount =
                peerValues.stream()

                        .filter(
                                value ->
                                        value.compareTo(target) < 0
                        )

                        .count();


        long equalCount =
                peerValues.stream()

                        .filter(
                                value ->
                                        value.compareTo(target) == 0
                        )

                        .count();


        /*
         * Mid-rank percentile
         *
         * lower + equal / 2
         */
        BigDecimal rank =
                BigDecimal.valueOf(lowerCount)

                        .add(
                                BigDecimal.valueOf(equalCount)
                                        .divide(
                                                new BigDecimal("2"),
                                                SCALE,
                                                RoundingMode.HALF_UP
                                        )
                        );


        return rank

                .divide(
                        BigDecimal.valueOf(
                                peerValues.size()
                        ),
                        6,
                        RoundingMode.HALF_UP
                )

                .multiply(
                        new BigDecimal("100")
                )

                .setScale(
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }
}