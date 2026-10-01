package com.tony.tradinglab.smoke;

import com.tony.tradinglab.discovery.quant.*;
import com.tony.tradinglab.fundamental.domain.PercentileValuationAssessment;
import com.tony.tradinglab.fundamental.domain.RegressionAdjustedValuationAssessment;
import com.tony.tradinglab.fundamental.domain.RobustZScoreValuationAssessment;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.fundamental.service.PointInTimeValuationComparisonService;
import com.tony.tradinglab.fundamental.service.PointInTimeValuationSnapshotService;
import com.tony.tradinglab.universe.UniverseTarget;
import com.tony.tradinglab.universe.ValuationUniverse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("smoke")
@RequiredArgsConstructor
public class RealFundamentalSmokeRunner
        implements CommandLineRunner {

    private final PointInTimeValuationSnapshotService
            pointInTimeValuationSnapshotService;

    private final PointInTimeValuationComparisonService
            pointInTimeValuationComparisonService;

    private final QuantDiscoveryEngine quantDiscoveryEngine;

    private final QuantBacktestSampleFactory
            quantBacktestSampleFactory;

    private final QuantBacktestService
            quantBacktestService;

    private final QuantBacktestAnalyzer
            quantBacktestAnalyzer;

    private final QuantScoreQuintileAnalyzer
            quantScoreQuintileAnalyzer;

    @Override
    public void run(
            String... args
    ) {


        /*
         * 기존 상세 valuation comparison은
         * 가장 최근 검증 시점으로 계속 수행한다.
         */
        runValuationComparison(
                LocalDate.of(
                        2026,
                        7,
                        31
                )
        );

        testQuantDiscovery();

//        testQuantBacktestScoreBuckets();

//        testQuantBacktestTotalQuintiles();

        testQuantBacktestComponentQuintiles();

    }

    private void runValuationComparison(
            LocalDate observationDate
    ) {

        List<UniverseTarget> targets =
                ValuationUniverse.targets();


        List<ValuationPeerSnapshotInput> inputs =
                new ArrayList<>();


        List<String> snapshotFailures =
                new ArrayList<>();


        for (UniverseTarget target : targets) {

            try {

                pointInTimeValuationSnapshotService
                        .analyze(
                                target.symbol(),
                                target.exchange(),
                                observationDate
                        )
                        .ifPresentOrElse(
                                inputs::add,
                                () ->
                                        snapshotFailures.add(
                                                target.symbol()
                                                        + " -> snapshot unavailable"
                                        )
                        );

            } catch (RuntimeException e) {

                snapshotFailures.add(
                        target.symbol()
                                + " -> "
                                + e.getMessage()
                );
            }
        }


        Set<String> createdSymbols =
                inputs.stream()
                        .map(
                                ValuationPeerSnapshotInput::symbol
                        )
                        .collect(
                                Collectors.toSet()
                        );


        List<String> missingSymbols =
                targets.stream()
                        .map(
                                UniverseTarget::symbol
                        )
                        .filter(
                                symbol ->
                                        !createdSymbols.contains(
                                                symbol
                                        )
                        )
                        .toList();


        ValuationPeerSnapshotInput target =
                inputs.stream()
                        .filter(
                                input ->
                                        "NVDA".equals(
                                                input.symbol()
                                        )
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "NVDA valuation snapshot not found."
                                        )
                        );


        ValuationComparisonResult result =
                pointInTimeValuationComparisonService
                        .compare(
                                target.stockId(),
                                observationDate,
                                inputs
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Valuation comparison could not be created."
                                        )
                        );


        PercentileValuationAssessment percentile =
                result.percentile();

        RobustZScoreValuationAssessment robustZScore =
                result.robustZScore();

        RegressionAdjustedValuationAssessment regression =
                result.regressionAdjusted();


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " VALUATION COMPARISON"
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "Universe Target Count = "
                        + targets.size()
        );

        System.out.println(
                "Snapshot Created Count = "
                        + inputs.size()
        );


        if (!missingSymbols.isEmpty()) {

            System.out.println(
                    "Snapshot Missing = "
                            + missingSymbols
            );
        }


        if (!snapshotFailures.isEmpty()) {

            System.out.println(
                    "Snapshot Failures:"
            );

            snapshotFailures.forEach(
                    failure ->
                            System.out.println(
                                    " - " + failure
                            )
            );
        }


        System.out.println();
        System.out.println(
                "Symbol = "
                        + result.symbol()
        );

        System.out.println(
                "Price Date = "
                        + result.priceDate()
        );

        System.out.println(
                "Peer Selection Level = "
                        + result.peerSelectionLevel()
        );

        System.out.println(
                "Peer Count = "
                        + result.peerCount()
        );


        System.out.println();
        System.out.println(
                "----- Percentile -----"
        );

        System.out.println(
                "P/E Percentile = "
                        + percentile.pePercentile()
                        + " | peers="
                        + percentile.validPePeerCount()
        );

        System.out.println(
                "P/S Percentile = "
                        + percentile.psPercentile()
                        + " | peers="
                        + percentile.validPsPeerCount()
        );

        System.out.println(
                "P/FCF Percentile = "
                        + percentile.priceToFcfPercentile()
                        + " | peers="
                        + percentile.validPriceToFcfPeerCount()
        );


        System.out.println();
        System.out.println(
                "----- Robust Z-Score -----"
        );

        System.out.println(
                "P/E = "
                        + robustZScore.pe()
        );

        System.out.println(
                "P/S = "
                        + robustZScore.ps()
        );

        System.out.println(
                "P/FCF = "
                        + robustZScore.priceToFcf()
        );


        System.out.println();
        System.out.println(
                "----- Regression Adjusted -----"
        );


        if (regression == null) {

            System.out.println(
                    "Regression = unavailable"
            );

        } else {

            System.out.println(
                    "Metric = "
                            + regression.metric()
            );

            System.out.println(
                    "Actual Multiple = "
                            + regression.actualMultiple()
            );

            System.out.println(
                    "Predicted Multiple = "
                            + regression.predictedMultiple()
            );

            System.out.println(
                    "Residual = "
                            + regression.residual()
            );

            System.out.println(
                    "Relative Deviation = "
                            + regression.relativeDeviationPct()
                            + "%"
            );

            System.out.println(
                    "R² = "
                            + regression.rSquared()
            );

            System.out.println(
                    "Regression Peer Count = "
                            + regression.peerCount()
            );
        }


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " END"
        );

        System.out.println(
                "======================================"
        );
    }
    private void testQuantDiscovery() {

        LocalDate observationDate =
                LocalDate.of(
                        2026,
                        7,
                        31
                );


        List<UniverseTarget> targets =
                ValuationUniverse.targets();


        List<QuantDiscoveryCandidate> candidates =
                quantDiscoveryEngine.discover(
                        observationDate
                );


        Set<String> candidateSymbols =
                candidates.stream()
                        .map(
                                QuantDiscoveryCandidate::symbol
                        )
                        .collect(
                                Collectors.toSet()
                        );


        List<String> missingSymbols =
                targets.stream()
                        .map(
                                UniverseTarget::symbol
                        )
                        .filter(
                                symbol ->
                                        !candidateSymbols.contains(
                                                symbol
                                        )
                        )
                        .toList();


        boolean invalidObservationDate =
                candidates.stream()
                        .anyMatch(
                                candidate ->
                                        !observationDate.equals(
                                                candidate.observationDate()
                                        )
                        );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " QUANT DISCOVERY"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Observation Date = "
                        + observationDate
        );

        System.out.println(
                "Universe Count = "
                        + targets.size()
        );

        System.out.println(
                "Candidate Count = "
                        + candidates.size()
        );


        if (!missingSymbols.isEmpty()) {

            System.out.println(
                    "Missing Symbols = "
                            + missingSymbols
            );
        }


        if (invalidObservationDate) {

            throw new IllegalStateException(
                    "Quant Discovery candidate에 잘못된 observationDate가 포함되어 있습니다."
            );
        }


        if (candidates.size()
                != targets.size()) {

            throw new IllegalStateException(
                    "Quant Discovery candidate count mismatch. "
                            + "expected="
                            + targets.size()
                            + ", actual="
                            + candidates.size()
            );
        }


        if (candidateSymbols.size()
                != candidates.size()) {

            throw new IllegalStateException(
                    "Quant Discovery candidate에 중복 symbol이 존재합니다."
            );
        }


        System.out.println();
        System.out.println(
                "----- SELECTED SCORES -----"
        );


        List<String> selectedSymbols =
                List.of(
                        "NVDA",
                        "MSFT",
                        "AAPL"
                );


        candidates.stream()

                .filter(
                        candidate ->
                                selectedSymbols.contains(
                                        candidate.symbol()
                                )
                )

                .forEach(
                        candidate -> {

                            QuantScoreBreakdown score =
                                    candidate.score();


                            System.out.println(
                                    candidate.symbol()
                                            + " | Growth="
                                            + score.growthScore()
                                            + " | Quality="
                                            + score.qualityScore()
                                            + " | Valuation="
                                            + score.valuationScore()
                                            + " | Total="
                                            + score.totalScore()
                            );
                        }
                );

        System.out.println();
        System.out.println(
                "----- TOP 10 TOTAL SCORE -----"
        );


        candidates.stream()

                .sorted(
                        (first, second) ->
                                second.score()
                                        .totalScore()
                                        .compareTo(
                                                first.score()
                                                        .totalScore()
                                        )
                )

                .limit(10)

                .forEach(
                        candidate -> {

                            QuantScoreBreakdown score =
                                    candidate.score();


                            System.out.println(
                                    candidate.symbol()
                                            + " | Total="
                                            + score.totalScore()
                                            + " | Growth="
                                            + score.growthScore()
                                            + " | Quality="
                                            + score.qualityScore()
                                            + " | Valuation="
                                            + score.valuationScore()
                            );
                        }
                );



        System.out.println();
        System.out.println(
                "Quant Discovery Validation = SUCCESS"
        );

        System.out.println(
                "======================================"
        );
    }

    private void testQuantBacktestSample() {

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        List<UniverseTarget> targets =
                ValuationUniverse.targets();


        List<QuantDiscoveryCandidate> candidates =
                quantDiscoveryEngine.discover(
                        observationDate
                );


        List<QuantBacktestSample> samples =
                new ArrayList<>();


        for (QuantDiscoveryCandidate candidate : candidates) {

            UniverseTarget target =
                    targets.stream()

                            .filter(
                                    item ->
                                            item.symbol()
                                                    .equals(
                                                            candidate.symbol()
                                                    )
                            )

                            .findFirst()

                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Universe target not found. symbol="
                                                            + candidate.symbol()
                                            )
                            );


            quantBacktestSampleFactory
                    .create(
                            candidate,
                            target.exchange()
                    )
                    .ifPresent(
                            samples::add
                    );
        }


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " QUANT BACKTEST SAMPLE"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Observation Date = "
                        + observationDate
        );

        System.out.println(
                "Candidate Count = "
                        + candidates.size()
        );

        System.out.println(
                "Sample Count = "
                        + samples.size()
        );


        System.out.println();
        System.out.println(
                "----- SELECTED BACKTEST SAMPLES -----"
        );


        List<String> selectedSymbols =
                List.of(
                        "NVDA",
                        "MU",
                        "SMCI",
                        "AAPL",
                        "MSFT"
                );


        samples.stream()

                .filter(
                        sample ->
                                selectedSymbols.contains(
                                        sample.symbol()
                                )
                )

                .forEach(
                        sample -> {

                            System.out.println();

                            System.out.println(
                                    sample.symbol()
                            );

                            System.out.println(
                                    "Score"
                                            + " | Growth="
                                            + sample.score()
                                            .growthScore()
                                            + " | Quality="
                                            + sample.score()
                                            .qualityScore()
                                            + " | Valuation="
                                            + sample.score()
                                            .valuationScore()
                                            + " | Total="
                                            + sample.score()
                                            .totalScore()
                            );

                            System.out.println(
                                    "Entry"
                                            + " | Date="
                                            + sample.entryPriceDate()
                                            + " | Price="
                                            + sample.entryPrice()
                            );

                            System.out.println(
                                    "3M"
                                            + " | Date="
                                            + sample.threeMonthPriceDate()
                                            + " | Price="
                                            + sample.threeMonthPrice()
                                            + " | Return="
                                            + sample.threeMonthReturnPct()
                                            + "%"
                            );

                            System.out.println(
                                    "6M"
                                            + " | Date="
                                            + sample.sixMonthPriceDate()
                                            + " | Price="
                                            + sample.sixMonthPrice()
                                            + " | Return="
                                            + sample.sixMonthReturnPct()
                                            + "%"
                            );

                            System.out.println(
                                    "12M"
                                            + " | Date="
                                            + sample.twelveMonthPriceDate()
                                            + " | Price="
                                            + sample.twelveMonthPrice()
                                            + " | Return="
                                            + sample.twelveMonthReturnPct()
                                            + "%"
                            );
                        }
                );


        System.out.println();
        System.out.println(
                "Quant Backtest Sample Validation = SUCCESS"
        );

        System.out.println(
                "======================================"
        );
    }

    private void testQuantBacktestDataset() {

        List<LocalDate> observationDates =
                List.of(
                        LocalDate.of(
                                2024,
                                12,
                                31
                        ),
                        LocalDate.of(
                                2025,
                                3,
                                31
                        ),
                        LocalDate.of(
                                2025,
                                6,
                                30
                        )
                );


        List<QuantBacktestSample> samples =
                quantBacktestService.run(
                        observationDates
                );


        int expectedSampleCount =
                observationDates.size()
                        * ValuationUniverse.targets()
                        .size();


        long threeMonthAvailableCount =
                samples.stream()

                        .filter(
                                sample ->
                                        sample.threeMonthReturnPct()
                                                != null
                        )

                        .count();


        long sixMonthAvailableCount =
                samples.stream()

                        .filter(
                                sample ->
                                        sample.sixMonthReturnPct()
                                                != null
                        )

                        .count();


        long twelveMonthAvailableCount =
                samples.stream()

                        .filter(
                                sample ->
                                        sample.twelveMonthReturnPct()
                                                != null
                        )

                        .count();


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " QUANT BACKTEST DATASET"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Observation Date Count = "
                        + observationDates.size()
        );

        System.out.println(
                "Expected Sample Count = "
                        + expectedSampleCount
        );

        System.out.println(
                "Actual Sample Count = "
                        + samples.size()
        );


        System.out.println();
        System.out.println(
                "----- FUTURE RETURN AVAILABILITY -----"
        );

        System.out.println(
                "3M Available = "
                        + threeMonthAvailableCount
                        + " / "
                        + samples.size()
        );

        System.out.println(
                "6M Available = "
                        + sixMonthAvailableCount
                        + " / "
                        + samples.size()
        );

        System.out.println(
                "12M Available = "
                        + twelveMonthAvailableCount
                        + " / "
                        + samples.size()
        );


        System.out.println();
        System.out.println(
                "----- SAMPLE COUNT BY DATE -----"
        );


        for (LocalDate observationDate
                : observationDates) {

            long count =
                    samples.stream()

                            .filter(
                                    sample ->
                                            observationDate.equals(
                                                    sample.observationDate()
                                            )
                            )

                            .count();


            System.out.println(
                    observationDate
                            + " = "
                            + count
            );
        }


        if (samples.size()
                != expectedSampleCount) {

            throw new IllegalStateException(
                    "Quant backtest sample count mismatch. "
                            + "expected="
                            + expectedSampleCount
                            + ", actual="
                            + samples.size()
            );
        }


        if (threeMonthAvailableCount
                != samples.size()) {

            throw new IllegalStateException(
                    "3M future return is missing. "
                            + "available="
                            + threeMonthAvailableCount
                            + ", total="
                            + samples.size()
            );
        }


        if (sixMonthAvailableCount
                != samples.size()) {

            throw new IllegalStateException(
                    "6M future return is missing. "
                            + "available="
                            + sixMonthAvailableCount
                            + ", total="
                            + samples.size()
            );
        }


        if (twelveMonthAvailableCount
                != samples.size()) {

            throw new IllegalStateException(
                    "12M future return is missing. "
                            + "available="
                            + twelveMonthAvailableCount
                            + ", total="
                            + samples.size()
            );
        }


        System.out.println();
        System.out.println(
                "Quant Backtest Dataset Validation = SUCCESS"
        );

        System.out.println(
                "======================================"
        );
    }

    private void testQuantBacktestScoreBuckets() {

        List<LocalDate> observationDates =
                List.of(
                        LocalDate.of(
                                2024,
                                12,
                                31
                        ),
                        LocalDate.of(
                                2025,
                                3,
                                31
                        ),
                        LocalDate.of(
                                2025,
                                6,
                                30
                        )
                );


        List<QuantBacktestSample> samples =
                quantBacktestService.run(
                        observationDates
                );


        List<QuantBacktestAnalyzer.BucketStats> bucketStats =
                quantBacktestAnalyzer
                        .analyzeByTotalScore(
                                samples
                        );


        int bucketSampleCount =
                bucketStats.stream()

                        .mapToInt(
                                QuantBacktestAnalyzer.BucketStats::sampleCount
                        )

                        .sum();


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " QUANT BACKTEST SCORE BUCKETS"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Observation Dates = "
                        + observationDates
        );

        System.out.println(
                "Total Samples = "
                        + samples.size()
        );


        System.out.println();
        System.out.println(
                "----- TOTAL SCORE BUCKET PERFORMANCE -----"
        );


        for (QuantBacktestAnalyzer.BucketStats stats
                : bucketStats) {

            String range =
                    stats.maxScore() == 100
                            ? "["
                            + stats.minScore()
                            + ", "
                            + stats.maxScore()
                            + "]"
                            : "["
                            + stats.minScore()
                            + ", "
                            + stats.maxScore()
                            + ")";


            System.out.println();

            System.out.println(
                    "Score "
                            + range
                            + " | Samples="
                            + stats.sampleCount()
            );


            System.out.println(
                    "3M"
                            + " | Avg="
                            + stats.averageThreeMonthReturnPct()
                            + "%"
                            + " | Median="
                            + stats.medianThreeMonthReturnPct()
                            + "%"
            );


            System.out.println(
                    "6M"
                            + " | Avg="
                            + stats.averageSixMonthReturnPct()
                            + "%"
                            + " | Median="
                            + stats.medianSixMonthReturnPct()
                            + "%"
            );


            System.out.println(
                    "12M"
                            + " | Avg="
                            + stats.averageTwelveMonthReturnPct()
                            + "%"
                            + " | Median="
                            + stats.medianTwelveMonthReturnPct()
                            + "%"
            );
        }


        /*
         * 모든 sample이 정확히 하나의
         * Total Score bucket에 들어갔는지 검증.
         */
        if (bucketSampleCount
                != samples.size()) {

            throw new IllegalStateException(
                    "Quant backtest bucket sample count mismatch. "
                            + "samples="
                            + samples.size()
                            + ", buckets="
                            + bucketSampleCount
            );
        }


        System.out.println();
        System.out.println(
                "Quant Backtest Bucket Validation = SUCCESS"
        );

        System.out.println(
                "======================================"
        );
    }

    private void testQuantBacktestTotalQuintiles() {

        List<LocalDate> observationDates =
                List.of(
                        LocalDate.of(
                                2024,
                                12,
                                31
                        ),
                        LocalDate.of(
                                2025,
                                3,
                                31
                        ),
                        LocalDate.of(
                                2025,
                                6,
                                30
                        )
                );


        List<QuantBacktestSample> samples =
                quantBacktestService.run(
                        observationDates
                );


        List<QuantScoreQuintileAnalyzer.QuintileStats> quintiles =
                quantScoreQuintileAnalyzer
                        .analyze(
                                samples,
                                QuantScoreQuintileAnalyzer.ScoreType.TOTAL
                        );


        int quintileSampleCount =
                quintiles.stream()

                        .mapToInt(
                                QuantScoreQuintileAnalyzer.QuintileStats::sampleCount
                        )

                        .sum();


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " QUANT BACKTEST TOTAL SCORE QUINTILES"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Observation Dates = "
                        + observationDates
        );

        System.out.println(
                "Total Samples = "
                        + samples.size()
        );


        System.out.println();
        System.out.println(
                "----- TOTAL SCORE QUINTILE PERFORMANCE -----"
        );


        for (QuantScoreQuintileAnalyzer.QuintileStats stats
                : quintiles) {

            System.out.println();

            System.out.println(
                    "Q"
                            + stats.quintile()
                            + " | Samples="
                            + stats.sampleCount()
                            + " | Score="
                            + stats.minScore()
                            + " ~ "
                            + stats.maxScore()
            );


            System.out.println(
                    "3M"
                            + " | Avg="
                            + stats.averageThreeMonthReturnPct()
                            + "%"
                            + " | Median="
                            + stats.medianThreeMonthReturnPct()
                            + "%"
            );


            System.out.println(
                    "6M"
                            + " | Avg="
                            + stats.averageSixMonthReturnPct()
                            + "%"
                            + " | Median="
                            + stats.medianSixMonthReturnPct()
                            + "%"
            );


            System.out.println(
                    "12M"
                            + " | Avg="
                            + stats.averageTwelveMonthReturnPct()
                            + "%"
                            + " | Median="
                            + stats.medianTwelveMonthReturnPct()
                            + "%"
            );
        }


        if (quintiles.size() != 5) {

            throw new IllegalStateException(
                    "Expected 5 quintiles but got "
                            + quintiles.size()
            );
        }


        if (quintileSampleCount
                != samples.size()) {

            throw new IllegalStateException(
                    "Quant quintile sample count mismatch. "
                            + "samples="
                            + samples.size()
                            + ", quintiles="
                            + quintileSampleCount
            );
        }


        System.out.println();
        System.out.println(
                "Quant Total Quintile Validation = SUCCESS"
        );

        System.out.println(
                "======================================"
        );
    }

    private void testQuantBacktestComponentQuintiles() {

        List<LocalDate> observationDates =
                List.of(
                        LocalDate.of(
                                2024,
                                12,
                                31
                        ),
                        LocalDate.of(
                                2025,
                                3,
                                31
                        ),
                        LocalDate.of(
                                2025,
                                6,
                                30
                        )
                );


        List<QuantBacktestSample> samples =
                quantBacktestService.run(
                        observationDates
                );


        List<QuantScoreQuintileAnalyzer.ScoreType> scoreTypes =
                List.of(
                        QuantScoreQuintileAnalyzer.ScoreType.GROWTH,
                        QuantScoreQuintileAnalyzer.ScoreType.QUALITY,
                        QuantScoreQuintileAnalyzer.ScoreType.VALUATION
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " QUANT COMPONENT QUINTILE ANALYSIS"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Total Samples = "
                        + samples.size()
        );


        for (QuantScoreQuintileAnalyzer.ScoreType scoreType
                : scoreTypes) {

            List<QuantScoreQuintileAnalyzer.QuintileStats> quintiles =
                    quantScoreQuintileAnalyzer
                            .analyze(
                                    samples,
                                    scoreType
                            );


            System.out.println();
            System.out.println(
                    "======================================"
            );

            System.out.println(
                    " "
                            + scoreType
                            + " SCORE QUINTILES"
            );

            System.out.println(
                    "======================================"
            );


            for (QuantScoreQuintileAnalyzer.QuintileStats stats
                    : quintiles) {

                System.out.println();

                System.out.println(
                        "Q"
                                + stats.quintile()
                                + " | Samples="
                                + stats.sampleCount()
                                + " | Score="
                                + stats.minScore()
                                + " ~ "
                                + stats.maxScore()
                );


                System.out.println(
                        "3M"
                                + " | Avg="
                                + stats.averageThreeMonthReturnPct()
                                + "%"
                                + " | Median="
                                + stats.medianThreeMonthReturnPct()
                                + "%"
                );


                System.out.println(
                        "6M"
                                + " | Avg="
                                + stats.averageSixMonthReturnPct()
                                + "%"
                                + " | Median="
                                + stats.medianSixMonthReturnPct()
                                + "%"
                );


                System.out.println(
                        "12M"
                                + " | Avg="
                                + stats.averageTwelveMonthReturnPct()
                                + "%"
                                + " | Median="
                                + stats.medianTwelveMonthReturnPct()
                                + "%"
                );
            }


            int quintileSampleCount =
                    quintiles.stream()

                            .mapToInt(
                                    QuantScoreQuintileAnalyzer
                                            .QuintileStats::sampleCount
                            )

                            .sum();


            if (quintiles.size() != 5) {

                throw new IllegalStateException(
                        scoreType
                                + " expected 5 quintiles but got "
                                + quintiles.size()
                );
            }


            if (quintileSampleCount
                    != samples.size()) {

                throw new IllegalStateException(
                        scoreType
                                + " quintile sample count mismatch. "
                                + "samples="
                                + samples.size()
                                + ", quintiles="
                                + quintileSampleCount
                );
            }
        }


        System.out.println();
        System.out.println(
                "Quant Component Quintile Validation = SUCCESS"
        );

        System.out.println(
                "======================================"
        );
    }
}