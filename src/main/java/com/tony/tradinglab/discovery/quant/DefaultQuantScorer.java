package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import org.springframework.stereotype.Component;

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
            ValuationPeerSnapshotInput snapshot
    ) {

        if (snapshot == null) {

            return new QuantScoreBreakdown(
                    ZERO,
                    ZERO,
                    ZERO,
                    ZERO
            );
        }


        BigDecimal growthScore =
                calculateGrowthScore(
                        snapshot
                );


        /*
         * 아직 구현하지 않은 영역.
         */
        BigDecimal qualityScore =
                ZERO;

        BigDecimal valuationScore =
                ZERO;


        /*
         * 현재는 Growth Score만 구현되어 있으므로
         * totalScore 역시 Growth Score와 동일하게 둔다.
         *
         * Quality / Valuation 구현 후
         * 최종 가중합으로 변경한다.
         */
        BigDecimal totalScore =
                growthScore;


        return new QuantScoreBreakdown(
                growthScore,
                qualityScore,
                valuationScore,
                totalScore
        );
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
}