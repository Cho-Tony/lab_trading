package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.RegressionValuationDataset;
import com.tony.tradinglab.fundamental.domain.RegressionValuationFeature;
import com.tony.tradinglab.fundamental.domain.RegressionValuationMetric;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegressionValuationDatasetBuilder {

    private static final int MIN_REGRESSION_PEER_COUNT = 20;

    private final RegressionValuationFeatureFactory featureFactory;


    public Optional<RegressionValuationDataset> build(
            PeerUniverse universe,
            RegressionValuationMetric metric
    ) {

        if (universe == null
                || universe.target() == null
                || universe.peers() == null
                || metric == null) {

            return Optional.empty();
        }


        Optional<RegressionValuationFeature> target =
                featureFactory.create(
                        universe.target(),
                        metric
                );


        if (target.isEmpty()) {

            return Optional.empty();
        }


        List<RegressionValuationFeature> peerFeatures =
                universe.peers()
                        .stream()
                        .map(
                                peer ->
                                        featureFactory.create(
                                                peer,
                                                metric
                                        )
                        )
                        .flatMap(Optional::stream)
                        .toList();


        if (peerFeatures.size()
                < MIN_REGRESSION_PEER_COUNT) {

            return Optional.empty();
        }


        return Optional.of(
                new RegressionValuationDataset(
                        metric,
                        target.get(),
                        peerFeatures
                )
        );
    }
}