package com.tony.tradinglab.smoke;

import com.tony.tradinglab.discovery.quant.HistoricalStockUniverseService;
import com.tony.tradinglab.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("historical-universe-smoke")
@RequiredArgsConstructor
public class HistoricalUniverseSmokeRunner
        implements CommandLineRunner {

    private final HistoricalStockUniverseService
            historicalStockUniverseService;


    @Override
    public void run(
            String... args
    ) {

        List<LocalDate> observationDates =
                List.of(
                        LocalDate.of(2010, 3, 31),
                        LocalDate.of(2015, 3, 31),
                        LocalDate.of(2020, 3, 31),
                        LocalDate.of(2025, 3, 31)
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " HISTORICAL UNIVERSE CHECK"
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


            long activeNow =
                    stocks.stream()
                            .filter(
                                    Stock::isActive
                            )
                            .count();


            long currentlyDelisted =
                    stocks.size()
                            - activeNow;


            System.out.println();

            System.out.println(
                    "Observation Date = "
                            + observationDate
            );

            System.out.println(
                    "Universe Count   = "
                            + stocks.size()
            );

            System.out.println(
                    "Still Active Now = "
                            + activeNow
            );

            System.out.println(
                    "Delisted Later   = "
                            + currentlyDelisted
            );


            System.out.println(
                    "Sample:"
            );


            stocks.stream()
                    .limit(10)
                    .forEach(
                            stock ->
                                    System.out.println(
                                            "  "
                                                    + stock.getSymbol()
                                                    + " | "
                                                    + stock.getExchange()
                                                    + " | start="
                                                    + stock.getListingStartDate()
                                                    + " | end="
                                                    + stock.getListingEndDate()
                                                    + " | activeNow="
                                                    + stock.isActive()
                                    )
                    );
        }


        System.out.println();
        System.out.println(
                "======================================"
        );
    }
}