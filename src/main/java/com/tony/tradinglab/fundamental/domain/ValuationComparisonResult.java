package com.tony.tradinglab.fundamental.domain;

import java.time.LocalDate;

public record ValuationComparisonResult(

        Long stockId,
        String symbol,

        LocalDate priceDate,

        PeerSelectionLevel peerSelectionLevel,
        int peerCount,

        PercentileValuationAssessment percentile,

        RobustZScoreValuationAssessment robustZScore,

        RegressionAdjustedValuationAssessment regressionAdjusted

) {
}