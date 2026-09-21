package com.tony.tradinglab.smoke;

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
import java.util.Optional;
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


    @Override
    public void run(
            String... args
    ) {

        LocalDate observationDate =
                LocalDate.of(
                        2026,
                        7,
                        31
                );


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
}