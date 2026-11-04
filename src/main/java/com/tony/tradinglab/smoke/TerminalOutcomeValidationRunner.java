package com.tony.tradinglab.smoke;

import com.tony.tradinglab.backtest.outcome.TerminalOutcomeResolver;
import com.tony.tradinglab.backtest.outcome.TerminalOutcomeType;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("terminal-outcome-validation")
@RequiredArgsConstructor
public class TerminalOutcomeValidationRunner
        implements CommandLineRunner {

    private static final LocalDate BACKTEST_START =
            LocalDate.of(
                    2010,
                    3,
                    31
            );

    private static final LocalDate PRICE_END =
            LocalDate.of(
                    2026,
                    9,
                    30
            );


    private final StockRepository stockRepository;

    private final TerminalOutcomeResolver terminalOutcomeResolver;


    @Override
    public void run(
            String... args
    ) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " TERMINAL OUTCOME VALIDATION"
        );

        System.out.println(
                "======================================"
        );


        List<Stock> stocks =
                stockRepository.findAll()
                        .stream()
                        .filter(
                                this::isBacktestRelevantDelistedCommonStock
                        )
                        .toList();


        Map<TerminalOutcomeType, Integer> counts =
                new EnumMap<>(
                        TerminalOutcomeType.class
                );


        for (
                TerminalOutcomeType type
                : TerminalOutcomeType.values()
        ) {

            counts.put(
                    type,
                    0
            );
        }


        int processed = 0;


        for (Stock stock : stocks) {

            TerminalOutcomeType type =
                    terminalOutcomeResolver.resolve(
                            stock
                    );


            counts.merge(
                    type,
                    1,
                    Integer::sum
            );


            processed++;


            if (processed % 500 == 0) {

                System.out.println(
                        "Processed = "
                                + processed
                                + " / "
                                + stocks.size()
                );
            }
        }


        System.out.println();
        System.out.println(
                "Total Stocks = "
                        + stocks.size()
        );

        System.out.println();


        for (
                TerminalOutcomeType type
                : TerminalOutcomeType.values()
        ) {

            System.out.printf(
                    "%-30s = %d%n",
                    type,
                    counts.get(
                            type
                    )
            );
        }


        int totalResolved =
                counts.values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        System.out.println();
        System.out.println(
                "Resolved Total = "
                        + totalResolved
        );


        if (totalResolved
                != stocks.size()) {

            throw new IllegalStateException(
                    "Terminal outcome count mismatch."
                            + " stocks="
                            + stocks.size()
                            + " resolved="
                            + totalResolved
            );
        }


        System.out.println(
                "======================================"
        );
    }


    private boolean isBacktestRelevantDelistedCommonStock(
            Stock stock
    ) {

        if (stock == null) {
            return false;
        }


        if (!"SHARADAR".equalsIgnoreCase(
                stock.getDataSource()
        )) {

            return false;
        }


        if (!"COMMON STOCK".equalsIgnoreCase(
                stock.getSecurityType()
        )) {

            return false;
        }


        LocalDate listingEndDate =
                stock.getListingEndDate();


        if (listingEndDate == null) {
            return false;
        }


        return !listingEndDate.isBefore(
                BACKTEST_START
        )
                && !listingEndDate.isAfter(
                PRICE_END
        );
    }
}