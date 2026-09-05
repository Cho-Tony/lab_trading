package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.RegressionAdjustedValuationAssessment;
import com.tony.tradinglab.fundamental.domain.RegressionValuationDataset;
import com.tony.tradinglab.fundamental.domain.RegressionValuationFeature;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Component
public class RegressionAdjustedValuationEvaluator {

    /*
     * intercept + feature 2개이므로
     * 최소한 3개의 observation은 있어야
     * 수학적으로 계수를 구할 수 있다.
     *
     * 이것은 "신뢰할 만한 최소 Peer 수"를
     * 3으로 확정한다는 뜻은 아니다.
     *
     * 실제 운영 최소 sample size는
     * 나중에 백테스트로 별도 결정한다.
     */
    private static final int MIN_MATHEMATICAL_PEER_COUNT = 3;

    private static final int SCALE = 4;


    public Optional<RegressionAdjustedValuationAssessment> evaluate(
            RegressionValuationDataset dataset
    ) {

        if (dataset == null
                || dataset.target() == null) {

            return Optional.empty();
        }


        if (dataset.peerCount()
                < MIN_MATHEMATICAL_PEER_COUNT) {

            return Optional.empty();
        }


        double[] y =
                createDependentVariable(
                        dataset
                );


        double[][] x =
                createIndependentVariables(
                        dataset
                );


        OLSMultipleLinearRegression regression =
                new OLSMultipleLinearRegression();


        try {

            regression.newSampleData(
                    y,
                    x
            );


            /*
             * 반환 순서:
             *
             * [0] intercept
             * [1] revenue growth coefficient
             * [2] operating margin coefficient
             */
            double[] coefficients =
                    regression.estimateRegressionParameters();


            double intercept =
                    coefficients[0];

            double revenueGrowthCoefficient =
                    coefficients[1];

            double operatingMarginCoefficient =
                    coefficients[2];


            RegressionValuationFeature target =
                    dataset.target();


            double actualMultiple =
                    target.valuationMultiple()
                            .doubleValue();


            double actualLogMultiple =
                    Math.log(
                            actualMultiple
                    );


            double predictedLogMultiple =
                    intercept

                            + revenueGrowthCoefficient
                            * target.revenueGrowthPct()
                            .doubleValue()

                            + operatingMarginCoefficient
                            * target.operatingMarginPct()
                            .doubleValue();


            double predictedMultiple =
                    Math.exp(
                            predictedLogMultiple
                    );


            /*
             * 핵심 Regression-adjusted residual
             */
            double residual =
                    actualLogMultiple
                            - predictedLogMultiple;


            /*
             * 사람이 읽기 쉬운 할인/프리미엄 비율
             *
             * -20
             * → 모델 예상보다 20% 낮음
             *
             * +25
             * → 모델 예상보다 25% 높음
             */
            double relativeDeviationPct =
                    (
                            actualMultiple
                                    / predictedMultiple
                                    - 1.0
                    ) * 100.0;


            double rSquared =
                    regression.calculateRSquared();


            return Optional.of(
                    new RegressionAdjustedValuationAssessment(

                            target.symbol(),

                            dataset.metric(),

                            decimal(
                                    actualMultiple
                            ),

                            decimal(
                                    predictedMultiple
                            ),

                            decimal(
                                    actualLogMultiple
                            ),

                            decimal(
                                    predictedLogMultiple
                            ),

                            decimal(
                                    residual
                            ),

                            decimal(
                                    relativeDeviationPct
                            ),

                            decimal(
                                    intercept
                            ),

                            decimal(
                                    revenueGrowthCoefficient
                            ),

                            decimal(
                                    operatingMarginCoefficient
                            ),

                            decimal(
                                    rSquared
                            ),

                            dataset.peerCount()
                    )
            );

        } catch (RuntimeException e) {

            /*
             * Peer 데이터가 지나치게 동일하거나
             * feature들이 선형종속이면
             * 회귀계수를 안정적으로 구하지 못할 수 있다.
             *
             * 이런 경우 억지 결과를 만들지 않고
             * 평가 불가로 처리한다.
             */
            return Optional.empty();
        }
    }


    private double[] createDependentVariable(
            RegressionValuationDataset dataset
    ) {

        return dataset.peerFeatures()
                .stream()

                .mapToDouble(
                        feature ->
                                Math.log(
                                        feature.valuationMultiple()
                                                .doubleValue()
                                )
                )

                .toArray();
    }


    private double[][] createIndependentVariables(
            RegressionValuationDataset dataset
    ) {

        return dataset.peerFeatures()
                .stream()

                .map(
                        feature ->
                                new double[]{

                                        feature.revenueGrowthPct()
                                                .doubleValue(),

                                        feature.operatingMarginPct()
                                                .doubleValue()
                                }
                )

                .toArray(
                        double[][]::new
                );
    }


    private BigDecimal decimal(
            double value
    ) {

        if (!Double.isFinite(value)) {
            return null;
        }


        return BigDecimal.valueOf(
                        value
                )
                .setScale(
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }
}