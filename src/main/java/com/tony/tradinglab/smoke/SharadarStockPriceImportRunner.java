package com.tony.tradinglab.smoke;

import com.tony.tradinglab.stock.ingest.sharadar.SharadarStockPriceImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sharadar-price-import")
@RequiredArgsConstructor
public class SharadarStockPriceImportRunner
        implements CommandLineRunner {

    private final SharadarStockPriceImportService importService;


    @Override
    public void run(
            String... args
    ) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " SHARADAR STOCK PRICE IMPORT"
        );

        System.out.println(
                "======================================"
        );


        SharadarStockPriceImportService.ImportResult result =
                importService.importAll();


        System.out.println();

        System.out.println(
                "Source Rows            = "
                        + result.sourceRows()
        );

        System.out.println(
                "Written Rows           = "
                        + result.writtenRows()
        );

        System.out.println(
                "Outside Range Rows     = "
                        + result.outsideRangeRows()
        );

        System.out.println(
                "Ticker Not In Universe = "
                        + result.tickerNotInUniverseRows()
        );


        System.out.println(
                "======================================"
        );
    }
}