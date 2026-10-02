package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import org.springframework.stereotype.Component;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DefaultQuantScorer
        implements QuantScorer {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    private static final BigDecimal HUNDRED =
            BigDecimal.valueOf(100);

    private static final BigDecimal TWO =
            BigDecimal.valueOf(2);


    @Override
    public QuantScoreBreakdown score(
            ValuationPeerSnapshotInput snapshot,
            ValuationComparisonResult valuationComparison
    ) {

        if (snapshot == null) {

            return new QuantScoreBreakdown(
                    ZERO,
                    ZERO,
                    ZERO,
                    false,
                    ZERO
            );
        }


        BigDecimal growthScore =
                calculateGrowthScore(
                        snapshot
                );


        BigDecimal qualityScore =
                calculateQualityScore(
                        snapshot
                );


        boolean valuationAvailable =
                isValuationAvailable(
                        valuationComparison
                );


        BigDecimal valuationScore =
                calculateValuationScore(
                        valuationComparison
                );


        /*
         * 아직 baseline 유지.
         *
         * Valuation unavailable인 경우에도 기존과 동일하게
         * valuationScore=0을 포함하여 평균낸다.
         *
         * 먼저 missing 원인을 확인한 뒤
         * 계산 방식을 바꿀지 결정한다.
         */
        BigDecimal totalScore =
                growthScore
                        .add(
                                qualityScore
                        )
                        .add(
                                valuationScore
                        )
                        .divide(
                                BigDecimal.valueOf(3),
                                2,
                                RoundingMode.HALF_UP
                        );


        return new QuantScoreBreakdown(
                growthScore,
                qualityScore,
                valuationScore,
                valuationAvailable,
                totalScore
        );
    }

    private boolean isValuationAvailable(
            ValuationComparisonResult comparison
    ) {

        if (comparison == null
                || comparison.percentile() == null) {

            return false;
        }


        return comparison.percentile()
                .pePercentile() != null

                || comparison.percentile()
                .psPercentile() != null

                || comparison.percentile()
                .priceToFcfPercentile() != null;
    }

    private BigDecimal calculateGrowthScore(
            ValuationPeerSnapshotInput snapshot
    ) {

        if (snapshot.growth() == null) {
            return ZERO;
        }


        BigDecimal latestYoyGrowth =
                snapshot.growth()
                        .latestYoyGrowthPct();


        BigDecimal averageAcceleration =
                snapshot.growth()
                        .averageAccelerationPctPoint();


        if (latestYoyGrowth == null) {
            return ZERO;
        }


        /*
         * 음수 성장률은 Growth Score의
         * 기본 점수를 0으로 본다.
         */
        BigDecimal positiveGrowth =
                latestYoyGrowth.max(
                        ZERO
                );


        /*
         * 성장률에 diminishing return 적용.
         *
         * 공식:
         *
         * growth / (growth + 25) * 100
         *
         * 예:
         *
         * 10%  -> 약 28.6점
         * 20%  -> 약 44.4점
         * 25%  -> 50점
         * 50%  -> 약 66.7점
         * 85%  -> 약 77.3점
         * 100% -> 80점
         *
         * 성장률이 매우 높더라도
         * 바로 100점에 포화되지 않는다.
         */
        BigDecimal baseScore =
                ZERO;


        if (positiveGrowth.signum() > 0) {

            baseScore =
                    positiveGrowth
                            .multiply(
                                    HUNDRED
                            )
                            .divide(
                                    positiveGrowth.add(
                                            BigDecimal.valueOf(25)
                                    ),
                                    4,
                                    RoundingMode.HALF_UP
                            );
        }


        /*
         * 성장 가속도 보정.
         *
         * +1%p 가속 = +1점
         * -1%p 둔화 = -1점
         *
         * 가속도가 전체 점수를 지배하지 않도록
         * ±10점으로 제한한다.
         */
        BigDecimal accelerationAdjustment =
                ZERO;


        if (averageAcceleration != null) {

            accelerationAdjustment =
                    clamp(
                            averageAcceleration,
                            BigDecimal.valueOf(-10),
                            BigDecimal.valueOf(10)
                    );
        }


        BigDecimal score =
                baseScore.add(
                        accelerationAdjustment
                );


        return clamp(
                score,
                ZERO,
                HUNDRED
        ).setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal clamp(
            BigDecimal value,
            BigDecimal min,
            BigDecimal max
    ) {

        if (value.compareTo(min) < 0) {
            return min;
        }


        if (value.compareTo(max) > 0) {
            return max;
        }


        return value;
    }

    private BigDecimal calculateQualityScore(
            ValuationPeerSnapshotInput snapshot
    ) {

        if (snapshot.valuation() == null) {
            return ZERO;
        }


        BigDecimal revenue =
                snapshot.valuation()
                        .ttmRevenue();

        BigDecimal netIncome =
                snapshot.valuation()
                        .ttmNetIncome();

        BigDecimal freeCashFlow =
                snapshot.valuation()
                        .ttmFreeCashFlow();


        if (revenue == null
                || revenue.signum() <= 0) {

            return ZERO;
        }


        /*
         * TTM Net Margin
         */
        BigDecimal netMarginPct =
                calculateMarginPct(
                        netIncome,
                        revenue
                );


        /*
         * TTM Free Cash Flow Margin
         */
        BigDecimal fcfMarginPct =
                calculateMarginPct(
                        freeCashFlow,
                        revenue
                );


        /*
         * Net Margin 최대 45점.
         *
         * 20% margin이면 약 절반 수준,
         * 이후에는 diminishing return.
         */
        BigDecimal netMarginScore =
                calculateMarginScore(
                        netMarginPct,
                        BigDecimal.valueOf(20),
                        BigDecimal.valueOf(45)
                );


        /*
         * FCF Margin 최대 45점.
         */
        BigDecimal fcfMarginScore =
                calculateMarginScore(
                        fcfMarginPct,
                        BigDecimal.valueOf(20),
                        BigDecimal.valueOf(45)
                );


        /*
         * 수익성 개선 추세 최대 ±10점.
         *
         * ProfitabilityTrendAnalysis 자체가
         * unavailable인 기업도 있으므로
         * 이 부분은 optional bonus/penalty로 취급한다.
         */
        BigDecimal trendAdjustment =
                ZERO;


        if (snapshot.profitability() != null) {

            BigDecimal operatingMarginChange =
                    snapshot.profitability()
                            .averageOperatingMarginChangePctPoint();

            BigDecimal netMarginChange =
                    snapshot.profitability()
                            .averageNetMarginChangePctPoint();


            if (operatingMarginChange != null) {

                trendAdjustment =
                        trendAdjustment.add(
                                operatingMarginChange
                        );
            }


            if (netMarginChange != null) {

                trendAdjustment =
                        trendAdjustment.add(
                                netMarginChange
                        );
            }


            trendAdjustment =
                    clamp(
                            trendAdjustment,
                            BigDecimal.valueOf(-10),
                            BigDecimal.valueOf(10)
                    );
        }


        BigDecimal score =
                netMarginScore
                        .add(
                                fcfMarginScore
                        )
                        .add(
                                trendAdjustment
                        );


        return clamp(
                score,
                ZERO,
                HUNDRED
        ).setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculateMarginPct(
            BigDecimal value,
            BigDecimal revenue
    ) {

        if (value == null
                || revenue == null
                || revenue.signum() <= 0) {

            return ZERO;
        }


        return value
                .multiply(
                        HUNDRED
                )
                .divide(
                        revenue,
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateMarginScore(
            BigDecimal marginPct,
            BigDecimal halfSaturationPoint,
            BigDecimal maxScore
    ) {

        if (marginPct == null
                || marginPct.signum() <= 0) {

            return ZERO;
        }


        /*
         * diminishing return:
         *
         * margin
         * --------------------- × maxScore
         * margin + 기준값
         *
         * 기준값이 20이면:
         *
         * margin 10% → 최대점수의 약 33%
         * margin 20% → 최대점수의 50%
         * margin 40% → 최대점수의 약 67%
         */
        return marginPct
                .multiply(
                        maxScore
                )
                .divide(
                        marginPct.add(
                                halfSaturationPoint
                        ),
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateValuationScore(
            ValuationComparisonResult comparison
    ) {

        if (comparison == null
                || comparison.percentile() == null) {

            return ZERO;
        }


        List<BigDecimal> scores =
                new ArrayList<>();


        BigDecimal pePercentile =
                comparison.percentile()
                        .pePercentile();

        BigDecimal psPercentile =
                comparison.percentile()
                        .psPercentile();

        BigDecimal priceToFcfPercentile =
                comparison.percentile()
                        .priceToFcfPercentile();


        /*
         * Percentile이 낮을수록
         * peer 대비 multiple이 낮다는 뜻이므로
         *
         * Valuation Score = 100 - Percentile
         *
         * 예:
         *
         * percentile 20
         * → valuation score 80
         *
         * percentile 80
         * → valuation score 20
         */
        if (pePercentile != null) {

            scores.add(
                    HUNDRED.subtract(
                            pePercentile
                    )
            );
        }


        if (psPercentile != null) {

            scores.add(
                    HUNDRED.subtract(
                            psPercentile
                    )
            );
        }


        if (priceToFcfPercentile != null) {

            scores.add(
                    HUNDRED.subtract(
                            priceToFcfPercentile
                    )
            );
        }


        if (scores.isEmpty()) {
            return ZERO;
        }


        BigDecimal total =
                scores.stream()
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );


        return total
                .divide(
                        BigDecimal.valueOf(
                                scores.size()
                        ),
                        2,
                        RoundingMode.HALF_UP
                );
    }
}