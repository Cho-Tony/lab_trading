package com.tony.tradinglab.smoke;

import com.tony.tradinglab.discovery.quant.BacktestDatasetSpec;
import com.tony.tradinglab.discovery.quant.HistoricalStockUniverseService;
import com.tony.tradinglab.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("historical-universe-coverage")
@RequiredArgsConstructor
public class BacktestUniverseCoverageSmokeRunner
        implements CommandLineRunner {

    private final HistoricalStockUniverseService
            historicalStockUniverseService;


    @Override
    public void run(
            String... args
    ) {

        List<LocalDate> observationDates =
                BacktestDatasetSpec.observationDates();


        if (observationDates.size() != 63) {

            throw new IllegalStateException(
                    "Unexpected observation date count: "
                            + observationDates.size()
            );
        }


        int minUniverseCount =
                Integer.MAX_VALUE;

        int maxUniverseCount =
                Integer.MIN_VALUE;

        long totalUniverseCount =
                0;

        Integer previousCount =
                null;


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " BACKTEST UNIVERSE COVERAGE"
        );

        System.out.println(
                "======================================"
        );


        for (
                LocalDate observationDate
                : observationDates
        ) {

            List<Stock> stocks =
                    historicalStockUniverseService
                            .getStocksAsOf(
                                    observationDate
                            );


            int universeCount =
                    stocks.size();


            long activeNowCount =
                    stocks.stream()
                            .filter(
                                    Stock::isActive
                            )
                            .count();


            long delistedLaterCount =
                    universeCount
                            - activeNowCount;


            int delta =
                    previousCount == null
                            ? 0
                            : universeCount
                            - previousCount;


            System.out.printf(
                    "%s | total=%5d | activeNow=%5d | delistedLater=%5d | delta=%+5d%n",
                    observationDate,
                    universeCount,
                    activeNowCount,
                    delistedLaterCount,
                    delta
            );


            minUniverseCount =
                    Math.min(
                            minUniverseCount,
                            universeCount
                    );


            maxUniverseCount =
                    Math.max(
                            maxUniverseCount,
                            universeCount
                    );


            totalUniverseCount +=
                    universeCount;


            previousCount =
                    universeCount;
        }


        double averageUniverseCount =
                totalUniverseCount
                        / (double) observationDates.size();


        System.out.println();
        System.out.println(
                "--------------------------------------"
        );

        System.out.println(
                "Observation Dates = "
                        + observationDates.size()
        );

        System.out.println(
                "First Date        = "
                        + observationDates.get(0)
        );

        System.out.println(
                "Last Date         = "
                        + observationDates.get(
                        observationDates.size() - 1
                )
        );

        System.out.println(
                "Min Universe      = "
                        + minUniverseCount
        );

        System.out.println(
                "Max Universe      = "
                        + maxUniverseCount
        );

        System.out.printf(
                "Average Universe  = %.2f%n",
                averageUniverseCount
        );

        System.out.println(
                "======================================"
        );
    }
}