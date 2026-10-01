package com.tony.tradinglab.discovery.quant;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class QuantBacktestAnalyzer {

    private static final int BUCKET_SIZE = 20;


    public List<BucketStats> analyzeByTotalScore(
            List<QuantBacktestSample> samples
    ) {

        if (samples == null
                || samples.isEmpty()) {

            return List.of();
        }


        List<BucketStats> results =
                new ArrayList<>();


        for (int bucketStart = 0;
             bucketStart < 100;
             bucketStart += BUCKET_SIZE) {

            int bucketEnd =
                    bucketStart + BUCKET_SIZE;


            int finalBucketStart =
                    bucketStart;

            int finalBucketEnd =
                    bucketEnd;


            List<QuantBacktestSample> bucketSamples =
                    samples.stream()

                            .filter(
                                    sample ->
                                            sample != null
                                                    && sample.score() != null
                                                    && sample.score()
                                                    .totalScore()
                                                    != null
                            )

                            .filter(
                                    sample -> {

                                        BigDecimal score =
                                                sample.score()
                                                        .totalScore();


                                        BigDecimal min =
                                                BigDecimal.valueOf(
                                                        finalBucketStart
                                                );


                                        BigDecimal max =
                                                BigDecimal.valueOf(
                                                        finalBucketEnd
                                                );


                                        /*
                                         * 마지막 구간만 100 포함.
                                         *
                                         * [0,20)
                                         * [20,40)
                                         * [40,60)
                                         * [60,80)
                                         * [80,100]
                                         */
                                        if (finalBucketEnd == 100) {

                                            return score.compareTo(min) >= 0
                                                    && score.compareTo(max) <= 0;
                                        }


                                        return score.compareTo(min) >= 0
                                                && score.compareTo(max) < 0;
                                    }
                            )

                            .toList();


            if (bucketSamples.isEmpty()) {
                continue;
            }


            List<BigDecimal> threeMonthReturns =
                    extractReturns(
                            bucketSamples,
                            Horizon.THREE_MONTH
                    );


            List<BigDecimal> sixMonthReturns =
                    extractReturns(
                            bucketSamples,
                            Horizon.SIX_MONTH
                    );


            List<BigDecimal> twelveMonthReturns =
                    extractReturns(
                            bucketSamples,
                            Horizon.TWELVE_MONTH
                    );


            results.add(
                    new BucketStats(

                            bucketStart,

                            bucketEnd,

                            bucketSamples.size(),

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


    private List<BigDecimal> extractReturns(
            List<QuantBacktestSample> samples,
            Horizon horizon
    ) {

        return samples.stream()

                .map(
                        sample -> {

                            return switch (horizon) {

                                case THREE_MONTH ->
                                        sample.threeMonthReturnPct();

                                case SIX_MONTH ->
                                        sample.sixMonthReturnPct();

                                case TWELVE_MONTH ->
                                        sample.twelveMonthReturnPct();
                            };
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

                        .sorted(
                                Comparator.naturalOrder()
                        )

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


    private enum Horizon {

        THREE_MONTH,

        SIX_MONTH,

        TWELVE_MONTH
    }


    public record BucketStats(

            int minScore,

            int maxScore,

            int sampleCount,

            BigDecimal averageThreeMonthReturnPct,

            BigDecimal medianThreeMonthReturnPct,

            BigDecimal averageSixMonthReturnPct,

            BigDecimal medianSixMonthReturnPct,

            BigDecimal averageTwelveMonthReturnPct,

            BigDecimal medianTwelveMonthReturnPct

    ) {
    }
}