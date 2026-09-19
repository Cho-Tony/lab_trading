package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.RobustZScoreMetric;
import com.tony.tradinglab.fundamental.domain.RobustZScoreValuationAssessment;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Component
public class RobustZScoreValuationEvaluator {

    private static final int MIN_VALID_PEER_COUNT = 10;

    private static final BigDecimal MODIFIED_Z_SCALE =
            new BigDecimal("0.67448975");

    private static final int SCORE_SCALE = 4;

    private static final int CALCULATION_SCALE = 10;


    public RobustZScoreValuationAssessment evaluate(
            PeerUniverse universe
    ) {

        if (universe == null
                || universe.target() == null
                || universe.target().valuation() == null) {

            throw new IllegalArgumentException(
                    "PeerUniverse and target valuation must not be null."
            );
        }


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


        return new RobustZScoreValuationAssessment(
                universe.target().symbol(),

                calculateMetric(
                        universe.target()
                                .valuation()
                                .peRatio(),
                        peerPeValues
                ),

                calculateMetric(
                        universe.target()
                                .valuation()
                                .psRatio(),
                        peerPsValues
                ),

                calculateMetric(
                        universe.target()
                                .valuation()
                                .priceToFcfRatio(),
                        peerPriceToFcfValues
                )
        );
    }


    private RobustZScoreMetric calculateMetric(
            BigDecimal targetValue,
            List<BigDecimal> peerValues
    ) {

        if (peerValues.size()
                < MIN_VALID_PEER_COUNT) {

            return new RobustZScoreMetric(
                    targetValue,
                    null,
                    null,
                    null,
                    peerValues.size()
            );
        }


        BigDecimal median =
                median(
                        peerValues
                );


        BigDecimal mad =
                calculateMad(
                        peerValues,
                        median
                );


        BigDecimal robustZScore =
                calculateRobustZScore(
                        targetValue,
                        median,
                        mad
                );


        return new RobustZScoreMetric(
                targetValue,
                median,
                mad,
                robustZScore,
                peerValues.size()
        );
    }


    private BigDecimal calculateRobustZScore(
            BigDecimal targetValue,
            BigDecimal median,
            BigDecimal mad
    ) {

        if (targetValue == null
                || targetValue.compareTo(BigDecimal.ZERO) <= 0
                || median == null
                || mad == null
                || mad.compareTo(BigDecimal.ZERO) == 0) {

            return null;
        }


        BigDecimal deviation =
                targetValue.subtract(
                        median
                );


        return MODIFIED_Z_SCALE
                .multiply(
                        deviation
                )
                .divide(
                        mad,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }


    private BigDecimal calculateMad(
            List<BigDecimal> values,
            BigDecimal median
    ) {

        List<BigDecimal> deviations =
                values.stream()
                        .map(
                                value ->
                                        value.subtract(
                                                median
                                        ).abs()
                        )
                        .toList();


        return median(
                deviations
        );
    }


    private BigDecimal median(
            List<BigDecimal> values
    ) {

        if (values.isEmpty()) {

            return null;
        }


        List<BigDecimal> sorted =
                values.stream()
                        .sorted(
                                Comparator.naturalOrder()
                        )
                        .toList();


        int size =
                sorted.size();

        int middle =
                size / 2;


        if (size % 2 == 1) {

            return sorted.get(
                    middle
            );
        }


        BigDecimal left =
                sorted.get(
                        middle - 1
                );

        BigDecimal right =
                sorted.get(
                        middle
                );


        return left.add(
                        right
                )
                .divide(
                        new BigDecimal("2"),
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .stripTrailingZeros();
    }


    private List<BigDecimal> extractValidValues(
            List<ValuationPeerSnapshot> peers,
            Function<ValuationPeerSnapshot, BigDecimal> extractor
    ) {

        if (peers == null
                || peers.isEmpty()) {

            return List.of();
        }


        return peers.stream()
                .filter(
                        peer ->
                                peer != null
                                        && peer.valuation() != null
                )
                .map(extractor)
                .filter(
                        value ->
                                value != null
                                        && value.compareTo(BigDecimal.ZERO) > 0
                )
                .toList();
    }
}