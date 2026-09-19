package com.tony.tradinglab.smoke;

import com.tony.tradinglab.fundamental.client.FundamentalDataClient;
import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.fundamental.service.FundamentalUniverseSyncService;
import com.tony.tradinglab.fundamental.service.PointInTimeValuationSnapshotService;
import com.tony.tradinglab.fundamental.service.ValuationPeerSnapshotAssembler;
import com.tony.tradinglab.marketdata.service.MarketPriceUniverseSyncService;
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

    private final FundamentalUniverseSyncService fundamentalUniverseSyncService;

    private final FundamentalDataClient fundamentalDataClient;

    private final MarketPriceUniverseSyncService marketPriceUniverseSyncService;

    private final PointInTimeValuationSnapshotService pointInTimeValuationSnapshotService;

    private final ValuationPeerSnapshotAssembler valuationPeerSnapshotAssembler;

    @Override
    public void run(
            String... args
    ) {

//        syncFundamentalUniverse();
//        diagnoseNvdaFundamental();

//        syncPriceUniverse();

//        testValuationUniverse();

        testPeerSnapshotAssembly();


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


    private void syncFundamentalUniverse() {

        List<FundamentalUniverseSyncService.Target> targets =
                List.of(

                        new FundamentalUniverseSyncService.Target(
                                "AAPL",
                                "Apple Inc.",
                                "NASDAQ",
                                "US",
                                "USD"
                        ),

                        new FundamentalUniverseSyncService.Target(
                                "MSFT",
                                "Microsoft Corporation",
                                "NASDAQ",
                                "US",
                                "USD"
                        ),

                        new FundamentalUniverseSyncService.Target(
                                "NVDA",
                                "NVIDIA Corporation",
                                "NASDAQ",
                                "US",
                                "USD"
                        ),

                        new FundamentalUniverseSyncService.Target(
                                "AVGO",
                                "Broadcom Inc.",
                                "NASDAQ",
                                "US",
                                "USD"
                        )
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " FUNDAMENTAL UNIVERSE SYNC"
        );

        System.out.println(
                "======================================"
        );


        List<FundamentalUniverseSyncService.SyncResult> results =
                fundamentalUniverseSyncService.sync(
                        targets
                );


        for (FundamentalUniverseSyncService.SyncResult result : results) {

            System.out.println(
                    result.symbol()
                            + " | stockId="
                            + result.stockId()
                            + " | exchange="
                            + result.exchange()
                            + " | inserted="
                            + result.insertedStatements()
            );
        }


        System.out.println(
                "======================================"
        );
    }

    private void diagnoseNvdaFundamental() {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " NVDA FUNDAMENTAL DIAGNOSTIC"
        );

        System.out.println(
                "======================================"
        );


        List<FinancialStatementData> statements =
                fundamentalDataClient
                        .getFinancialStatements(
                                "NVDA"
                        );


        System.out.println(
                "FinancialStatementData count : "
                        + statements.size()
        );


        statements.stream()
                .skip(
                        Math.max(
                                0,
                                statements.size() - 8
                        )
                )
                .forEach(
                        data -> {

                            System.out.println();

                            System.out.println(
                                    data.fiscalYear()
                                            + " "
                                            + data.fiscalQuarter()
                                            + " | period="
                                            + data.periodEndDate()
                                            + " | filed="
                                            + data.filedDate()
                            );

                            System.out.println(
                                    "Revenue = "
                                            + data.revenue()
                            );

                            System.out.println(
                                    "Operating Income = "
                                            + data.operatingIncome()
                            );

                            System.out.println(
                                    "Net Income = "
                                            + data.netIncome()
                            );
                        }
                );


        System.out.println(
                "======================================"
        );
    }

    private void syncPriceUniverse() {

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        1,
                        1
                );

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        7,
                        31
                );


        List<MarketPriceUniverseSyncService.Target> targets =
                List.of(

                        new MarketPriceUniverseSyncService.Target(
                                "AAPL",
                                "NASDAQ"
                        ),

                        new MarketPriceUniverseSyncService.Target(
                                "MSFT",
                                "NASDAQ"
                        ),

                        new MarketPriceUniverseSyncService.Target(
                                "NVDA",
                                "NASDAQ"
                        ),

                        new MarketPriceUniverseSyncService.Target(
                                "AVGO",
                                "NASDAQ"
                        )
                );


        List<MarketPriceUniverseSyncService.SyncResult> results =
                marketPriceUniverseSyncService.sync(
                        targets,
                        startDate,
                        endDate
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " PRICE UNIVERSE SYNC"
        );

        System.out.println(
                "======================================"
        );


        for (MarketPriceUniverseSyncService.SyncResult result
                : results) {

            System.out.println(
                    result.symbol()
                            + " | stockId="
                            + result.stockId()
                            + " | exchange="
                            + result.exchange()
                            + " | inserted="
                            + result.insertedPrices()
            );
        }


        System.out.println(
                "======================================"
        );
    }

    private void testValuationUniverse() {

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


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " VALUATION UNIVERSE"
        );

        System.out.println(
                " Observation Date = "
                        + observationDate
        );

        System.out.println(
                "======================================"
        );


        for (String symbol : symbols) {

            Optional<ValuationPeerSnapshotInput> result =
                    pointInTimeValuationSnapshotService
                            .analyze(
                                    symbol,
                                    "NASDAQ",
                                    observationDate
                            );


            System.out.println();

            System.out.println(
                    "--------------------------------------"
            );

            System.out.println(
                    symbol
            );

            System.out.println(
                    "--------------------------------------"
            );


            if (result.isEmpty()) {

                System.out.println(
                        "SNAPSHOT = EMPTY"
                );

                continue;
            }


            ValuationPeerSnapshotInput snapshot =
                    result.get();


            System.out.println(
                    "Stock ID = "
                            + snapshot.stockId()
            );

            System.out.println(
                    "Observation Date = "
                            + snapshot.observationDate()
            );

            System.out.println(
                    "Price Date = "
                            + snapshot.valuation()
                            .priceDate()
            );

            System.out.println(
                    "Share Price = "
                            + snapshot.valuation()
                            .sharePrice()
            );

            System.out.println(
                    "Market Cap = "
                            + snapshot.valuation()
                            .marketCap()
            );

            System.out.println(
                    "P/E = "
                            + snapshot.valuation()
                            .peRatio()
            );

            System.out.println(
                    "P/S = "
                            + snapshot.valuation()
                            .psRatio()
            );

            System.out.println(
                    "P/FCF = "
                            + snapshot.valuation()
                            .priceToFcfRatio()
            );

            System.out.println(
                    "Growth = "
                            + snapshot.growth()
                            .trend()
            );

            System.out.println(
                    "Latest Revenue YoY = "
                            + snapshot.growth()
                            .latestYoyGrowthPct()
            );

            System.out.println(
                    "Operating Margin = "
                            + snapshot.profitability()
                            .latestOperatingMarginPct()
            );

            System.out.println(
                    "Net Margin = "
                            + snapshot.profitability()
                            .latestNetMarginPct()
            );
        }


        System.out.println();

        System.out.println(
                "======================================"
        );


    }

    private void testPeerSnapshotAssembly() {

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

                        .flatMap(
                                Optional::stream
                        )

                        .toList();


        List<ValuationPeerSnapshot> snapshots =
                valuationPeerSnapshotAssembler
                        .assemble(
                                inputs
                        );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " PEER SNAPSHOT ASSEMBLY"
        );

        System.out.println(
                " Observation Date = "
                        + observationDate
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "Input Count = "
                        + inputs.size()
        );

        System.out.println(
                "Snapshot Count = "
                        + snapshots.size()
        );


        for (ValuationPeerSnapshot snapshot
                : snapshots) {

            System.out.println();

            System.out.println(
                    "--------------------------------------"
            );

            System.out.println(
                    snapshot.symbol()
            );

            System.out.println(
                    "--------------------------------------"
            );

            System.out.println(
                    "Stock ID = "
                            + snapshot.stockId()
            );

            System.out.println(
                    "Sector = "
                            + snapshot.sector()
            );

            System.out.println(
                    "Industry = "
                            + snapshot.industry()
            );

            System.out.println(
                    "Market Cap = "
                            + snapshot.valuation()
                            .marketCap()
            );

            System.out.println(
                    "P/E = "
                            + snapshot.valuation()
                            .peRatio()
            );

            System.out.println(
                    "P/S = "
                            + snapshot.valuation()
                            .psRatio()
            );

            System.out.println(
                    "P/FCF = "
                            + snapshot.valuation()
                            .priceToFcfRatio()
            );
        }


        System.out.println();
        System.out.println(
                "======================================"
        );
    }
}