package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.RegressionValuationDataset;
import com.tony.tradinglab.fundamental.domain.RegressionValuationFeature;
import com.tony.tradinglab.fundamental.domain.RegressionValuationMetric;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RegressionValuationDatasetBuilder {

    private final RegressionValuationFeatureFactory featureFactory;

    public RegressionValuationDatasetBuilder(
            RegressionValuationFeatureFactory featureFactory
    ) {
        this.featureFactory = featureFactory;
    }


    public Optional<RegressionValuationDataset> build(
            PeerUniverse universe,
            RegressionValuationMetric metric
    ) {

        if (universe == null
                || universe.target() == null
                || metric == null) {

            return Optional.empty();
        }


        /*
         * 1. Target feature 생성
         *
         * Target 자체에 valuation/growth/margin이 없으면
         * 회귀 평가 자체가 불가능하다.
         */
        Optional<RegressionValuationFeature> targetOptional =
                featureFactory.create(
                        universe.target(),
                        metric
                );


        if (targetOptional.isEmpty()) {
            return Optional.empty();
        }


        RegressionValuationFeature target =
                targetOptional.orElseThrow();


        /*
         * 2. Peer 중 회귀에 사용할 수 있는 회사만 변환
         *
         * 예:
         * P/E 모델인데 적자기업이면
         * featureFactory에서 Optional.empty()
         */
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

                        .flatMap(
                                Optional::stream
                        )

                        .toList();


        return Optional.of(
                new RegressionValuationDataset(
                        metric,
                        target,
                        List.copyOf(peerFeatures)
                )
        );
    }
}