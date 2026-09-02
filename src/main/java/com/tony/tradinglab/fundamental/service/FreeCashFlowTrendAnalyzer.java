package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FreeCashFlowDirection;
import com.tony.tradinglab.fundamental.domain.FreeCashFlowMetrics;
import com.tony.tradinglab.fundamental.domain.FreeCashFlowTrendAnalysis;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
public class FreeCashFlowTrendAnalyzer {

    private static final int ANALYSIS_QUARTERS = 3;

    /*
     * FCF YoY ±10% 이내의 움직임은
     * 일단 큰 방향 변화가 없는 것으로 본다.
     *
     * v1 휴리스틱이며 나중에 백테스트로 조정.
     */
    private static final BigDecimal GROWTH_THRESHOLD =
            new BigDecimal("10.00");

    public FreeCashFlowTrendAnalysis analyze(
            List<FreeCashFlowMetrics> metrics
    ) {

        if (metrics == null || metrics.isEmpty()) {
            return null;
        }

        List<FreeCashFlowMetrics> recent =
                metrics.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                FreeCashFlowMetrics::fiscalYear
                                        )
                                        .thenComparing(
                                                metric ->
                                                        quarterOrder(
                                                                metric.fiscalQuarter()
                                                        )
                                        )
                                        .reversed()
                        )
                        .limit(ANALYSIS_QUARTERS)
                        .toList();

        FreeCashFlowMetrics latest =
                recent.get(0);

        int positiveGrowthCount = 0;
        int negativeGrowthCount = 0;
        int negativeFcfCount = 0;

        for (FreeCashFlowMetrics metric : recent) {

            if (metric.freeCashFlow() != null
                    && metric.freeCashFlow()
                    .compareTo(BigDecimal.ZERO) <= 0) {

                negativeFcfCount++;
            }

            BigDecimal growth =
                    metric.freeCashFlowGrowthYoYPct();

            if (growth == null) {
                continue;
            }

            if (growth.compareTo(
                    GROWTH_THRESHOLD
            ) >= 0) {

                positiveGrowthCount++;

            } else if (
                    growth.compareTo(
                            GROWTH_THRESHOLD.negate()
                    ) <= 0
            ) {

                negativeGrowthCount++;
            }
        }

        FreeCashFlowDirection direction =
                determineDirection(
                        recent,
                        latest,
                        positiveGrowthCount,
                        negativeGrowthCount,
                        negativeFcfCount
                );

        return new FreeCashFlowTrendAnalysis(

                direction,

                latest.freeCashFlow(),
                latest.freeCashFlowMarginPct(),
                latest.freeCashFlowGrowthYoYPct(),

                positiveGrowthCount,
                negativeGrowthCount,
                negativeFcfCount,

                recent.size()
        );
    }

    private FreeCashFlowDirection determineDirection(
            List<FreeCashFlowMetrics> recent,
            FreeCashFlowMetrics latest,
            int positiveGrowthCount,
            int negativeGrowthCount,
            int negativeFcfCount
    ) {

        /*
         * 전년 동기 적자 → 현재 흑자.
         * 가장 강한 반전 신호 중 하나.
         */
        if (latest.turnaround()) {
            return FreeCashFlowDirection.TURNAROUND;
        }

        /*
         * 최근 관찰 분기 모두 FCF <= 0이면
         * 지속적인 마이너스 FCF 상태.
         *
         * 단, 이것만으로 기업을 REJECT하지는 않는다.
         * 성장투자 기업일 수도 있기 때문.
         */
        if (recent.size() >= 2
                && negativeFcfCount == recent.size()) {

            return FreeCashFlowDirection.PERSISTENT_NEGATIVE;
        }

        /*
         * 성장 신호가 반복되고
         * 의미 있는 악화가 없음.
         */
        if (positiveGrowthCount >= 2
                && negativeGrowthCount == 0) {

            return FreeCashFlowDirection.IMPROVING;
        }

        /*
         * 악화가 반복되고
         * 의미 있는 개선이 없음.
         */
        if (negativeGrowthCount >= 2
                && positiveGrowthCount == 0) {

            return FreeCashFlowDirection.DETERIORATING;
        }

        /*
         * 성장/악화 신호가 모두 없음.
         */
        if (positiveGrowthCount == 0
                && negativeGrowthCount == 0) {

            return FreeCashFlowDirection.STABLE;
        }

        /*
         * 데이터 자체가 너무 적은 경우.
         */
        if (recent.size() < 2) {

            return FreeCashFlowDirection.INSUFFICIENT_DATA;
        }

        return FreeCashFlowDirection.MIXED;
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