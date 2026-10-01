package com.tony.tradinglab.discovery.quant;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class QuantScoreQuintileAnalyzer {

    private static final int QUINTILE_COUNT = 5;


    public List<QuintileStats> analyze(
            List<QuantBacktestSample> samples,
            ScoreType scoreType
    ) {

        if (samples == null
                || samples.isEmpty()
                || scoreType == null) {

            return List.of();
        }


        List<QuantBacktestSample> validSamples =
                samples.stream()

                        .filter(
                                sample ->
                                        sample != null
                                                && sample.score() != null
                        )

                        .filter(
                                sample ->
                                        scoreOf(
                                                sample,
                                                scoreType
                                        ) != null
                        )

                        .sorted(
                                Comparator.comparing(
                                        sample ->
                                                scoreOf(
                                                        sample,
                                                        scoreType
                                                )
                                )
                        )

                        .toList();


        if (validSamples.size()
                < QUINTILE_COUNT) {

            return List.of();
        }


        List<QuintileStats> results =
                new ArrayList<>();


        int sampleCount =
                validSamples.size();


        for (int quintile = 1;
             quintile <= QUINTILE_COUNT;
             quintile++) {

            int fromIndex =
                    (quintile - 1)
                            * sampleCount
                            / QUINTILE_COUNT;


            int toIndex =
                    quintile
                            * sampleCount
                            / QUINTILE_COUNT;


            List<QuantBacktestSample> bucket =
                    validSamples.subList(
                            fromIndex,
                            toIndex
                    );


            if (bucket.isEmpty()) {
                continue;
            }


            BigDecimal minScore =
                    scoreOf(
                            bucket.get(0),
                            scoreType
                    );


            BigDecimal maxScore =
                    scoreOf(
                            bucket.get(
                                    bucket.size() - 1
                            ),
                            scoreType
                    );


            List<BigDecimal> threeMonthReturns =
                    extractReturns(
                            bucket,
                            Horizon.THREE_MONTH
                    );


            List<BigDecimal> sixMonthReturns =
                    extractReturns(
                            bucket,
                            Horizon.SIX_MONTH
                    );


            List<BigDecimal> twelveMonthReturns =
                    extractReturns(
                            bucket,
                            Horizon.TWELVE_MONTH
                    );


            results.add(
                    new QuintileStats(

                            scoreType,

                            quintile,

                            bucket.size(),

                            minScore,

                            maxScore,

                            average(
                                    threeMonthReturns
                            ),

                            median(
                                    threeMonthReturns
                            ),

                            average(
                                    sixMonthReturns
                            ),

                            median(
                                    sixMonthReturns
                            ),

                            average(
                                    twelveMonthReturns
                            ),

                            median(
                                    twelveMonthReturns
                            )
                    )
            );
        }


        return List.copyOf(
                results
        );
    }


    private BigDecimal scoreOf(
            QuantBacktestSample sample,
            ScoreType scoreType
    ) {

        return switch (scoreType) {

            case GROWTH ->
                    sample.score()
                            .growthScore();

            case QUALITY ->
                    sample.score()
                            .qualityScore();

            case VALUATION ->
                    sample.score()
                            .valuationScore();

            case TOTAL ->
                    sample.score()
                            .totalScore();
        };
    }


    private List<BigDecimal> extractReturns(
            List<QuantBacktestSample> samples,
            Horizon horizon
    ) {

        return samples.stream()

                .map(
                        sample ->
                                switch (horizon) {

                                    case THREE_MONTH ->
                                            sample.threeMonthReturnPct();

                                    case SIX_MONTH ->
                                            sample.sixMonthReturnPct();

                                    case TWELVE_MONTH ->
                                            sample.twelveMonthReturnPct();
                                }
                )

                .filter(
                        value ->
                                value != null
                )

                .sorted()

                .toList();
    }


    private BigDecimal average(
            List<BigDecimal> values
    ) {

        if (values == null
                || values.isEmpty()) {

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
                2,
                RoundingMode.HALF_UP
        );
    }


    private BigDecimal median(
            List<BigDecimal> values
    ) {

        if (values == null
                || values.isEmpty()) {

            return null;
        }


        List<BigDecimal> sorted =
                values.stream()

                        .sorted()

                        .toList();


        int size =
                sorted.size();


        int middle =
                size / 2;


        if (size % 2 == 1) {

            return sorted.get(
                    middle
            ).setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }


        return sorted.get(
                        middle - 1
                )
                .add(
                        sorted.get(
                                middle
                        )
                )
                .divide(
                        BigDecimal.valueOf(2),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    public enum ScoreType {

        GROWTH,

        QUALITY,

        VALUATION,

        TOTAL
    }


    private enum Horizon {

        THREE_MONTH,

        SIX_MONTH,

        TWELVE_MONTH
    }


    public record QuintileStats(

            ScoreType scoreType,

            int quintile,

            int sampleCount,

            BigDecimal minScore,

            BigDecimal maxScore,

            BigDecimal averageThreeMonthReturnPct,

            BigDecimal medianThreeMonthReturnPct,

            BigDecimal averageSixMonthReturnPct,

            BigDecimal medianSixMonthReturnPct,

            BigDecimal averageTwelveMonthReturnPct,

            BigDecimal medianTwelveMonthReturnPct

    ) {
    }
}