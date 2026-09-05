package com.tony.tradinglab.fundamental.domain;

public record RobustZScoreValuationAssessment(

        String symbol,

        RobustZScoreMetric pe,
        RobustZScoreMetric ps,
        RobustZScoreMetric priceToFcf

) {
}