package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ValuationBacktestStabilityAnalyzer {

    private static final int SCALE = 4;

    private final ValuationBacktestAnalyzer backtestAnalyzer;


    public ValuationBacktestStabilityAnalyzer(
            ValuationBacktestAnalyzer backtestAnalyzer
    ) {

        this.backtestAnalyzer =
                backtestAnalyzer;
    }


    public ValuationBacktestStabilitySummary analyze(
            List<ValuationBacktestObservation> observations,
            ValuationBacktestMethod method,
            BacktestHorizon horizon
    ) {

        /*
         * observationDate의 연도로 그룹핑
         */
        Map<Integer, List<ValuationBacktestObservation>> byYear =
                observations.stream()

                        .filter(
                                observation ->
                                        observation != null
                                                && observation.observationDate() != null
                        )

                        .collect(
                                Collectors.groupingBy(
                                        observation ->
                                                observation
                                                        .observationDate()
                                                        .getYear()
                                )
                        );


        List<YearlyValuationBacktestSummary> yearlySummaries =
                byYear.entrySet()
                        .stream()

                        .map(
                                entry ->
                                        createYearlySummary(
                                                entry.getKey(),
                                                entry.getValue(),
                                                method,
                                                horizon
                                        )
                        )

                        /*
                         * 실제 분석 가능한 observation이
                         * 하나도 없는 연도는 제외
                         */
                        .filter(
                                yearly ->
                                        yearly.summary()
                                                .observationCount() > 0
                        )

                        .sorted(
                                Comparator.comparingInt(
                                        YearlyValuationBacktestSummary::year
                                )
                        )

                        .toList();


        int negativeCorrelationYearCount =
                (int) yearlySummaries.stream()

                        .map(
                                YearlyValuationBacktestSummary::summary
                        )

                        .map(
                                ValuationMethodBacktestSummary
                                        ::spearmanCorrelation
                        )

                        .filter(
                                value ->
                                        value != null
                                                && value.compareTo(
                                                BigDecimal.ZERO
                                        ) < 0
                        )

                        .count();


        int positiveSpreadYearCount =
                (int) yearlySummaries.stream()

                        .map(
                                YearlyValuationBacktestSummary::summary
                        )

                        .map(
                                ValuationMethodBacktestSummary
                                        ::quintileSpreadPctPoint
                        )

                        .filter(
                                value ->
                                        value != null
                                                && value.compareTo(
                                                BigDecimal.ZERO
                                        ) > 0
                        )

                        .count();


        BigDecimal averageCorrelation =
                average(
                        yearlySummaries.stream()

                                .map(
                                        YearlyValuationBacktestSummary
                                                ::summary
                                )

                                .map(
                                        ValuationMethodBacktestSummary
                                                ::spearmanCorrelation
                                )

                                .filter(
                                        value ->
                                                value != null
                                )

                                .toList()
                );


        List<BigDecimal> spreads =
                yearlySummaries.stream()

                        .map(
                                YearlyValuationBacktestSummary::summary
                        )

                        .map(
                                ValuationMethodBacktestSummary
                                        ::quintileSpreadPctPoint
                        )

                        .filter(
                                value ->
                                        value != null
                        )

                        .toList();


        BigDecimal averageSpread =
                average(
                        spreads
                );


        BigDecimal worstSpread =
                spreads.stream()
                        .min(
                                BigDecimal::compareTo
                        )
                        .orElse(null);


        BigDecimal bestSpread =
                spreads.stream()
                        .max(
                                BigDecimal::compareTo
                        )
                        .orElse(null);


        return new ValuationBacktestStabilitySummary(

                method,
                horizon,

                List.copyOf(
                        yearlySummaries
                ),

                yearlySummaries.size(),

                negativeCorrelationYearCount,
                positiveSpreadYearCount,

                averageCorrelation,
                averageSpread,

                worstSpread,
                bestSpread
        );
    }


    private YearlyValuationBacktestSummary createYearlySummary(
            int year,
            List<ValuationBacktestObservation> observations,
            ValuationBacktestMethod method,
            BacktestHorizon horizon
    ) {

        ValuationMethodBacktestSummary summary =
                backtestAnalyzer.analyze(
                        observations,
                        method,
                        horizon
                );


        return new YearlyValuationBacktestSummary(
                year,
                summary
        );
    }


    private BigDecimal average(
            List<BigDecimal> values
    ) {

        if (values.isEmpty()) {
            return null;
        }


        BigDecimal sum =
                values.stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        return sum.divide(

                BigDecimal.valueOf(
                        values.size()
                ),

                SCALE,

                RoundingMode.HALF_UP
        );
    }
}