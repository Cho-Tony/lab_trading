package com.tony.tradinglab.smoke;

import com.tony.tradinglab.backtest.outcome.FutureReturnCalculator;
import com.tony.tradinglab.backtest.outcome.FutureReturnResult;
import com.tony.tradinglab.backtest.outcome.TerminalOutcomeType;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@Profile("future-return-smoke")
@RequiredArgsConstructor
public class FutureReturnSmokeRunner
        implements CommandLineRunner {

    private static final String FIND_STOCK_ID_BY_SYMBOL_SQL = """
            SELECT id
            FROM stocks
            WHERE data_source = 'SHARADAR'
              AND UPPER(symbol) = UPPER(?)
            ORDER BY id
            LIMIT 1
            """;


    private static final String FIND_ACQUISITION_SAMPLE_SQL = """
            SELECT
                s.id,
                s.listing_end_date
            FROM stocks s
            WHERE s.data_source = 'SHARADAR'
              AND UPPER(s.security_type) = 'COMMON STOCK'
              AND s.listing_end_date >= '2012-01-01'
              AND s.listing_end_date <= '2025-09-30'
              AND s.listing_start_date <=
                  DATE_SUB(
                      s.listing_end_date,
                      INTERVAL 6 MONTH
                  )

              AND EXISTS (
                  SELECT 1
                  FROM sharadar_corporate_actions a
                  WHERE a.stock_id = s.id
                    AND a.action_date = s.listing_end_date
                    AND a.action_type LIKE 'acquisition%'
              )

              AND NOT EXISTS (
                  SELECT 1
                  FROM sharadar_corporate_actions a
                  WHERE a.stock_id = s.id
                    AND a.action_date = s.listing_end_date
                    AND a.action_type = 'bankruptcyliquidation'
              )

              AND NOT EXISTS (
                  SELECT 1
                  FROM sharadar_corporate_actions a
                  WHERE a.stock_id = s.id
                    AND a.action_type = 'spinoff'
                    AND a.action_date BETWEEN
                        s.listing_end_date
                        AND DATE_ADD(
                            s.listing_end_date,
                            INTERVAL 7 DAY
                        )
              )

            ORDER BY s.listing_end_date
            LIMIT 1
            """;


    private static final String FIND_BANKRUPTCY_SAMPLE_SQL = """
            SELECT
                s.id,
                s.listing_end_date
            FROM stocks s
            WHERE s.data_source = 'SHARADAR'
              AND UPPER(s.security_type) = 'COMMON STOCK'
              AND s.listing_end_date >= '2012-01-01'
              AND s.listing_end_date <= '2025-09-30'
              AND s.listing_start_date <=
                  DATE_SUB(
                      s.listing_end_date,
                      INTERVAL 6 MONTH
                  )

              AND EXISTS (
                  SELECT 1
                  FROM sharadar_corporate_actions a
                  WHERE a.stock_id = s.id
                    AND a.action_date = s.listing_end_date
                    AND a.action_type = 'bankruptcyliquidation'
              )

            ORDER BY s.listing_end_date
            LIMIT 1
            """;


    private final StockRepository stockRepository;

    private final FutureReturnCalculator futureReturnCalculator;

    private final JdbcTemplate jdbcTemplate;


    @Override
    public void run(
            String... args
    ) {

        System.out.println();
        System.out.println(
                "======================================"
        );
        System.out.println(
                " FUTURE RETURN SMOKE"
        );
        System.out.println(
                "======================================"
        );


        runSurvivorCase();

        runAcquisitionCase();

        runBankruptcyCase();

        runSpinoffContinuationCase();


        System.out.println();
        System.out.println(
                "======================================"
        );
        System.out.println(
                " FUTURE RETURN SMOKE PASSED"
        );
        System.out.println(
                "======================================"
        );
    }


    private void runSurvivorCase() {

        Stock stock =
                findSharadarStockBySymbol(
                        "AAPL"
                );


        LocalDate observationDate =
                LocalDate.of(
                        2020,
                        3,
                        31
                );


        FutureReturnResult result =
                requireResult(
                        stock,
                        observationDate,
                        12
                );


        if (result.terminalBeforeTarget()) {

            throw new IllegalStateException(
                    "AAPL survivor case was unexpectedly terminal."
            );
        }


        if (result.returnRate() == null) {

            throw new IllegalStateException(
                    "AAPL survivor returnRate is null."
            );
        }


        printResult(
                "SURVIVOR",
                stock,
                result
        );
    }


    private void runAcquisitionCase() {

        TerminalSample sample =
                findTerminalSample(
                        FIND_ACQUISITION_SAMPLE_SQL
                );


        Stock stock =
                findStockById(
                        sample.stockId()
                );


        LocalDate observationDate =
                sample.listingEndDate()
                        .minusMonths(
                                3
                        );


        FutureReturnResult result =
                requireResult(
                        stock,
                        observationDate,
                        6
                );


        assertTerminalType(
                stock,
                result,
                TerminalOutcomeType.ACQUISITION
        );


        printResult(
                "ACQUISITION",
                stock,
                result
        );
    }


    private void runBankruptcyCase() {

        TerminalSample sample =
                findTerminalSample(
                        FIND_BANKRUPTCY_SAMPLE_SQL
                );


        Stock stock =
                findStockById(
                        sample.stockId()
                );


        LocalDate observationDate =
                sample.listingEndDate()
                        .minusMonths(
                                3
                        );


        FutureReturnResult result =
                requireResult(
                        stock,
                        observationDate,
                        6
                );


        assertTerminalType(
                stock,
                result,
                TerminalOutcomeType.BANKRUPTCY_LIQUIDATION
        );


        printResult(
                "BANKRUPTCY",
                stock,
                result
        );
    }


    private void runSpinoffContinuationCase() {

        Stock stock =
                findSharadarStockBySymbol(
                        "YHOO"
                );


        LocalDate listingEndDate =
                stock.getListingEndDate();


        if (listingEndDate == null) {

            throw new IllegalStateException(
                    "YHOO listingEndDate is null."
            );
        }


        LocalDate observationDate =
                listingEndDate.minusMonths(
                        3
                );


        FutureReturnResult result =
                requireResult(
                        stock,
                        observationDate,
                        6
                );


        assertTerminalType(
                stock,
                result,
                TerminalOutcomeType.SPINOFF_CONTINUATION
        );


        printResult(
                "SPINOFF_CONTINUATION",
                stock,
                result
        );
    }


    private FutureReturnResult requireResult(
            Stock stock,
            LocalDate observationDate,
            int horizonMonths
    ) {

        Optional<FutureReturnResult> result =
                futureReturnCalculator.calculate(
                        stock,
                        observationDate,
                        horizonMonths
                );


        if (result.isEmpty()) {

            throw new IllegalStateException(
                    "Future return result is empty."
                            + " symbol="
                            + stock.getSymbol()
                            + " observationDate="
                            + observationDate
                            + " horizonMonths="
                            + horizonMonths
            );
        }


        return result.get();
    }


    private void assertTerminalType(
            Stock stock,
            FutureReturnResult result,
            TerminalOutcomeType expectedType
    ) {

        if (!result.terminalBeforeTarget()) {

            throw new IllegalStateException(
                    "Expected terminal result."
                            + " symbol="
                            + stock.getSymbol()
            );
        }


        if (result.terminalOutcomeType()
                != expectedType) {

            throw new IllegalStateException(
                    "Unexpected terminal type."
                            + " symbol="
                            + stock.getSymbol()
                            + " expected="
                            + expectedType
                            + " actual="
                            + result.terminalOutcomeType()
            );
        }


        /*
         * 아직 terminal-return policy를
         * 구현하지 않았으므로 이 둘은 NULL이어야 한다.
         */
        if (result.exitAdjustedClose() != null) {

            throw new IllegalStateException(
                    "Terminal exitAdjustedClose must still be null."
                            + " symbol="
                            + stock.getSymbol()
            );
        }


        if (result.returnRate() != null) {

            throw new IllegalStateException(
                    "Terminal returnRate must still be null."
                            + " symbol="
                            + stock.getSymbol()
            );
        }
    }


    private Stock findSharadarStockBySymbol(
            String symbol
    ) {

        Long stockId =
                jdbcTemplate.queryForObject(
                        FIND_STOCK_ID_BY_SYMBOL_SQL,
                        Long.class,
                        symbol
                );


        if (stockId == null) {

            throw new IllegalStateException(
                    "Sharadar stock not found: "
                            + symbol
            );
        }


        return findStockById(
                stockId
        );
    }


    private Stock findStockById(
            Long stockId
    ) {

        return stockRepository.findById(
                        stockId
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Stock not found."
                                                + " stockId="
                                                + stockId
                                )
                );
    }


    private TerminalSample findTerminalSample(
            String sql
    ) {

        return jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                new TerminalSample(
                                        rs.getLong(
                                                "id"
                                        ),
                                        rs.getObject(
                                                "listing_end_date",
                                                LocalDate.class
                                        )
                                )
                )
                .stream()
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Terminal sample not found."
                                )
                );
    }


    private void printResult(
            String label,
            Stock stock,
            FutureReturnResult result
    ) {

        System.out.println();

        System.out.println(
                "--------------------------------------"
        );

        System.out.println(
                label
        );

        System.out.println(
                "symbol              = "
                        + stock.getSymbol()
        );

        System.out.println(
                "listingEndDate      = "
                        + stock.getListingEndDate()
        );

        System.out.println(
                "observationDate     = "
                        + result.observationDate()
        );

        System.out.println(
                "targetDate          = "
                        + result.targetDate()
        );

        System.out.println(
                "entryPriceDate      = "
                        + result.entryPriceDate()
        );

        System.out.println(
                "exitPriceDate       = "
                        + result.exitPriceDate()
        );

        System.out.println(
                "entryAdjustedClose  = "
                        + result.entryAdjustedClose()
        );

        System.out.println(
                "exitAdjustedClose   = "
                        + result.exitAdjustedClose()
        );

        System.out.println(
                "returnRate          = "
                        + result.returnRate()
        );

        System.out.println(
                "terminal            = "
                        + result.terminalBeforeTarget()
        );

        System.out.println(
                "terminalType        = "
                        + result.terminalOutcomeType()
        );
    }


    private record TerminalSample(
            Long stockId,
            LocalDate listingEndDate
    ) {
    }
}