package com.tony.tradinglab.smoke;

import com.tony.tradinglab.stock.classification.service.HistoricalStockClassificationImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sharadar-classification-import")
@RequiredArgsConstructor
public class SharadarClassificationImportRunner
        implements CommandLineRunner {

    private final HistoricalStockClassificationImportService importService;


    @Override
    public void run(
            String... args
    ) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " SHARADAR CLASSIFICATION IMPORT"
        );

        System.out.println(
                "======================================"
        );


        HistoricalStockClassificationImportService.ImportResult result =
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
                "Unchanged      = "
                        + result.unchangedCount()
        );

        System.out.println(
                "Skipped        = "
                        + result.skippedCount()
        );

        System.out.println(
                "Stock Missing  = "
                        + result.stockMissingCount()
        );


        System.out.println(
                "======================================"
        );
    }
}