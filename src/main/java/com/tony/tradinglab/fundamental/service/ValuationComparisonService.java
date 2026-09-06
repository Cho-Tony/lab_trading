package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.springframework.stereotype.Service;

@Service
public class ValuationComparisonService {

    private final PercentileValuationEvaluator percentileEvaluator;

    private final RobustZScoreValuationEvaluator robustZScoreEvaluator;

    private final RegressionValuationDatasetBuilder regressionDatasetBuilder;

    private final RegressionAdjustedValuationEvaluator regressionEvaluator;


    public ValuationComparisonService(
            PercentileValuationEvaluator percentileEvaluator,
            RobustZScoreValuationEvaluator robustZScoreEvaluator,
            RegressionValuationDatasetBuilder regressionDatasetBuilder,
            RegressionAdjustedValuationEvaluator regressionEvaluator
    ) {

        this.percentileEvaluator =
                percentileEvaluator;

        this.robustZScoreEvaluator =
                robustZScoreEvaluator;

        this.regressionDatasetBuilder =
                regressionDatasetBuilder;

        this.regressionEvaluator =
                regressionEvaluator;
    }


    public ValuationComparisonResult compare(
            PeerUniverse universe,
            RegressionValuationMetric regressionMetric
    ) {

        validateUniverse(
                universe
        );


        /*
         * 1. Percentile
         */
        PercentileValuationAssessment percentile =
                percentileEvaluator.evaluate(
                        universe
                );


        /*
         * 2. Robust Z-score
         */
        RobustZScoreValuationAssessment robustZScore =
                robustZScoreEvaluator.evaluate(
                        universe
                );


        /*
         * 3. Regression-adjusted
         *
         * Feature 부족 / 회귀 불가능하면 null.
         */
        RegressionAdjustedValuationAssessment regressionAdjusted =
                regressionDatasetBuilder

                        .build(
                                universe,
                                regressionMetric
                        )

                        .flatMap(
                                regressionEvaluator::evaluate
                        )

                        .orElse(null);


        ValuationPeerSnapshot target =
                universe.target();


        return new ValuationComparisonResult(

                target.stockId(),

                target.symbol(),

                target.valuation()
                        .priceDate(),

                universe.selectionLevel(),

                universe.peers()
                        .size(),

                percentile,

                robustZScore,

                regressionAdjusted
        );
    }


    private void validateUniverse(
            PeerUniverse universe
    ) {

        if (universe == null
                || universe.target() == null
                || universe.target().valuation() == null) {

            throw new IllegalArgumentException(
                    "PeerUniverse and target valuation must not be null."
            );
        }
    }
}