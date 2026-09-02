package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.ProfitabilityDirection;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrend;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrendAnalysis;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Component
public class ProfitabilityTrendAnalyzer {

    private static final BigDecimal CHANGE_THRESHOLD =
            new BigDecimal("1.00");

    private static final int ANALYSIS_QUARTERS = 3;

    private static final int MINIMUM_TREND_COUNT = 2;

    public ProfitabilityTrendAnalysis analyze(
            List<ProfitabilityTrend> trends
    ) {

        if (trends == null || trends.isEmpty()) {
            return null;
        }

        List<ProfitabilityTrend> recent =
                trends.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                ProfitabilityTrend::fiscalYear
                                        )
                                        .thenComparing(
                                                trend -> quarterOrder(
                                                        trend.fiscalQuarter()
                                                )
                                        )
                                        .reversed()
                        )
                        .limit(ANALYSIS_QUARTERS)
                        .toList();

        ProfitabilityTrend latest =
                recent.get(0);

        ProfitabilityDirection operatingDirection =
                analyzeDirection(
                        recent.stream()
                                .map(
                                        ProfitabilityTrend
                                                ::operatingMarginChangePctPoint
                                )
                                .toList()
                );

        ProfitabilityDirection netDirection =
                analyzeDirection(
                        recent.stream()
                                .map(
                                        ProfitabilityTrend
                                                ::netMarginChangePctPoint
                                )
                                .toList()
                );

        BigDecimal averageOperatingChange =
                calculateAverage(
                        recent.stream()
                                .map(
                                        ProfitabilityTrend
                                                ::operatingMarginChangePctPoint
                                )
                                .toList()
                );

        BigDecimal averageNetChange =
                calculateAverage(
                        recent.stream()
                                .map(
                                        ProfitabilityTrend
                                                ::netMarginChangePctPoint
                                )
                                .toList()
                );

        return new ProfitabilityTrendAnalysis(

                operatingDirection,
                netDirection,

                latest.operatingMarginPct(),
                latest.netMarginPct(),

                averageOperatingChange,
                averageNetChange,

                recent.size()
        );
    }

    private ProfitabilityDirection analyzeDirection(
            List<BigDecimal> changes
    ) {

        List<BigDecimal> validChanges =
                changes.stream()
                        .filter(change -> change != null)
                        .toList();

        if (validChanges.size() < MINIMUM_TREND_COUNT) {
            return ProfitabilityDirection.INSUFFICIENT_DATA;
        }

        int positiveCount = 0;
        int negativeCount = 0;

        for (BigDecimal change : validChanges) {

            if (change.compareTo(CHANGE_THRESHOLD) >= 0) {

                positiveCount++;

            } else if (
                    change.compareTo(
                            CHANGE_THRESHOLD.negate()
                    ) <= 0
            ) {

                negativeCount++;
            }
        }

        if (positiveCount == 0
                && negativeCount == 0) {

            return ProfitabilityDirection.STABLE;
        }

        if (positiveCount >= 2
                && negativeCount == 0) {

            return ProfitabilityDirection.IMPROVING;
        }

        if (negativeCount >= 2
                && positiveCount == 0) {

            return ProfitabilityDirection.DETERIORATING;
        }

        return ProfitabilityDirection.MIXED;
    }

    private BigDecimal calculateAverage(
            List<BigDecimal> values
    ) {

        List<BigDecimal> validValues =
                values.stream()
                        .filter(value -> value != null)
                        .toList();

        if (validValues.isEmpty()) {
            return null;
        }

        BigDecimal sum =
                validValues.stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return sum.divide(
                BigDecimal.valueOf(
                        validValues.size()
                ),
                2,
                RoundingMode.HALF_UP
        );
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
                            "알 수 없는 quarter: "
                                    + quarter
                    );
        };
    }
}