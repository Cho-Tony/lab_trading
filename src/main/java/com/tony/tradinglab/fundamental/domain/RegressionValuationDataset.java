package com.tony.tradinglab.fundamental.domain;

import java.util.List;

public record RegressionValuationDataset(

        RegressionValuationMetric metric,

        RegressionValuationFeature target,

        List<RegressionValuationFeature> peerFeatures

) {

    public int peerCount() {
        return peerFeatures.size();
    }
}