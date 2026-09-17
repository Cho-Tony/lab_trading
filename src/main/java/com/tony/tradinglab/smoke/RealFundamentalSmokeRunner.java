package com.tony.tradinglab.smoke;

import com.tony.tradinglab.fundamental.client.FundamentalDataClient;
import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.service.FundamentalUniverseSyncService;
import com.tony.tradinglab.marketdata.service.MarketPriceUniverseSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("smoke")
@RequiredArgsConstructor
public class RealFundamentalSmokeRunner
        implements CommandLineRunner {

    private final FundamentalUniverseSyncService fundamentalUniverseSyncService;

    private final FundamentalDataClient fundamentalDataClient;

    private final MarketPriceUniverseSyncService marketPriceUniverseSyncService;

    @Override
    public void run(
            String... args
    ) {

//        syncFundamentalUniverse();
//        diagnoseNvdaFundamental();

        syncPriceUniverse();

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
}