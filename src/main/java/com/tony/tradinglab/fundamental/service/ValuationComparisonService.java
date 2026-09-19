package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.PercentileValuationAssessment;
import com.tony.tradinglab.fundamental.domain.RegressionAdjustedValuationAssessment;
import com.tony.tradinglab.fundamental.domain.RegressionValuationMetric;
import com.tony.tradinglab.fundamental.domain.RobustZScoreValuationAssessment;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValuationComparisonService {

    private final PercentileValuationEvaluator percentileEvaluator;

    private final RobustZScoreValuationEvaluator robustZScoreEvaluator;

    private final RegressionValuationDatasetBuilder regressionDatasetBuilder;

    private final RegressionAdjustedValuationEvaluator regressionEvaluator;


    public ValuationComparisonResult compare(
            PeerUniverse universe,
            RegressionValuationMetric regressionMetric
    ) {

        validateUniverse(
                universe
        );


        PercentileValuationAssessment percentile =
                percentileEvaluator.evaluate(
                        universe
                );


        RobustZScoreValuationAssessment robustZScore =
                robustZScoreEvaluator.evaluate(
                        universe
                );


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
                target.valuation().priceDate(),
                universe.selectionLevel(),
                universe.peers().size(),
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