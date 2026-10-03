package com.tony.tradinglab.smoke;

import com.tony.tradinglab.stock.ingest.HistoricalStockMasterRecord;
import com.tony.tradinglab.stock.ingest.HistoricalStockMasterSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("sharadar-tickers-smoke")
@RequiredArgsConstructor
public class SharadarTickerLoadSmokeRunner
        implements CommandLineRunner {

    private final HistoricalStockMasterSource stockMasterSource;


    @Override
    public void run(
            String... args
    ) {

        List<HistoricalStockMasterRecord> records =
                stockMasterSource.loadAll();


        long activeCount =
                records.stream()
                        .filter(
                                HistoricalStockMasterRecord::active
                        )
                        .count();


        long delistedCount =
                records.size()
                        - activeCount;


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " SHARADAR TICKERS LOAD"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Total Records = "
                        + records.size()
        );

        System.out.println(
                "Active = "
                        + activeCount
        );

        System.out.println(
                "Delisted = "
                        + delistedCount
        );


        System.out.println();
        System.out.println(
                "----- SAMPLE -----"
        );


        records.stream()
                .limit(20)
                .forEach(
                        record -> {

                            System.out.println(
                                    record.symbol()
                                            + " | sourceId="
                                            + record.sourceSecurityId()
                                            + " | exchange="
                                            + record.exchange()
                                            + " | active="
                                            + record.active()
                                            + " | sector="
                                            + record.sector()
                                            + " | industry="
                                            + record.industry()
                            );
                        }
                );


        System.out.println(
                "======================================"
        );
    }
}