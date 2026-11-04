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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharadarCorporateActionImportService {

    private static final int BATCH_SIZE =
            5_000;

    private static final long PROGRESS_INTERVAL =
            100_000L;


    private static final String INSERT_SQL = """
            INSERT INTO sharadar_corporate_actions (
                source_line,
                stock_id,
                action_date,
                action_type,
                ticker,
                security_name,
                action_value,
                contra_ticker,
                contra_name
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                source_line = VALUES(source_line)
            """;


    private final StockRepository stockRepository;

    private final DataSource dataSource;


    @Value("${sharadar.actions-file:}")
    private String actionsFile;


    @Value("${sharadar.actions-checkpoint-file:}")
    private String actionsCheckpointFile;


    public ImportResult importAll() {

        Path sourcePath =
                validateAndGetSourcePath();


        Path checkpointPath =
                validateAndGetCheckpointPath();


        long checkpointLine =
                readCheckpoint(
                        checkpointPath
                );


        Map<String, Long> stockIdsByTicker =
                buildStockIdIndex();


        log.info(
                "[SHARADAR ACTION IMPORT] START file={} stocks={} range={}~{} checkpointLine={}",
                sourcePath.toAbsolutePath(),
                stockIdsByTicker.size(),
                BacktestDatasetSpec.PRICE_START,
                BacktestDatasetSpec.PRICE_END,
                checkpointLine
        );


        long sourceRows = 0L;
        long writtenRows = 0L;
        long irrelevantActionRows = 0L;
        long outsideRangeRows = 0L;
        long tickerNotMappedRows = 0L;


        try (
                ZipInputStream zipInputStream =
                        new ZipInputStream(
                                Files.newInputStream(
                                        sourcePath
                                ),
                                StandardCharsets.UTF_8
                        )
        ) {

            ZipEntry entry =
                    findActionsCsvEntry(
                            zipInputStream
                    );


            if (entry == null) {

                throw new IllegalStateException(
                        "actions.csv was not found in ZIP: "
                                + sourcePath.toAbsolutePath()
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


            String headerLine =
                    reader.readLine();


            if (headerLine == null) {

                throw new IllegalStateException(
                        "Sharadar actions.csv is empty."
                );
            }


            Map<String, Integer> headerIndex =
                    buildHeaderIndex(
                            headerLine
                    );


            validateRequiredColumns(
                    headerIndex
            );


            try (
                    Connection connection =
                            dataSource.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(
                                    INSERT_SQL
                            )
            ) {

                boolean originalAutoCommit =
                        connection.getAutoCommit();


                connection.setAutoCommit(
                        false
                );


                int batchCount = 0;

                long lineNumber = 1L;


                try {

                    String line;


                    while (
                            (line = reader.readLine())
                                    != null
                    ) {

                        lineNumber++;


                        /*
                         * 이미 확실히 처리된 원본 line.
                         */
                        if (lineNumber <= checkpointLine) {
                            continue;
                        }


                        if (line.isBlank()) {
                            continue;
                        }


                        sourceRows++;


                        String[] values =
                                parseCsvLine(
                                        line
                                );


                        String action =
                                requiredValue(
                                        values,
                                        headerIndex,
                                        "action",
                                        lineNumber
                                )
                                        .trim()
                                        .toLowerCase(
                                                Locale.ROOT
                                        );


                        if (!isRelevantAction(
                                action
                        )) {

                            irrelevantActionRows++;

                            continue;
                        }


                        LocalDate actionDate =
                                requiredDate(
                                        values,
                                        headerIndex,
                                        "date",
                                        lineNumber
                                );


                        /*
                         * 현재 backtest price horizon에
                         * 필요한 action만 저장한다.
                         */
                        if (actionDate.isBefore(
                                BacktestDatasetSpec.PRICE_START
                        )
                                || actionDate.isAfter(
                                BacktestDatasetSpec.PRICE_END
                        )) {

                            outsideRangeRows++;

                            continue;
                        }


                        String ticker =
                                normalizeNullable(
                                        value(
                                                values,
                                                headerIndex,
                                                "ticker"
                                        )
                                );


                        String securityName =
                                normalizeNullable(
                                        value(
                                                values,
                                                headerIndex,
                                                "name"
                                        )
                                );


                        BigDecimal actionValue =
                                nullableDecimal(
                                        value(
                                                values,
                                                headerIndex,
                                                "value"
                                        )
                                );


                        String contraTicker =
                                normalizeNullable(
                                        value(
                                                values,
                                                headerIndex,
                                                "contraticker"
                                        )
                                );


                        String contraName =
                                normalizeNullable(
                                        value(
                                                values,
                                                headerIndex,
                                                "contraname"
                                        )
                                );


                        Long stockId =
                                findStockId(
                                        stockIdsByTicker,
                                        ticker
                                );


                        /*
                         * Actions에는 ticker=N/A도 있고
                         * 우리 master와 직접 연결되지 않는
                         * security도 있으므로 skip하지 않는다.
                         *
                         * stock_id만 NULL로 저장한다.
                         */
                        if (stockId == null) {
                            tickerNotMappedRows++;
                        }


                        bindRow(
                                statement,
                                lineNumber,
                                stockId,
                                actionDate,
                                action,
                                ticker,
                                securityName,
                                actionValue,
                                contraTicker,
                                contraName
                        );


                        statement.addBatch();

                        batchCount++;


                        if (batchCount >= BATCH_SIZE) {

                            statement.executeBatch();

                            connection.commit();


                            /*
                             * DB commit 성공 후에만
                             * checkpoint를 앞으로 이동.
                             */
                            writeCheckpoint(
                                    checkpointPath,
                                    lineNumber
                            );


                            writtenRows +=
                                    batchCount;


                            batchCount = 0;


                            log.info(
                                    "[SHARADAR ACTION IMPORT] committed={} sourceLine={}",
                                    writtenRows,
                                    lineNumber
                            );
                        }


                        if (sourceRows
                                % PROGRESS_INTERVAL
                                == 0) {

                            log.info(
                                    "[SHARADAR ACTION IMPORT] sourceRows={} written={} irrelevant={} outsideRange={} tickerNotMapped={} sourceLine={}",
                                    sourceRows,
                                    writtenRows,
                                    irrelevantActionRows,
                                    outsideRangeRows,
                                    tickerNotMappedRows,
                                    lineNumber
                            );
                        }
                    }


                    if (batchCount > 0) {

                        statement.executeBatch();

                        connection.commit();


                        writtenRows +=
                                batchCount;
                    }


                    /*
                     * 마지막 구간이 모두 skip 대상이어도
                     * EOF까지 검사했다는 의미이므로
                     * 마지막 source line을 checkpoint로 기록.
                     */
                    writeCheckpoint(
                            checkpointPath,
                            lineNumber
                    );


                } catch (Exception e) {

                    connection.rollback();


                    throw new IllegalStateException(
                            "Sharadar corporate action import failed."
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
                    "Failed to import Sharadar corporate actions: "
                            + sourcePath.toAbsolutePath(),
                    e
            );
        }


        ImportResult result =
                new ImportResult(
                        sourceRows,
                        writtenRows,
                        irrelevantActionRows,
                        outsideRangeRows,
                        tickerNotMappedRows
                );


        log.info(
                "[SHARADAR ACTION IMPORT] END {}",
                result
        );


        return result;
    }


    private boolean isRelevantAction(
            String action
    ) {

        if (action == null
                || action.isBlank()) {

            return false;
        }

        String normalized =
                action.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return normalized.equals("delisted")
                || normalized.equals("bankruptcyliquidation")
                || normalized.equals("regulatorydelisting")
                || normalized.equals("voluntarydelisting")

                || normalized.startsWith("acquisition")

                || normalized.equals("mergerfrom")
                || normalized.equals("mergerto")
                || normalized.equals("spacmerger")

                || normalized.equals("tickerchangefrom")
                || normalized.equals("tickerchangeto")

                || normalized.equals("spinoff")
                || normalized.equals("spunofffrom")
                || normalized.equals("listed");
    }


    private void bindRow(
            PreparedStatement statement,
            long sourceLine,
            Long stockId,
            LocalDate actionDate,
            String actionType,
            String ticker,
            String securityName,
            BigDecimal actionValue,
            String contraTicker,
            String contraName
    ) throws Exception {

        statement.setLong(
                1,
                sourceLine
        );


        if (stockId == null) {

            statement.setNull(
                    2,
                    Types.BIGINT
            );

        } else {

            statement.setLong(
                    2,
                    stockId
            );
        }


        statement.setDate(
                3,
                Date.valueOf(
                        actionDate
                )
        );


        statement.setString(
                4,
                actionType
        );


        setString(
                statement,
                5,
                ticker
        );


        setString(
                statement,
                6,
                securityName
        );


        if (actionValue == null) {

            statement.setNull(
                    7,
                    Types.DECIMAL
            );

        } else {

            statement.setBigDecimal(
                    7,
                    actionValue
            );
        }


        setString(
                statement,
                8,
                contraTicker
        );


        setString(
                statement,
                9,
                contraName
        );
    }


    private Long findStockId(
            Map<String, Long> stockIdsByTicker,
            String ticker
    ) {

        if (ticker == null) {
            return null;
        }


        return stockIdsByTicker.get(
                normalizeTicker(
                        ticker
                )
        );
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


    private Path validateAndGetSourcePath() {

        if (actionsFile == null
                || actionsFile.isBlank()) {

            throw new IllegalStateException(
                    "sharadar.actions-file "
                            + "is not configured."
            );
        }


        Path path =
                Path.of(
                        actionsFile
                );


        if (!Files.exists(
                path
        )) {

            throw new IllegalStateException(
                    "Sharadar actions ZIP not found: "
                            + path.toAbsolutePath()
            );
        }


        return path;
    }


    private Path validateAndGetCheckpointPath() {

        if (actionsCheckpointFile == null
                || actionsCheckpointFile.isBlank()) {

            throw new IllegalStateException(
                    "sharadar.actions-checkpoint-file "
                            + "is not configured."
            );
        }


        return Path.of(
                actionsCheckpointFile
        );
    }


    private ZipEntry findActionsCsvEntry(
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
                    "actions.csv"
            )) {

                return entry;
            }
        }


        return null;
    }


    private Map<String, Integer> buildHeaderIndex(
            String headerLine
    ) {

        String[] columns =
                parseCsvLine(
                        removeBom(
                                headerLine
                        )
                );


        Map<String, Integer> result =
                new HashMap<>();


        for (
                int i = 0;
                i < columns.length;
                i++
        ) {

            String column =
                    columns[i]
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );


            Integer previous =
                    result.put(
                            column,
                            i
                    );


            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate CSV header column: "
                                + column
                );
            }
        }


        return result;
    }


    private void validateRequiredColumns(
            Map<String, Integer> headerIndex
    ) {

        List<String> requiredColumns =
                List.of(
                        "date",
                        "action",
                        "ticker",
                        "name",
                        "value",
                        "contraticker",
                        "contraname"
                );


        List<String> missing =
                requiredColumns.stream()
                        .filter(
                                column ->
                                        !headerIndex.containsKey(
                                                column
                                        )
                        )
                        .toList();


        if (!missing.isEmpty()) {

            throw new IllegalStateException(
                    "Missing required Sharadar action columns: "
                            + missing
            );
        }
    }


    private String value(
            String[] values,
            Map<String, Integer> headerIndex,
            String column
    ) {

        Integer index =
                headerIndex.get(
                        column
                );


        if (index == null) {

            throw new IllegalStateException(
                    "CSV column not found: "
                            + column
            );
        }


        if (index >= values.length) {
            return null;
        }


        return values[index];
    }


    private String requiredValue(
            String[] values,
            Map<String, Integer> headerIndex,
            String column,
            long lineNumber
    ) {

        String result =
                normalizeNullable(
                        value(
                                values,
                                headerIndex,
                                column
                        )
                );


        if (result == null) {

            throw new IllegalArgumentException(
                    "Required value is empty."
                            + " line="
                            + lineNumber
                            + " column="
                            + column
            );
        }


        return result;
    }


    private LocalDate requiredDate(
            String[] values,
            Map<String, Integer> headerIndex,
            String column,
            long lineNumber
    ) {

        String dateValue =
                requiredValue(
                        values,
                        headerIndex,
                        column,
                        lineNumber
                );


        try {

            return LocalDate.parse(
                    dateValue
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid date."
                            + " line="
                            + lineNumber
                            + " column="
                            + column
                            + " value="
                            + dateValue,
                    e
            );
        }
    }


    private BigDecimal nullableDecimal(
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
        )
                || "NAN".equalsIgnoreCase(
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


    private void setString(
            PreparedStatement statement,
            int index,
            String value
    ) throws Exception {

        if (value == null) {

            statement.setNull(
                    index,
                    Types.VARCHAR
            );

            return;
        }


        statement.setString(
                index,
                value
        );
    }


    private long readCheckpoint(
            Path checkpointPath
    ) {

        try {

            if (!Files.exists(
                    checkpointPath
            )) {

                return 0L;
            }


            String value =
                    Files.readString(
                            checkpointPath,
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
                        "Checkpoint line cannot be negative: "
                                + lineNumber
                );
            }


            return lineNumber;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to read action checkpoint: "
                            + checkpointPath.toAbsolutePath(),
                    e
            );
        }
    }


    private void writeCheckpoint(
            Path checkpointPath,
            long lineNumber
    ) {

        Path parent =
                checkpointPath.getParent();


        Path tempPath =
                checkpointPath.resolveSibling(
                        checkpointPath.getFileName()
                                + ".tmp"
                );


        try {

            if (parent != null) {

                Files.createDirectories(
                        parent
                );
            }


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
                        checkpointPath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                        tempPath,
                        checkpointPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to write action checkpoint."
                            + " line="
                            + lineNumber
                            + " path="
                            + checkpointPath.toAbsolutePath(),
                    e
            );
        }
    }


    private String[] parseCsvLine(
            String line
    ) {

        List<String> result =
                new ArrayList<>();


        StringBuilder current =
                new StringBuilder();


        boolean inQuotes = false;


        for (
                int i = 0;
                i < line.length();
                i++
        ) {

            char c =
                    line.charAt(
                            i
                    );


            if (c == '"') {

                if (inQuotes
                        && i + 1 < line.length()
                        && line.charAt(
                        i + 1
                ) == '"') {

                    current.append(
                            '"'
                    );

                    i++;

                } else {

                    inQuotes =
                            !inQuotes;
                }

                continue;
            }


            if (c == ','
                    && !inQuotes) {

                result.add(
                        current.toString()
                );

                current.setLength(
                        0
                );

                continue;
            }


            current.append(
                    c
            );
        }


        if (inQuotes) {

            throw new IllegalArgumentException(
                    "Unclosed quote in CSV row."
            );
        }


        result.add(
                current.toString()
        );


        return result.toArray(
                String[]::new
        );
    }


    private String removeBom(
            String value
    ) {

        if (value != null
                && !value.isEmpty()
                && value.charAt(0) == '\uFEFF') {

            return value.substring(
                    1
            );
        }


        return value;
    }


    public record ImportResult(
            long sourceRows,
            long writtenRows,
            long irrelevantActionRows,
            long outsideRangeRows,
            long tickerNotMappedRows
    ) {
    }
}