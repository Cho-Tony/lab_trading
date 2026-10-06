package com.tony.tradinglab.smoke;

import com.tony.tradinglab.stock.ingest.HistoricalStockMasterRecord;
import com.tony.tradinglab.stock.ingest.HistoricalStockMasterSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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


        Map<String, Long> sourceIdCounts =
                records.stream()
                        .collect(
                                java.util.stream.Collectors.groupingBy(
                                        HistoricalStockMasterRecord::sourceSecurityId,
                                        java.util.stream.Collectors.counting()
                                )
                        );


        List<Map.Entry<String, Long>> duplicateSourceIds =
                sourceIdCounts.entrySet()
                        .stream()
                        .filter(
                                entry -> entry.getValue() > 1
                        )
                        .sorted(
                                Map.Entry.<String, Long>comparingByValue()
                                        .reversed()
                        )
                        .toList();


        Map<String, Long> symbolExchangeCounts =
                records.stream()
                        .collect(
                                java.util.stream.Collectors.groupingBy(
                                        record ->
                                                record.symbol()
                                                        + "|"
                                                        + record.exchange(),
                                        java.util.stream.Collectors.counting()
                                )
                        );


        List<Map.Entry<String, Long>> duplicateSymbolExchanges =
                symbolExchangeCounts.entrySet()
                        .stream()
                        .filter(
                                entry -> entry.getValue() > 1
                        )
                        .sorted(
                                Map.Entry.<String, Long>comparingByValue()
                                        .reversed()
                        )
                        .toList();


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
                "----- IDENTITY CHECK -----"
        );


        System.out.println(
                "Duplicate sourceSecurityId = "
                        + duplicateSourceIds.size()
        );


        duplicateSourceIds.stream()
                .limit(20)
                .forEach(
                        entry ->
                                System.out.println(
                                        "SOURCE ID DUPLICATE: "
                                                + entry.getKey()
                                                + " | count="
                                                + entry.getValue()
                                )
                );


        System.out.println(
                "Duplicate symbol+exchange = "
                        + duplicateSymbolExchanges.size()
        );


        duplicateSymbolExchanges.stream()
                .limit(20)
                .forEach(
                        entry ->
                                System.out.println(
                                        "SYMBOL DUPLICATE: "
                                                + entry.getKey()
                                                + " | count="
                                                + entry.getValue()
                                )
                );


        System.out.println();
        System.out.println(
                "----- SAMPLE -----"
        );


        records.stream()
                .limit(20)
                .forEach(
                        record ->
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
                                )
                );


        System.out.println(
                "======================================"
        );
    }

}