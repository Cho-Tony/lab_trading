package com.tony.tradinglab.smoke;

import com.tony.tradinglab.stock.ingest.HistoricalStockMasterImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sharadar-stock-import")
@RequiredArgsConstructor
public class SharadarStockImportRunner
        implements CommandLineRunner {

    private final HistoricalStockMasterImportService importService;


    @Override
    public void run(
            String... args
    ) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " SHARADAR STOCK MASTER IMPORT"
        );

        System.out.println(
                "======================================"
        );


        HistoricalStockMasterImportService.ImportResult result =
                importService.importAll();


        System.out.println();
        System.out.println(
                "Source Records = "
                        + result.sourceRecordCount()
        );

        System.out.println(
                "Inserted       = "
                        + result.insertedCount()
        );

        System.out.println(
                "Updated        = "
                        + result.updatedCount()
        );

        System.out.println(
                "Skipped        = "
                        + result.skippedCount()
        );

        System.out.println(
                "Conflicts      = "
                        + result.conflictCount()
        );


        System.out.println(
                "======================================"
        );
    }
}