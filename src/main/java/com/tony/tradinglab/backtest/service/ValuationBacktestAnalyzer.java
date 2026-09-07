package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ValuationBacktestAnalyzer {

    private static final int QUINTILE_COUNT = 5;

    private static final int SCALE = 4;


    public ValuationMethodBacktestSummary analyze(
            List<ValuationBacktestObservation> observations,
            ValuationBacktestMethod method,
            BacktestHorizon horizon
    ) {

        List<ObservationPoint> points =
                observations.stream()

                        .map(
                                observation ->
                                        toPoint(
                                                observation,
                                                method,
                                                horizon
                                        )
                        )

                        .filter(
                                point ->
                                        point != null
                        )

                        .sorted(
                                Comparator.comparing(
                                        ObservationPoint::valuationScore
                                )
                        )

                        .toList();


        if (points.isEmpty()) {

            return new ValuationMethodBacktestSummary(

                    method,
                    horizon,

                    0,

                    null,

                    List.of(),

                    null,
                    null,
                    null
            );
        }


        BigDecimal correlation =
                calculateSpearman(
                        points
                );


        List<QuintileReturnSummary> quintiles =
                calculateQuintiles(
                        points
                );


        BigDecimal cheapest =
                findQuintileAverage(
                        quintiles,
                        1
                );


        BigDecimal mostExpensive =
                findQuintileAverage(
                        quintiles,
                        5
                );


        BigDecimal spread =
                subtract(
                        cheapest,
                        mostExpensive
                );


        return new ValuationMethodBacktestSummary(

                method,
                horizon,

                points.size(),

                correlation,

                quintiles,

                cheapest,
                mostExpensive,

                spread
        );
    }


    private ObservationPoint toPoint(
            ValuationBacktestObservation observation,
            ValuationBacktestMethod method,
            BacktestHorizon horizon
    ) {

        if (observation == null
                || observation.valuationComparison() == null
                || observation.excessReturns() == null) {

            return null;
        }


        BigDecimal valuationScore =
                extractValuationScore(
                        observation.valuationComparison(),
                        method
                );


        BigDecimal excessReturn =
                extractExcessReturn(
                        observation.excessReturns(),
                        horizon
                );


        if (valuationScore == null
                || excessReturn == null) {

            return null;
        }


        return new ObservationPoint(

                valuationScore,
                excessReturn
        );
    }


    private BigDecimal extractValuationScore(
            ValuationComparisonResult comparison,
            ValuationBacktestMethod method
    ) {

        return switch (method) {

            case PERCENTILE_PS -> {

                if (comparison.percentile() == null) {
                    yield null;
                }

                yield comparison.percentile()
                        .psPercentile();
            }


            case ROBUST_Z_PS -> {

                if (comparison.robustZScore() == null
                        || comparison.robustZScore().ps() == null) {

                    yield null;
                }

                yield comparison.robustZScore()
                        .ps()
                        .robustZScore();
            }


            case REGRESSION_ADJUSTED_PS -> {

                if (comparison.regressionAdjusted() == null) {
                    yield null;
                }

                yield comparison.regressionAdjusted()
                        .relativeDeviationPct();
            }
        };
    }


    private BigDecimal extractExcessReturn(
            ExcessReturnMetrics returns,
            BacktestHorizon horizon
    ) {

        return switch (horizon) {

            case DAYS_63 ->
                    returns.excessReturn63dPct();

            case DAYS_126 ->
                    returns.excessReturn126dPct();

            case DAYS_252 ->
                    returns.excessReturn252dPct();
        };
    }


    private BigDecimal calculateSpearman(
            List<ObservationPoint> points
    ) {

        /*
         * correlation 계산에는 최소 2개 필요.
         */
        if (points.size() < 2) {
            return null;
        }


        double[] valuationScores =
                points.stream()

                        .mapToDouble(
                                point ->
                                        point.valuationScore()
                                                .doubleValue()
                        )

                        .toArray();


        double[] returns =
                points.stream()

                        .mapToDouble(
                                point ->
                                        point.excessReturn()
                                                .doubleValue()
                        )

                        .toArray();


        double correlation =
                new SpearmansCorrelation()
                        .correlation(
                                valuationScores,
                                returns
                        );


        if (!Double.isFinite(correlation)) {
            return null;
        }


        return BigDecimal.valueOf(
                        correlation
                )
                .setScale(
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }


    private List<QuintileReturnSummary> calculateQuintiles(
            List<ObservationPoint> sortedPoints
    ) {

        List<List<ObservationPoint>> groups =
                new ArrayList<>();


        for (int i = 0;
             i < QUINTILE_COUNT;
             i++) {

            groups.add(
                    new ArrayList<>()
            );
        }


        /*
         * valuationScore 오름차순 상태.
         *
         * 가장 싼 종목 → Q1
         * 가장 비싼 종목 → Q5
         */
        for (int i = 0;
             i < sortedPoints.size();
             i++) {

            int quintileIndex =
                    (int) (
                            (long) i
                                    * QUINTILE_COUNT
                                    / sortedPoints.size()
                    );


            if (quintileIndex
                    >= QUINTILE_COUNT) {

                quintileIndex =
                        QUINTILE_COUNT - 1;
            }


            groups.get(
                    quintileIndex
            ).add(
                    sortedPoints.get(i)
            );
        }


        List<QuintileReturnSummary> result =
                new ArrayList<>();


        for (int i = 0;
             i < QUINTILE_COUNT;
             i++) {

            List<ObservationPoint> group =
                    groups.get(i);


            result.add(
                    new QuintileReturnSummary(

                            i + 1,

                            group.size(),

                            averageReturn(
                                    group
                            )
                    )
            );
        }


        return List.copyOf(result);
    }


    private BigDecimal averageReturn(
            List<ObservationPoint> points
    ) {

        if (points.isEmpty()) {
            return null;
        }


        BigDecimal sum =
                points.stream()

                        .map(
                                ObservationPoint::excessReturn
                        )

                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        return sum.divide(

                BigDecimal.valueOf(
                        points.size()
                ),

                SCALE,

                RoundingMode.HALF_UP
        );
    }


    private BigDecimal findQuintileAverage(
            List<QuintileReturnSummary> quintiles,
            int quintile
    ) {

        return quintiles.stream()

                .filter(
                        summary ->
                                summary.quintile()
                                        == quintile
                )

                .map(
                        QuintileReturnSummary
                                ::averageExcessReturnPct
                )

                .findFirst()

                .orElse(null);
    }


    private BigDecimal subtract(
            BigDecimal first,
            BigDecimal second
    ) {

        if (first == null
                || second == null) {

            return null;
        }


        return first
                .subtract(second)

                .setScale(
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }


    private record ObservationPoint(

            BigDecimal valuationScore,

            BigDecimal excessReturn

    ) {
    }
}