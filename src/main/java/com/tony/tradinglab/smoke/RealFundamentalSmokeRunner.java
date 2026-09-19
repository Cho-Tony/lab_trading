package com.tony.tradinglab.smoke;

import com.tony.tradinglab.fundamental.domain.PercentileValuationAssessment;
import com.tony.tradinglab.fundamental.domain.RegressionAdjustedValuationAssessment;
import com.tony.tradinglab.fundamental.domain.RobustZScoreValuationAssessment;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.fundamental.service.PointInTimeValuationComparisonService;
import com.tony.tradinglab.fundamental.service.PointInTimeValuationSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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


        List<String> symbols =
                List.of(
                        "AAPL",
                        "MSFT",
                        "NVDA",
                        "AVGO"
                );


        List<ValuationPeerSnapshotInput> inputs =
                symbols.stream()
                        .map(
                                symbol ->
                                        pointInTimeValuationSnapshotService
                                                .analyze(
                                                        symbol,
                                                        "NASDAQ",
                                                        observationDate
                                                )
                        )
                        .flatMap(Optional::stream)
                        .toList();


        if (inputs.size() != symbols.size()) {

            throw new IllegalStateException(
                    "Some valuation snapshots could not be created."
            );
        }


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