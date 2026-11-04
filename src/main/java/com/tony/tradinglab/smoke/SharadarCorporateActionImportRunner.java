package com.tony.tradinglab.smoke;

import com.tony.tradinglab.stock.ingest.sharadar.SharadarCorporateActionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sharadar-action-import")
@RequiredArgsConstructor
public class SharadarCorporateActionImportRunner
        implements CommandLineRunner {

    private final SharadarCorporateActionImportService importService;


    @Override
    public void run(
            String... args
    ) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " SHARADAR CORPORATE ACTION IMPORT"
        );

        System.out.println(
                "======================================"
        );


        SharadarCorporateActionImportService.ImportResult result =
                importService.importAll();


        System.out.println();

        System.out.println(
                "Source Rows          = "
                        + result.sourceRows()
        );

        System.out.println(
                "Written Rows         = "
                        + result.writtenRows()
        );

        System.out.println(
                "Irrelevant Rows      = "
                        + result.irrelevantActionRows()
        );

        System.out.println(
                "Outside Range Rows   = "
                        + result.outsideRangeRows()
        );

        System.out.println(
                "Ticker Not Mapped    = "
                        + result.tickerNotMappedRows()
        );

        System.out.println(
                "======================================"
        );
    }
}