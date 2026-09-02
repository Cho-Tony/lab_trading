package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.GrowthAcceleration;
import com.tony.tradinglab.fundamental.domain.GrowthTrend;
import com.tony.tradinglab.fundamental.domain.GrowthTrendAnalysis;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Component
public class GrowthTrendAnalyzer {

    /*
     * ±2%p 이내의 변화는
     * 의미 있는 가속/감속으로 보지 않는다.
     */
    private static final BigDecimal ACCELERATION_THRESHOLD =
            new BigDecimal("2.00");

    private static final int ANALYSIS_QUARTERS = 3;

    public GrowthTrendAnalysis analyze(
            List<GrowthAcceleration> accelerations
    ) {

        if (accelerations == null || accelerations.isEmpty()) {
            throw new IllegalArgumentException(
                    "성장 추세 분석을 위한 데이터가 없습니다."
            );
        }

        List<GrowthAcceleration> recent =
                accelerations.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                GrowthAcceleration::fiscalYear
                                        )
                                        .thenComparing(
                                                a -> quarterOrder(
                                                        a.fiscalQuarter()
                                                )
                                        )
                                        .reversed()
                        )
                        .limit(ANALYSIS_QUARTERS)
                        .toList();

        int positiveCount = 0;
        int negativeCount = 0;

        BigDecimal accelerationSum =
                BigDecimal.ZERO;

        for (GrowthAcceleration acceleration : recent) {

            BigDecimal value =
                    acceleration.accelerationPctPoint();

            accelerationSum =
                    accelerationSum.add(value);

            if (value.compareTo(
                    ACCELERATION_THRESHOLD
            ) > 0) {

                positiveCount++;

            } else if (value.compareTo(
                    ACCELERATION_THRESHOLD.negate()
            ) < 0) {

                negativeCount++;
            }
        }

        BigDecimal averageAcceleration =
                accelerationSum.divide(
                        BigDecimal.valueOf(recent.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        GrowthTrend trend =
                determineTrend(
                        recent.size(),
                        positiveCount,
                        negativeCount
                );

        GrowthAcceleration latest =
                recent.get(0);

        return new GrowthTrendAnalysis(
                trend,
                latest.yoyGrowthPct(),
                averageAcceleration,
                positiveCount,
                negativeCount,
                recent.size()
        );
    }

    private GrowthTrend determineTrend(
            int count,
            int positiveCount,
            int negativeCount
    ) {

        int requiredCount =
                Math.max(1, count - 1);

        if (positiveCount >= requiredCount) {
            return GrowthTrend.ACCELERATING;
        }

        if (negativeCount >= requiredCount) {
            return GrowthTrend.DECELERATING;
        }

        if (positiveCount == 0
                && negativeCount == 0) {

            return GrowthTrend.STABLE;
        }

        return GrowthTrend.MIXED;
    }

    private int quarterOrder(
            String quarter
    ) {

        return switch (quarter) {

            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;

            default ->
                    throw new IllegalArgumentException(
                            "알 수 없는 fiscal quarter: "
                                    + quarter
                    );
        };
    }
}