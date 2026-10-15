package com.tony.tradinglab.smoke;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Profile("sharadar-fundamental-key-check")
public class SharadarFundamentalKeyCheckRunner
        implements CommandLineRunner {

    @Value("${sharadar.fundamentals-file}")
    private String fundamentalsFile;


    @Override
    public void run(String... args) throws Exception {

        Path path =
                Path.of(fundamentalsFile);


        Set<String> keyCalendar =
                new HashSet<>();

        Set<String> keyFiling =
                new HashSet<>();

        Set<String> keyReport =
                new HashSet<>();


        long acceptedRows = 0L;

        long calendarDuplicates = 0L;
        long filingDuplicates = 0L;
        long reportDuplicates = 0L;


        try (
                ZipInputStream zip =
                        new ZipInputStream(
                                Files.newInputStream(path),
                                StandardCharsets.UTF_8
                        )
        ) {

            ZipEntry entry;


            while ((entry = zip.getNextEntry()) != null) {

                if (!entry.isDirectory()
                        && entry.getName()
                        .toLowerCase(Locale.ROOT)
                        .endsWith("fundamentals.csv")) {

                    break;
                }
            }


            if (entry == null) {
                throw new IllegalStateException(
                        "fundamentals.csv not found"
                );
            }


            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    zip,
                                    StandardCharsets.UTF_8
                            ),
                            1024 * 1024
                    );


            String header =
                    reader.readLine();


            String[] columns =
                    header.split(",", -1);


            Map<String, Integer> index =
                    new HashMap<>();


            for (int i = 0; i < columns.length; i++) {

                index.put(
                        columns[i]
                                .trim()
                                .toLowerCase(Locale.ROOT),
                        i
                );
            }


            String line;


            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }


                String[] values =
                        line.split(",", -1);


                String ticker =
                        values[index.get("ticker")]
                                .trim()
                                .toUpperCase(Locale.ROOT);


                String dimension =
                        values[index.get("dimension")]
                                .trim()
                                .toUpperCase(Locale.ROOT);


                if (!"ARQ".equals(dimension)
                        && !"ART".equals(dimension)) {

                    continue;
                }


                LocalDate calendarDate =
                        LocalDate.parse(
                                values[index.get("calendardate")]
                        );


                LocalDate filingDate =
                        LocalDate.parse(
                                values[index.get("date")]
                        );


                String reportPeriod =
                        values[index.get("reportperiod")]
                                .trim();


                if (calendarDate.isBefore(
                        LocalDate.of(2008, 1, 1)
                )
                        || calendarDate.isAfter(
                        LocalDate.of(2025, 9, 30)
                )
                        || filingDate.isAfter(
                        LocalDate.of(2025, 9, 30)
                )) {

                    continue;
                }


                acceptedRows++;


                String calendarKey =
                        ticker
                                + "|"
                                + dimension
                                + "|"
                                + calendarDate;


                String filingKey =
                        calendarKey
                                + "|"
                                + filingDate;


                String reportKey =
                        filingKey
                                + "|"
                                + reportPeriod;


                if (!keyCalendar.add(calendarKey)) {
                    calendarDuplicates++;
                }


                if (!keyFiling.add(filingKey)) {
                    filingDuplicates++;
                }


                if (!keyReport.add(reportKey)) {
                    reportDuplicates++;
                }
            }
        }


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " SHARADAR FUNDAMENTAL KEY CHECK"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Accepted Rows               = "
                        + acceptedRows
        );

        System.out.println(
                "Calendar Key Duplicates     = "
                        + calendarDuplicates
        );

        System.out.println(
                "Calendar+Filing Duplicates  = "
                        + filingDuplicates
        );

        System.out.println(
                "+ReportPeriod Duplicates     = "
                        + reportDuplicates
        );

        System.out.println(
                "======================================"
        );
    }
}