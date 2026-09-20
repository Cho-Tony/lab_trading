package com.tony.tradinglab.smoke;

import com.tony.tradinglab.fundamental.service.FundamentalUniverseSyncService;
import com.tony.tradinglab.fundamental.service.ValuationUniverseSyncService;
import com.tony.tradinglab.marketdata.service.MarketPriceUniverseSyncService;
import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.service.StockClassificationUniverseSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("valuation-universe-sync")
@RequiredArgsConstructor
public class ValuationUniverseSyncRunner
        implements CommandLineRunner {

    private final ValuationUniverseSyncService
            valuationUniverseSyncService;


    @Value("${valuation-universe.classification-effective-from}")
    private LocalDate classificationEffectiveFrom;

    @Value("${valuation-universe.classification-source}")
    private String classificationSource;

    @Value("${valuation-universe.price-start-date}")
    private LocalDate priceStartDate;

    @Value("${valuation-universe.price-end-date}")
    private LocalDate priceEndDate;


    @Override
    public void run(
            String... args
    ) {

        ClassificationSource source =
                ClassificationSource.valueOf(
                        classificationSource
                                .trim()
                                .toUpperCase()
                );


        ValuationUniverseSyncService.SyncResult result =
                valuationUniverseSyncService.sync(
                        classificationEffectiveFrom,
                        source,
                        priceStartDate,
                        priceEndDate
                );


        printResult(
                result
        );
    }


    private void printResult(
            ValuationUniverseSyncService.SyncResult result
    ) {

        List<FundamentalUniverseSyncService.SyncResult>
                fundamentalResults =
                result.fundamentals();

        List<StockClassificationUniverseSyncService.SyncResult>
                classificationResults =
                result.classifications();

        List<MarketPriceUniverseSyncService.SyncResult>
                priceResults =
                result.prices();


        long fundamentalSuccess =
                fundamentalResults.stream()
                        .filter(
                                sync ->
                                        sync.status()
                                                == FundamentalUniverseSyncService
                                                .SyncStatus.SUCCESS
                        )
                        .count();


        long fundamentalFailed =
                fundamentalResults.stream()
                        .filter(
                                sync ->
                                        sync.status()
                                                == FundamentalUniverseSyncService
                                                .SyncStatus.FAILED
                        )
                        .count();


        int insertedStatements =
                fundamentalResults.stream()
                        .mapToInt(
                                FundamentalUniverseSyncService
                                        .SyncResult::insertedStatements
                        )
                        .sum();


        long classificationInserted =
                classificationResults.stream()
                        .filter(
                                sync ->
                                        sync.status()
                                                == StockClassificationUniverseSyncService
                                                .SyncStatus.INSERTED
                        )
                        .count();


        long classificationChanged =
                classificationResults.stream()
                        .filter(
                                sync ->
                                        sync.status()
                                                == StockClassificationUniverseSyncService
                                                .SyncStatus.CHANGED
                        )
                        .count();


        long classificationUnchanged =
                classificationResults.stream()
                        .filter(
                                sync ->
                                        sync.status()
                                                == StockClassificationUniverseSyncService
                                                .SyncStatus.UNCHANGED
                        )
                        .count();


        long classificationFailed =
                classificationResults.stream()
                        .filter(
                                sync ->
                                        sync.status()
                                                == StockClassificationUniverseSyncService
                                                .SyncStatus.FAILED
                        )
                        .count();


        long priceSuccess =
                priceResults.stream()
                        .filter(
                                sync ->
                                        sync.status()
                                                == MarketPriceUniverseSyncService
                                                .SyncStatus.SUCCESS
                        )
                        .count();


        long priceFailed =
                priceResults.stream()
                        .filter(
                                sync ->
                                        sync.status()
                                                == MarketPriceUniverseSyncService
                                                .SyncStatus.FAILED
                        )
                        .count();


        int insertedPrices =
                priceResults.stream()
                        .mapToInt(
                                MarketPriceUniverseSyncService
                                        .SyncResult::insertedPrices
                        )
                        .sum();


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " VALUATION UNIVERSE SYNC"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Target Count = "
                        + result.targetCount()
        );


        System.out.println();
        System.out.println(
                "----- Fundamental -----"
        );

        System.out.println(
                "Success = "
                        + fundamentalSuccess
        );

        System.out.println(
                "Failed = "
                        + fundamentalFailed
        );

        System.out.println(
                "Inserted Statements = "
                        + insertedStatements
        );


        System.out.println();
        System.out.println(
                "----- Classification -----"
        );

        System.out.println(
                "Inserted = "
                        + classificationInserted
        );

        System.out.println(
                "Changed = "
                        + classificationChanged
        );

        System.out.println(
                "Unchanged = "
                        + classificationUnchanged
        );

        System.out.println(
                "Failed = "
                        + classificationFailed
        );


        System.out.println();
        System.out.println(
                "----- Price -----"
        );

        System.out.println(
                "Success = "
                        + priceSuccess
        );

        System.out.println(
                "Failed = "
                        + priceFailed
        );

        System.out.println(
                "Inserted Prices = "
                        + insertedPrices
        );


        printFailedDetails(
                fundamentalResults,
                classificationResults,
                priceResults
        );


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


    private void printFailedDetails(
            List<FundamentalUniverseSyncService.SyncResult>
                    fundamentalResults,

            List<StockClassificationUniverseSyncService.SyncResult>
                    classificationResults,

            List<MarketPriceUniverseSyncService.SyncResult>
                    priceResults
    ) {

        boolean hasFailure =
                fundamentalResults.stream()
                        .anyMatch(
                                sync ->
                                        sync.status()
                                                == FundamentalUniverseSyncService
                                                .SyncStatus.FAILED
                        )

                        || classificationResults.stream()
                        .anyMatch(
                                sync ->
                                        sync.status()
                                                == StockClassificationUniverseSyncService
                                                .SyncStatus.FAILED
                        )

                        || priceResults.stream()
                        .anyMatch(
                                sync ->
                                        sync.status()
                                                == MarketPriceUniverseSyncService
                                                .SyncStatus.FAILED
                        );


        if (!hasFailure) {

            return;
        }


        System.out.println();
        System.out.println(
                "----- Failed Details -----"
        );


        fundamentalResults.stream()
                .filter(
                        sync ->
                                sync.status()
                                        == FundamentalUniverseSyncService
                                        .SyncStatus.FAILED
                )
                .forEach(
                        sync ->
                                System.out.println(
                                        "[FUNDAMENTAL] "
                                                + sync.symbol()
                                                + " / "
                                                + sync.exchange()
                                                + " -> "
                                                + sync.errorMessage()
                                )
                );


        classificationResults.stream()
                .filter(
                        sync ->
                                sync.status()
                                        == StockClassificationUniverseSyncService
                                        .SyncStatus.FAILED
                )
                .forEach(
                        sync ->
                                System.out.println(
                                        "[CLASSIFICATION] "
                                                + sync.symbol()
                                                + " / "
                                                + sync.exchange()
                                                + " -> "
                                                + sync.errorMessage()
                                )
                );


        priceResults.stream()
                .filter(
                        sync ->
                                sync.status()
                                        == MarketPriceUniverseSyncService
                                        .SyncStatus.FAILED
                )
                .forEach(
                        sync ->
                                System.out.println(
                                        "[PRICE] "
                                                + sync.symbol()
                                                + " / "
                                                + sync.exchange()
                                                + " -> "
                                                + sync.errorMessage()
                                )
                );
    }
}