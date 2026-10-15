package com.tony.tradinglab.smoke;

import com.tony.tradinglab.stock.ingest.sharadar.SharadarFundamentalImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sharadar-fundamental-import")
@RequiredArgsConstructor
public class SharadarFundamentalImportRunner
        implements CommandLineRunner {

    private final SharadarFundamentalImportService importService;


    @Override
    public void run(
            String... args
    ) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " SHARADAR FUNDAMENTAL IMPORT"
        );

        System.out.println(
                "======================================"
        );


        SharadarFundamentalImportService.ImportResult result =
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
                "Dimension Skipped Rows = "
                        + result.dimensionSkippedRows()
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