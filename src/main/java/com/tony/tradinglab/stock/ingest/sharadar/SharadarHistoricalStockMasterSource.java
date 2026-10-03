package com.tony.tradinglab.stock.ingest.sharadar;

import com.tony.tradinglab.stock.ingest.HistoricalStockMasterRecord;
import com.tony.tradinglab.stock.ingest.HistoricalStockMasterSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SharadarHistoricalStockMasterSource
        implements HistoricalStockMasterSource {

    private final SharadarTickerMapper tickerMapper;


    @Value("${sharadar.tickers-file:}")
    private String tickersFile;


    @Override
    public List<HistoricalStockMasterRecord> loadAll() {

        if (tickersFile == null
                || tickersFile.isBlank()) {

            throw new IllegalStateException(
                    "sharadar.tickers-file is not configured."
            );
        }


        Path path =
                Path.of(
                        tickersFile
                );


        if (!Files.exists(path)) {

            throw new IllegalStateException(
                    "Sharadar TICKERS file not found: "
                            + path.toAbsolutePath()
            );
        }


        List<HistoricalStockMasterRecord> results =
                new ArrayList<>();


        try (
                BufferedReader reader =
                        Files.newBufferedReader(
                                path,
                                StandardCharsets.UTF_8
                        )
        ) {

            String headerLine =
                    reader.readLine();


            if (headerLine == null) {

                return List.of();
            }


            List<String> headers =
                    parseCsvLine(
                            removeBom(
                                    headerLine
                            )
                    );


            Map<String, Integer> headerIndex =
                    createHeaderIndex(
                            headers
                    );


            String line;

            long lineNumber = 1;


            while (
                    (line = reader.readLine())
                            != null
            ) {

                lineNumber++;


                if (line.isBlank()) {
                    continue;
                }


                try {

                    List<String> values =
                            parseCsvLine(
                                    line
                            );


                    SharadarTickerRecord source =
                            toTickerRecord(
                                    values,
                                    headerIndex
                            );


                    tickerMapper
                            .map(source)
                            .ifPresent(
                                    results::add
                            );

                } catch (RuntimeException e) {

                    throw new IllegalStateException(
                            "Failed to parse Sharadar TICKERS CSV. line="
                                    + lineNumber,
                            e
                    );
                }
            }


        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to read Sharadar TICKERS file: "
                            + path.toAbsolutePath(),
                    e
            );
        }


        return List.copyOf(
                results
        );
    }


    private SharadarTickerRecord toTickerRecord(
            List<String> values,
            Map<String, Integer> headerIndex
    ) {

        return new SharadarTickerRecord(

                getValue(
                        values,
                        headerIndex,
                        "table"
                ),

                getValue(
                        values,
                        headerIndex,
                        "permaticker"
                ),

                getValue(
                        values,
                        headerIndex,
                        "ticker"
                ),

                getValue(
                        values,
                        headerIndex,
                        "name"
                ),

                getValue(
                        values,
                        headerIndex,
                        "exchange"
                ),

                parseBooleanYn(
                        getValue(
                                values,
                                headerIndex,
                                "isdelisted"
                        )
                ),

                getValue(
                        values,
                        headerIndex,
                        "category"
                ),

                getValue(
                        values,
                        headerIndex,
                        "sector"
                ),

                getValue(
                        values,
                        headerIndex,
                        "industry"
                ),

                getValue(
                        values,
                        headerIndex,
                        "currency"
                ),

                getValue(
                        values,
                        headerIndex,
                        "relatedtickers"
                ),

                parseDate(
                        getValue(
                                values,
                                headerIndex,
                                "firstpricedate"
                        )
                ),

                parseDate(
                        getValue(
                                values,
                                headerIndex,
                                "lastpricedate"
                        )
                )
        );
    }


    private Map<String, Integer> createHeaderIndex(
            List<String> headers
    ) {

        Map<String, Integer> index =
                new HashMap<>();


        for (
                int i = 0;
                i < headers.size();
                i++
        ) {

            String header =
                    headers.get(i);


            if (header == null) {
                continue;
            }


            index.put(
                    header.trim()
                            .toLowerCase(),
                    i
            );
        }


        return index;
    }


    private String getValue(
            List<String> values,
            Map<String, Integer> headerIndex,
            String column
    ) {

        Integer index =
                headerIndex.get(
                        column
                );


        if (index == null
                || index < 0
                || index >= values.size()) {

            return null;
        }


        String value =
                values.get(
                        index
                );


        if (value == null) {
            return null;
        }


        String normalized =
                value.trim();


        return normalized.isEmpty()
                ? null
                : normalized;
    }


    private LocalDate parseDate(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return null;
        }


        return LocalDate.parse(
                value.trim()
        );
    }


    private boolean parseBooleanYn(
            String value
    ) {

        if (value == null) {
            return false;
        }


        return switch (
                value.trim()
                        .toUpperCase()
                ) {

            case "Y",
                 "YES",
                 "TRUE",
                 "1" -> true;

            default -> false;
        };
    }


    private String removeBom(
            String value
    ) {

        if (value == null
                || value.isEmpty()) {

            return value;
        }


        if (value.charAt(0) == '\uFEFF') {

            return value.substring(1);
        }


        return value;
    }


    private List<String> parseCsvLine(
            String line
    ) {

        List<String> values =
                new ArrayList<>();


        StringBuilder current =
                new StringBuilder();


        boolean insideQuotes =
                false;


        for (
                int i = 0;
                i < line.length();
                i++
        ) {

            char currentChar =
                    line.charAt(i);


            if (currentChar == '"') {

                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');

                    i++;

                } else {

                    insideQuotes =
                            !insideQuotes;
                }

                continue;
            }


            if (currentChar == ','
                    && !insideQuotes) {

                values.add(
                        current.toString()
                );

                current.setLength(0);

                continue;
            }


            current.append(
                    currentChar
            );
        }


        if (insideQuotes) {

            throw new IllegalArgumentException(
                    "Unclosed quoted CSV field."
            );
        }


        values.add(
                current.toString()
        );


        return values;
    }
}