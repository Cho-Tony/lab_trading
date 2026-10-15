package com.tony.tradinglab.stock.ingest.sharadar;

import com.tony.tradinglab.discovery.quant.BacktestDatasetSpec;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharadarStockPriceImportService {

    private static final int BATCH_SIZE = 5_000;

    private static final long PROGRESS_INTERVAL = 1_000_000L;

    private static final String EXPECTED_HEADER =
            "ticker,date,open,high,low,close,volume,"
                    + "closeadj,closeunadj,lastupdated";

    @Value("${sharadar.stocks-checkpoint-file:}")
    private String stocksCheckpointFile;


    private static final String UPSERT_SQL = """
            INSERT INTO sharadar_stock_prices (
                stock_id,
                trade_date,
                open_price,
                high_price,
                low_price,
                close_price,
                volume,
                adjusted_close,
                unadjusted_close,
                source_last_updated
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                open_price = VALUES(open_price),
                high_price = VALUES(high_price),
                low_price = VALUES(low_price),
                close_price = VALUES(close_price),
                volume = VALUES(volume),
                adjusted_close = VALUES(adjusted_close),
                unadjusted_close = VALUES(unadjusted_close),
                source_last_updated = VALUES(source_last_updated)
            """;


    private final StockRepository stockRepository;

    private final DataSource dataSource;


    @Value("${sharadar.stocks-file:}")
    private String stocksFile;


    public ImportResult importAll() {

        Path path =
                validateAndGetPath();


        Map<String, Long> stockIdsByTicker =
                buildStockIdIndex();


        long checkpointLine =
                readCheckpoint();


        log.info(
                "[SHARADAR PRICE IMPORT] START file={} stocks={} range={}~{} checkpointLine={}",
                path.toAbsolutePath(),
                stockIdsByTicker.size(),
                BacktestDatasetSpec.PRICE_START,
                BacktestDatasetSpec.PRICE_END,
                checkpointLine
        );


        long sourceRows = 0;
        long writtenRows = 0;
        long outsideRangeRows = 0;
        long tickerNotInUniverseRows = 0;


        try (
                ZipInputStream zipInputStream =
                        new ZipInputStream(
                                Files.newInputStream(path),
                                StandardCharsets.UTF_8
                        )
        ) {

            ZipEntry entry =
                    findStocksCsvEntry(
                            zipInputStream
                    );


            if (entry == null) {

                throw new IllegalStateException(
                        "stocks.csv was not found in ZIP: "
                                + path.toAbsolutePath()
                );
            }


            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    zipInputStream,
                                    StandardCharsets.UTF_8
                            ),
                            1024 * 1024
                    );


            String header =
                    reader.readLine();


            validateHeader(
                    header
            );


            try (
                    Connection connection =
                            dataSource.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(
                                    UPSERT_SQL
                            )
            ) {

                boolean originalAutoCommit =
                        connection.getAutoCommit();


                connection.setAutoCommit(
                        false
                );


                int batchCount = 0;

                long lineNumber = 1;


                try {

                    String line;


                    while (
                            (line = reader.readLine())
                                    != null
                    ) {

                        lineNumber++;


                        /*
                         * 이미 확실히 commit된 source line.
                         *
                         * parse / ticker lookup / DB 접근을
                         * 전부 하지 않고 넘어간다.
                         */
                        if (lineNumber <= checkpointLine) {

                            continue;
                        }


                        if (line.isBlank()) {
                            continue;
                        }


                        sourceRows++;


                        SharadarPriceRow row =
                                parseRow(
                                        line,
                                        lineNumber
                                );


                        if (row.tradeDate()
                                .isBefore(
                                        BacktestDatasetSpec.PRICE_START
                                )
                                || row.tradeDate()
                                .isAfter(
                                        BacktestDatasetSpec.PRICE_END
                                )) {

                            outsideRangeRows++;

                            continue;
                        }


                        Long stockId =
                                stockIdsByTicker.get(
                                        normalizeTicker(
                                                row.ticker()
                                        )
                                );


                        if (stockId == null) {

                            tickerNotInUniverseRows++;

                            continue;
                        }


                        validatePriceRange(
                                row,
                                lineNumber
                        );


                        bind(
                                statement,
                                stockId,
                                row
                        );


                        statement.addBatch();

                        batchCount++;


                        if (batchCount >= BATCH_SIZE) {

                            statement.executeBatch();

                            connection.commit();


                            /*
                             * DB commit 성공 이후에만
                             * checkpoint를 앞으로 이동시킨다.
                             */
                            writeCheckpoint(
                                    lineNumber
                            );


                            writtenRows +=
                                    batchCount;


                            batchCount = 0;


                            log.info(
                                    "[SHARADAR PRICE IMPORT] committed={} sourceLine={}",
                                    writtenRows,
                                    lineNumber
                            );
                        }


                        if (sourceRows
                                % PROGRESS_INTERVAL
                                == 0) {

                            log.info(
                                    "[SHARADAR PRICE IMPORT] sourceRows={} written={} outsideRange={} tickerNotInUniverse={} sourceLine={}",
                                    sourceRows,
                                    writtenRows,
                                    outsideRangeRows,
                                    tickerNotInUniverseRows,
                                    lineNumber
                            );
                        }
                    }


                    if (batchCount > 0) {

                        statement.executeBatch();

                        connection.commit();


                        writeCheckpoint(
                                lineNumber
                        );


                        writtenRows +=
                                batchCount;
                    }


                } catch (Exception e) {

                    connection.rollback();


                    throw new IllegalStateException(
                            "Sharadar stock price import failed."
                                    + " line="
                                    + lineNumber,
                            e
                    );

                } finally {

                    connection.setAutoCommit(
                            originalAutoCommit
                    );
                }
            }


        } catch (IllegalStateException e) {

            throw e;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to import Sharadar stock prices: "
                            + path.toAbsolutePath(),
                    e
            );
        }


        ImportResult result =
                new ImportResult(
                        sourceRows,
                        writtenRows,
                        outsideRangeRows,
                        tickerNotInUniverseRows
                );


        log.info(
                "[SHARADAR PRICE IMPORT] END {}",
                result
        );


        return result;
    }



    private Path validateAndGetPath() {

        if (stocksFile == null
                || stocksFile.isBlank()) {

            throw new IllegalStateException(
                    "sharadar.stocks-file is not configured."
            );
        }


        Path path =
                Path.of(
                        stocksFile
                );


        if (!Files.exists(path)) {

            throw new IllegalStateException(
                    "Sharadar stocks ZIP not found: "
                            + path.toAbsolutePath()
            );
        }


        return path;
    }


    private Map<String, Long> buildStockIdIndex() {

        Map<String, Long> result =
                new HashMap<>();


        for (
                Stock stock
                : stockRepository.findAll()
        ) {

            if (!"SHARADAR".equalsIgnoreCase(
                    stock.getDataSource()
            )) {

                continue;
            }


            if (stock.getSymbol() == null
                    || stock.getSymbol().isBlank()) {

                continue;
            }


            String ticker =
                    normalizeTicker(
                            stock.getSymbol()
                    );


            Long previous =
                    result.put(
                            ticker,
                            stock.getId()
                    );


            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate Sharadar ticker in stocks table: "
                                + ticker
                                + " | stockIds="
                                + previous
                                + ","
                                + stock.getId()
                );
            }
        }


        return result;
    }


    private ZipEntry findStocksCsvEntry(
            ZipInputStream zipInputStream
    ) throws Exception {

        ZipEntry entry;


        while (
                (entry =
                        zipInputStream.getNextEntry())
                        != null
        ) {

            if (entry.isDirectory()) {
                continue;
            }


            String name =
                    entry.getName()
                            .toLowerCase(
                                    Locale.ROOT
                            );


            if (name.endsWith(
                    "stocks.csv"
            )) {

                return entry;
            }
        }


        return null;
    }


    private void validateHeader(
            String header
    ) {

        if (header == null) {

            throw new IllegalStateException(
                    "Sharadar stocks.csv is empty."
            );
        }


        String normalized =
                removeBom(
                        header
                ).trim();


        if (!EXPECTED_HEADER.equalsIgnoreCase(
                normalized
        )) {

            throw new IllegalStateException(
                    "Unexpected Sharadar stocks.csv header."
                            + System.lineSeparator()
                            + "Expected: "
                            + EXPECTED_HEADER
                            + System.lineSeparator()
                            + "Actual  : "
                            + normalized
            );
        }
    }


    private SharadarPriceRow parseRow(
            String line,
            long lineNumber
    ) {

        String[] values =
                line.split(
                        ",",
                        -1
                );


        if (values.length != 10) {

            throw new IllegalArgumentException(
                    "Unexpected column count at line "
                            + lineNumber
                            + ": "
                            + values.length
            );
        }


        String ticker =
                values[0].trim();


        if (ticker.isEmpty()) {

            throw new IllegalArgumentException(
                    "Ticker is empty at line "
                            + lineNumber
            );
        }


        LocalDate tradeDate =
                LocalDate.parse(
                        values[1].trim()
                );


        return new SharadarPriceRow(
                ticker,
                tradeDate,

                parseDecimal(
                        values[2]
                ),

                parseDecimal(
                        values[3]
                ),

                parseDecimal(
                        values[4]
                ),

                parseDecimal(
                        values[5]
                ),

                parseDecimal(
                        values[6]
                ),

                parseDecimal(
                        values[7]
                ),

                parseDecimal(
                        values[8]
                ),

                parseDate(
                        values[9]
                )
        );
    }


    private void bind(
            PreparedStatement statement,
            Long stockId,
            SharadarPriceRow row
    ) throws Exception {

        statement.setLong(
                1,
                stockId
        );


        statement.setDate(
                2,
                Date.valueOf(
                        row.tradeDate()
                )
        );


        setBigDecimal(
                statement,
                3,
                row.open()
        );


        setBigDecimal(
                statement,
                4,
                row.high()
        );


        setBigDecimal(
                statement,
                5,
                row.low()
        );


        setBigDecimal(
                statement,
                6,
                row.close()
        );


        setBigDecimal(
                statement,
                7,
                row.volume()
        );


        setBigDecimal(
                statement,
                8,
                row.adjustedClose()
        );


        setBigDecimal(
                statement,
                9,
                row.unadjustedClose()
        );


        if (row.lastUpdated() == null) {

            statement.setNull(
                    10,
                    java.sql.Types.DATE
            );

        } else {

            statement.setDate(
                    10,
                    Date.valueOf(
                            row.lastUpdated()
                    )
            );
        }
    }


    private void setBigDecimal(
            PreparedStatement statement,
            int index,
            BigDecimal value
    ) throws Exception {

        if (value == null) {

            statement.setNull(
                    index,
                    java.sql.Types.DECIMAL
            );

            return;
        }


        statement.setBigDecimal(
                index,
                value
        );
    }


    private BigDecimal parseDecimal(
            String value
    ) {

        String normalized =
                normalizeNullable(
                        value
                );


        if (normalized == null) {
            return null;
        }


        return new BigDecimal(
                normalized
        );
    }


    private LocalDate parseDate(
            String value
    ) {

        String normalized =
                normalizeNullable(
                        value
                );


        if (normalized == null) {
            return null;
        }


        return LocalDate.parse(
                normalized
        );
    }


    private String normalizeNullable(
            String value
    ) {

        if (value == null) {
            return null;
        }


        String normalized =
                value.trim();


        if (normalized.isEmpty()
                || "N/A".equalsIgnoreCase(
                normalized
        )
                || "NULL".equalsIgnoreCase(
                normalized
        )) {

            return null;
        }


        return normalized;
    }


    private String normalizeTicker(
            String ticker
    ) {

        return ticker
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }


    private String removeBom(
            String value
    ) {

        if (value != null
                && !value.isEmpty()
                && value.charAt(0) == '\uFEFF') {

            return value.substring(1);
        }


        return value;
    }


    private record SharadarPriceRow(
            String ticker,
            LocalDate tradeDate,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal volume,
            BigDecimal adjustedClose,
            BigDecimal unadjustedClose,
            LocalDate lastUpdated
    ) {
    }


    public record ImportResult(
            long sourceRows,
            long writtenRows,
            long outsideRangeRows,
            long tickerNotInUniverseRows
    ) {
    }

    private static final BigDecimal MAX_PRICE =
            BigDecimal.TEN
                    .pow(22)
                    .subtract(
                            BigDecimal.ONE.movePointLeft(16)
                    );


    private void validatePriceRange(
            SharadarPriceRow row,
            long lineNumber
    ) {

        validatePrice(
                "open",
                row.open(),
                row,
                lineNumber
        );

        validatePrice(
                "high",
                row.high(),
                row,
                lineNumber
        );

        validatePrice(
                "low",
                row.low(),
                row,
                lineNumber
        );

        validatePrice(
                "close",
                row.close(),
                row,
                lineNumber
        );

        validatePrice(
                "adjustedClose",
                row.adjustedClose(),
                row,
                lineNumber
        );

        validatePrice(
                "unadjustedClose",
                row.unadjustedClose(),
                row,
                lineNumber
        );
    }


    private void validatePrice(
            String field,
            BigDecimal value,
            SharadarPriceRow row,
            long lineNumber
    ) {

        if (value == null) {
            return;
        }


        if (value.abs()
                .compareTo(
                        MAX_PRICE
                ) > 0) {

            throw new IllegalStateException(
                    "Price exceeds DECIMAL(20,8)"
                            + " | line="
                            + lineNumber
                            + " | ticker="
                            + row.ticker()
                            + " | date="
                            + row.tradeDate()
                            + " | field="
                            + field
                            + " | value="
                            + value
            );
        }
    }

    public long findSourceLineForEligibleRowCount(
            long targetEligibleRows
    ) {

        if (targetEligibleRows <= 0) {
            throw new IllegalArgumentException(
                    "targetEligibleRows must be > 0"
            );
        }


        Path path =
                validateAndGetPath();


        Map<String, Long> stockIdsByTicker =
                buildStockIdIndex();


        long eligibleRows = 0L;
        long lineNumber = 1L;


        log.info(
                "[SHARADAR PRICE CHECKPOINT] START targetEligibleRows={}",
                targetEligibleRows
        );


        try (
                ZipInputStream zipInputStream =
                        new ZipInputStream(
                                Files.newInputStream(path),
                                StandardCharsets.UTF_8
                        )
        ) {

            ZipEntry entry =
                    findStocksCsvEntry(
                            zipInputStream
                    );


            if (entry == null) {

                throw new IllegalStateException(
                        "stocks.csv was not found in ZIP: "
                                + path.toAbsolutePath()
                );
            }


            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    zipInputStream,
                                    StandardCharsets.UTF_8
                            ),
                            1024 * 1024
                    );


            String header =
                    reader.readLine();


            validateHeader(
                    header
            );


            String line;


            while (
                    (line = reader.readLine())
                            != null
            ) {

                lineNumber++;


                if (line.isBlank()) {
                    continue;
                }


                SharadarPriceRow row =
                        parseRow(
                                line,
                                lineNumber
                        );


                if (row.tradeDate()
                        .isBefore(
                                BacktestDatasetSpec.PRICE_START
                        )
                        || row.tradeDate()
                        .isAfter(
                                BacktestDatasetSpec.PRICE_END
                        )) {

                    continue;
                }


                Long stockId =
                        stockIdsByTicker.get(
                                normalizeTicker(
                                        row.ticker()
                                )
                        );


                if (stockId == null) {
                    continue;
                }


                eligibleRows++;


                if (eligibleRows % 1_000_000 == 0) {

                    log.info(
                            "[SHARADAR PRICE CHECKPOINT] eligibleRows={} line={}",
                            eligibleRows,
                            lineNumber
                    );
                }


                if (eligibleRows == targetEligibleRows) {

                    log.info(
                            "[SHARADAR PRICE CHECKPOINT] FOUND eligibleRows={} line={} ticker={} date={}",
                            eligibleRows,
                            lineNumber,
                            row.ticker(),
                            row.tradeDate()
                    );


                    return lineNumber;
                }
            }


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to find Sharadar price checkpoint.",
                    e
            );
        }


        throw new IllegalStateException(
                "Could not reach target eligible rows. target="
                        + targetEligibleRows
                        + ", actual="
                        + eligibleRows
        );
    }

    private long readCheckpoint() {

        if (stocksCheckpointFile == null
                || stocksCheckpointFile.isBlank()) {

            return 0L;
        }


        Path path =
                Path.of(
                        stocksCheckpointFile
                );


        if (!Files.exists(path)) {

            return 0L;
        }


        try {

            String value =
                    Files.readString(
                            path,
                            StandardCharsets.UTF_8
                    ).trim();


            if (value.isEmpty()) {
                return 0L;
            }


            long lineNumber =
                    Long.parseLong(
                            value
                    );


            if (lineNumber < 0) {

                throw new IllegalStateException(
                        "Checkpoint line must not be negative: "
                                + lineNumber
                );
            }


            return lineNumber;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to read checkpoint: "
                            + path.toAbsolutePath(),
                    e
            );
        }
    }


    private void writeCheckpoint(
            long lineNumber
    ) {

        if (stocksCheckpointFile == null
                || stocksCheckpointFile.isBlank()) {

            throw new IllegalStateException(
                    "sharadar.stocks-checkpoint-file "
                            + "is not configured."
            );
        }


        Path path =
                Path.of(
                        stocksCheckpointFile
                );


        Path tempPath =
                path.resolveSibling(
                        path.getFileName()
                                + ".tmp"
                );


        try {

            Files.createDirectories(
                    path.getParent()
            );


            Files.writeString(
                    tempPath,
                    Long.toString(
                            lineNumber
                    ),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );


            try {

                Files.move(
                        tempPath,
                        path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                        tempPath,
                        path,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to write checkpoint."
                            + " line="
                            + lineNumber
                            + " path="
                            + path.toAbsolutePath(),
                    e
            );
        }
    }

}