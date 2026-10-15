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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharadarFundamentalImportService {

    private static final int BATCH_SIZE =
            5_000;

    private static final long PROGRESS_INTERVAL =
            100_000L;


    /*
     * PIT backtest에서는 As-Reported 계열만 사용한다.
     *
     * ARQ = As Reported Quarterly
     * ART = As Reported Trailing Twelve Months
     */
    private static final Set<String> ACCEPTED_DIMENSIONS =
            Set.of(
                    "ARQ",
                    "ART"
            );


    /*
     * Sharadar source column
     *      ↓
     * sharadar_fundamentals DB column
     */
    private static final List<ColumnMapping> NUMERIC_COLUMNS =
            List.of(

                    new ColumnMapping(
                            "assets",
                            "assets"
                    ),

                    new ColumnMapping(
                            "assetsc",
                            "current_assets"
                    ),

                    new ColumnMapping(
                            "liabilities",
                            "liabilities"
                    ),

                    new ColumnMapping(
                            "liabilitiesc",
                            "current_liabilities"
                    ),

                    new ColumnMapping(
                            "equity",
                            "equity"
                    ),

                    new ColumnMapping(
                            "debt",
                            "debt"
                    ),

                    new ColumnMapping(
                            "debtc",
                            "current_debt"
                    ),

                    new ColumnMapping(
                            "debtnc",
                            "noncurrent_debt"
                    ),

                    new ColumnMapping(
                            "cashneq",
                            "cash_and_equivalents"
                    ),

                    new ColumnMapping(
                            "inventory",
                            "inventory"
                    ),

                    new ColumnMapping(
                            "receivables",
                            "receivables"
                    ),

                    new ColumnMapping(
                            "payables",
                            "payables"
                    ),

                    new ColumnMapping(
                            "investments",
                            "investments"
                    ),

                    new ColumnMapping(
                            "ppnenet",
                            "net_ppe"
                    ),

                    new ColumnMapping(
                            "intangibles",
                            "intangibles"
                    ),

                    new ColumnMapping(
                            "workingcapital",
                            "working_capital"
                    ),


                    new ColumnMapping(
                            "revenue",
                            "revenue"
                    ),

                    new ColumnMapping(
                            "cor",
                            "cost_of_revenue"
                    ),

                    new ColumnMapping(
                            "gp",
                            "gross_profit"
                    ),

                    new ColumnMapping(
                            "opex",
                            "operating_expenses"
                    ),

                    new ColumnMapping(
                            "opinc",
                            "operating_income"
                    ),

                    new ColumnMapping(
                            "ebit",
                            "ebit"
                    ),

                    new ColumnMapping(
                            "ebitda",
                            "ebitda"
                    ),

                    new ColumnMapping(
                            "netinc",
                            "net_income"
                    ),

                    new ColumnMapping(
                            "netinccmn",
                            "net_income_common"
                    ),

                    new ColumnMapping(
                            "rnd",
                            "research_and_development"
                    ),

                    new ColumnMapping(
                            "sgna",
                            "selling_general_admin"
                    ),

                    new ColumnMapping(
                            "sbcomp",
                            "stock_based_compensation"
                    ),

                    new ColumnMapping(
                            "intexp",
                            "interest_expense"
                    ),

                    new ColumnMapping(
                            "taxexp",
                            "tax_expense"
                    ),


                    new ColumnMapping(
                            "ncfo",
                            "operating_cash_flow"
                    ),

                    new ColumnMapping(
                            "capex",
                            "capital_expenditure"
                    ),

                    new ColumnMapping(
                            "fcf",
                            "free_cash_flow"
                    ),

                    new ColumnMapping(
                            "ncfi",
                            "investing_cash_flow"
                    ),

                    new ColumnMapping(
                            "ncff",
                            "financing_cash_flow"
                    ),

                    new ColumnMapping(
                            "ncf",
                            "net_cash_flow"
                    ),


                    new ColumnMapping(
                            "sharesbas",
                            "shares_basic"
                    ),

                    new ColumnMapping(
                            "shareswa",
                            "shares_weighted_average"
                    ),

                    new ColumnMapping(
                            "shareswadil",
                            "shares_weighted_diluted"
                    ),

                    new ColumnMapping(
                            "sharefactor",
                            "share_factor"
                    ),

                    new ColumnMapping(
                            "eps",
                            "eps"
                    ),

                    new ColumnMapping(
                            "epsdil",
                            "diluted_eps"
                    ),

                    new ColumnMapping(
                            "sps",
                            "sales_per_share"
                    ),

                    new ColumnMapping(
                            "fcfps",
                            "free_cash_flow_per_share"
                    ),

                    new ColumnMapping(
                            "bvps",
                            "book_value_per_share"
                    ),

                    new ColumnMapping(
                            "tbvps",
                            "tangible_book_value_per_share"
                    ),

                    new ColumnMapping(
                            "dps",
                            "dividends_per_share"
                    ),


                    new ColumnMapping(
                            "marketcap",
                            "market_cap"
                    ),

                    new ColumnMapping(
                            "ev",
                            "enterprise_value"
                    ),

                    new ColumnMapping(
                            "price",
                            "price"
                    ),

                    new ColumnMapping(
                            "pe",
                            "pe"
                    ),

                    new ColumnMapping(
                            "ps",
                            "ps"
                    ),

                    new ColumnMapping(
                            "pb",
                            "pb"
                    ),

                    new ColumnMapping(
                            "evebit",
                            "ev_to_ebit"
                    ),

                    new ColumnMapping(
                            "evebitda",
                            "ev_to_ebitda"
                    ),


                    new ColumnMapping(
                            "grossmargin",
                            "gross_margin"
                    ),

                    new ColumnMapping(
                            "ebitdamargin",
                            "ebitda_margin"
                    ),

                    new ColumnMapping(
                            "netmargin",
                            "net_margin"
                    ),

                    new ColumnMapping(
                            "roa",
                            "roa"
                    ),

                    new ColumnMapping(
                            "roe",
                            "roe"
                    ),

                    new ColumnMapping(
                            "roic",
                            "roic"
                    ),

                    new ColumnMapping(
                            "currentratio",
                            "current_ratio"
                    ),

                    new ColumnMapping(
                            "de",
                            "debt_to_equity"
                    ),


                    new ColumnMapping(
                            "fxusd",
                            "fx_usd"
                    ),

                    new ColumnMapping(
                            "revenueusd",
                            "revenue_usd"
                    ),

                    new ColumnMapping(
                            "netinccmnusd",
                            "net_income_common_usd"
                    ),

                    new ColumnMapping(
                            "equityusd",
                            "equity_usd"
                    ),

                    new ColumnMapping(
                            "debtusd",
                            "debt_usd"
                    ),

                    new ColumnMapping(
                            "cashnequsd",
                            "cash_and_equivalents_usd"
                    ),

                    new ColumnMapping(
                            "ebitusd",
                            "ebit_usd"
                    ),

                    new ColumnMapping(
                            "ebitdausd",
                            "ebitda_usd"
                    )
            );


    private static final String UPSERT_SQL =
            buildUpsertSql();


    private final StockRepository stockRepository;

    private final DataSource dataSource;


    @Value("${sharadar.fundamentals-file:}")
    private String fundamentalsFile;


    @Value("${sharadar.fundamentals-checkpoint-file:}")
    private String fundamentalsCheckpointFile;


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
                "[SHARADAR FUNDAMENTAL IMPORT] START file={} stocks={} range={}~{} checkpointLine={}",
                sourcePath.toAbsolutePath(),
                stockIdsByTicker.size(),
                BacktestDatasetSpec.FUNDAMENTAL_START,
                BacktestDatasetSpec.FUNDAMENTAL_END,
                checkpointLine
        );


        long sourceRows = 0L;
        long writtenRows = 0L;
        long dimensionSkippedRows = 0L;
        long outsideRangeRows = 0L;
        long tickerNotInUniverseRows = 0L;


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
                    findFundamentalsCsvEntry(
                            zipInputStream
                    );


            if (entry == null) {

                throw new IllegalStateException(
                        "fundamentals.csv was not found in ZIP: "
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
                        "Sharadar fundamentals.csv is empty."
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
                                    UPSERT_SQL
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
                         * 이미 DB commit이 보장된 source line은
                         * parse조차 하지 않고 건너뛴다.
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


                        String ticker =
                                requiredValue(
                                        values,
                                        headerIndex,
                                        "ticker",
                                        lineNumber
                                );


                        String dimension =
                                requiredValue(
                                        values,
                                        headerIndex,
                                        "dimension",
                                        lineNumber
                                )
                                        .toUpperCase(
                                                Locale.ROOT
                                        );


                        if (!ACCEPTED_DIMENSIONS.contains(
                                dimension
                        )) {

                            dimensionSkippedRows++;

                            continue;
                        }


                        LocalDate calendarDate =
                                requiredDate(
                                        values,
                                        headerIndex,
                                        "calendardate",
                                        lineNumber
                                );


                        LocalDate filingDate =
                                requiredDate(
                                        values,
                                        headerIndex,
                                        "date",
                                        lineNumber
                                );


                        /*
                         * 우리가 실제 backtest에서 필요로 하는
                         * fundamental history만 저장한다.
                         *
                         * 또한 observation end 이후에 시장에
                         * 공개된 filing은 현재 백테스트에서
                         * 사용할 수 없으므로 저장하지 않는다.
                         */
                        if (calendarDate.isBefore(
                                BacktestDatasetSpec.FUNDAMENTAL_START
                        )
                                || calendarDate.isAfter(
                                BacktestDatasetSpec.FUNDAMENTAL_END
                        )
                                || filingDate.isAfter(
                                BacktestDatasetSpec.OBSERVATION_END
                        )) {

                            outsideRangeRows++;

                            continue;
                        }


                        Long stockId =
                                stockIdsByTicker.get(
                                        normalizeTicker(
                                                ticker
                                        )
                                );


                        if (stockId == null) {

                            tickerNotInUniverseRows++;

                            continue;
                        }


                        LocalDate reportPeriod =
                                nullableDate(
                                        value(
                                                values,
                                                headerIndex,
                                                "reportperiod"
                                        )
                                );


                        String fiscalPeriod =
                                normalizeNullable(
                                        value(
                                                values,
                                                headerIndex,
                                                "fiscalperiod"
                                        )
                                );


                        LocalDate lastUpdated =
                                nullableDate(
                                        value(
                                                values,
                                                headerIndex,
                                                "lastupdated"
                                        )
                                );


                        bindRow(
                                statement,
                                stockId,
                                dimension,
                                calendarDate,
                                filingDate,
                                reportPeriod,
                                fiscalPeriod,
                                lastUpdated,
                                values,
                                headerIndex
                        );


                        statement.addBatch();

                        batchCount++;


                        if (batchCount >= BATCH_SIZE) {

                            statement.executeBatch();

                            connection.commit();


                            /*
                             * DB commit 성공 후에만
                             * checkpoint를 앞으로 이동시킨다.
                             */
                            writeCheckpoint(
                                    checkpointPath,
                                    lineNumber
                            );


                            writtenRows +=
                                    batchCount;


                            batchCount = 0;


                            log.info(
                                    "[SHARADAR FUNDAMENTAL IMPORT] committed={} sourceLine={}",
                                    writtenRows,
                                    lineNumber
                            );
                        }


                        if (sourceRows
                                % PROGRESS_INTERVAL
                                == 0) {

                            log.info(
                                    "[SHARADAR FUNDAMENTAL IMPORT] sourceRows={} written={} dimensionSkipped={} outsideRange={} tickerNotInUniverse={} sourceLine={}",
                                    sourceRows,
                                    writtenRows,
                                    dimensionSkippedRows,
                                    outsideRangeRows,
                                    tickerNotInUniverseRows,
                                    lineNumber
                            );
                        }
                    }


                    if (batchCount > 0) {

                        statement.executeBatch();

                        connection.commit();


                        writtenRows +=
                                batchCount;


                        batchCount = 0;
                    }


                    /*
                     * 마지막 부분이 모두 skip row였더라도
                     * EOF까지 정상적으로 검사했다는 뜻이므로
                     * 마지막 source line까지 checkpoint를 이동한다.
                     */
                    writeCheckpoint(
                            checkpointPath,
                            lineNumber
                    );


                } catch (Exception e) {

                    connection.rollback();


                    throw new IllegalStateException(
                            "Sharadar fundamental import failed."
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
                    "Failed to import Sharadar fundamentals: "
                            + sourcePath.toAbsolutePath(),
                    e
            );
        }


        ImportResult result =
                new ImportResult(
                        sourceRows,
                        writtenRows,
                        dimensionSkippedRows,
                        outsideRangeRows,
                        tickerNotInUniverseRows
                );


        log.info(
                "[SHARADAR FUNDAMENTAL IMPORT] END {}",
                result
        );


        return result;
    }


    private void bindRow(
            PreparedStatement statement,
            Long stockId,
            String dimension,
            LocalDate calendarDate,
            LocalDate filingDate,
            LocalDate reportPeriod,
            String fiscalPeriod,
            LocalDate lastUpdated,
            String[] values,
            Map<String, Integer> headerIndex
    ) throws Exception {

        int parameterIndex = 1;


        statement.setLong(
                parameterIndex++,
                stockId
        );


        statement.setString(
                parameterIndex++,
                dimension
        );


        statement.setDate(
                parameterIndex++,
                Date.valueOf(
                        calendarDate
                )
        );


        statement.setDate(
                parameterIndex++,
                Date.valueOf(
                        filingDate
                )
        );


        setDate(
                statement,
                parameterIndex++,
                reportPeriod
        );


        setString(
                statement,
                parameterIndex++,
                fiscalPeriod
        );


        setDate(
                statement,
                parameterIndex++,
                lastUpdated
        );


        for (
                ColumnMapping mapping
                : NUMERIC_COLUMNS
        ) {

            BigDecimal numericValue =
                    nullableDecimal(
                            value(
                                    values,
                                    headerIndex,
                                    mapping.sourceColumn()
                            )
                    );


            setBigDecimal(
                    statement,
                    parameterIndex++,
                    numericValue
            );
        }
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

        if (fundamentalsFile == null
                || fundamentalsFile.isBlank()) {

            throw new IllegalStateException(
                    "sharadar.fundamentals-file "
                            + "is not configured."
            );
        }


        Path path =
                Path.of(
                        fundamentalsFile
                );


        if (!Files.exists(path)) {

            throw new IllegalStateException(
                    "Sharadar fundamentals ZIP not found: "
                            + path.toAbsolutePath()
            );
        }


        return path;
    }


    private Path validateAndGetCheckpointPath() {

        if (fundamentalsCheckpointFile == null
                || fundamentalsCheckpointFile.isBlank()) {

            throw new IllegalStateException(
                    "sharadar.fundamentals-checkpoint-file "
                            + "is not configured."
            );
        }


        return Path.of(
                fundamentalsCheckpointFile
        );
    }


    private ZipEntry findFundamentalsCsvEntry(
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
                    "fundamentals.csv"
            )) {

                return entry;
            }
        }


        return null;
    }


    private Map<String, Integer> buildHeaderIndex(
            String headerLine
    ) {

        String normalizedHeader =
                removeBom(
                        headerLine
                );


        String[] columns =
                parseCsvLine(
                        normalizedHeader
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

        Set<String> required =
                new HashSet<>(
                        Set.of(
                                "ticker",
                                "dimension",
                                "calendardate",
                                "date",
                                "reportperiod",
                                "fiscalperiod",
                                "lastupdated"
                        )
                );


        for (
                ColumnMapping mapping
                : NUMERIC_COLUMNS
        ) {

            required.add(
                    mapping.sourceColumn()
            );
        }


        List<String> missing =
                required.stream()
                        .filter(
                                column ->
                                        !headerIndex.containsKey(
                                                column
                                        )
                        )
                        .sorted()
                        .toList();


        if (!missing.isEmpty()) {

            throw new IllegalStateException(
                    "Missing required Sharadar fundamentals columns: "
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


    private LocalDate nullableDate(
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


    private void setDate(
            PreparedStatement statement,
            int index,
            LocalDate value
    ) throws Exception {

        if (value == null) {

            statement.setNull(
                    index,
                    Types.DATE
            );

            return;
        }


        statement.setDate(
                index,
                Date.valueOf(
                        value
                )
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


    private void setBigDecimal(
            PreparedStatement statement,
            int index,
            BigDecimal value
    ) throws Exception {

        if (value == null) {

            statement.setNull(
                    index,
                    Types.DECIMAL
            );

            return;
        }


        statement.setBigDecimal(
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
                    "Failed to read fundamental checkpoint: "
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
                    "Failed to write fundamental checkpoint."
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


    private static String buildUpsertSql() {

        List<String> columns =
                new ArrayList<>(
                        List.of(
                                "stock_id",
                                "dimension",
                                "calendar_date",
                                "filing_date",
                                "report_period",
                                "fiscal_period",
                                "source_last_updated"
                        )
                );


        for (
                ColumnMapping mapping
                : NUMERIC_COLUMNS
        ) {

            columns.add(
                    mapping.databaseColumn()
            );
        }


        StringJoiner columnJoiner =
                new StringJoiner(
                        ", "
                );


        StringJoiner placeholderJoiner =
                new StringJoiner(
                        ", "
                );


        for (
                String column
                : columns
        ) {

            columnJoiner.add(
                    column
            );

            placeholderJoiner.add(
                    "?"
            );
        }


        StringJoiner updateJoiner =
                new StringJoiner(
                        ", "
                );


        for (
                String column
                : columns
        ) {

            if ("stock_id".equals(
                    column
            )
                    || "dimension".equals(
                    column
            )
                    || "calendar_date".equals(
                    column
            )) {

                continue;
            }


            updateJoiner.add(
                    column
                            + " = VALUES("
                            + column
                            + ")"
            );
        }


        return """
                INSERT INTO sharadar_fundamentals (
                """
                + columnJoiner
                + """
                )
                VALUES (
                """
                + placeholderJoiner
                + """
                )
                ON DUPLICATE KEY UPDATE
                """
                + updateJoiner;
    }


    private record ColumnMapping(
            String sourceColumn,
            String databaseColumn
    ) {
    }


    public record ImportResult(
            long sourceRows,
            long writtenRows,
            long dimensionSkippedRows,
            long outsideRangeRows,
            long tickerNotInUniverseRows
    ) {
    }
}