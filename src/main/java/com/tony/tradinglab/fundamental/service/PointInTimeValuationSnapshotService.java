package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.domain.*;
import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.price.service.MarketPriceProvider;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointInTimeValuationSnapshotService {

    private final StockRepository stockRepository;

    private final FundamentalDataProvider fundamentalDataProvider;

    private final MarketPriceProvider marketPriceProvider;

    private final PointInTimeFundamentalAnalysisService
            fundamentalAnalysisService;

    private final TtmCashFlowCalculator
            ttmCashFlowCalculator;

    private final ValuationMetricsCalculator
            valuationMetricsCalculator;

    private final ValuationPeerSnapshotInputFactory
            snapshotInputFactory;


    public Optional<ValuationPeerSnapshotInput> analyze(
            String symbol,
            String exchange,
            LocalDate observationDate
    ) {

        if (symbol == null
                || symbol.isBlank()
                || exchange == null
                || exchange.isBlank()
                || observationDate == null) {

            return Optional.empty();
        }


        String normalizedSymbol =
                symbol.trim()
                        .toUpperCase();

        String normalizedExchange =
                exchange.trim()
                        .toUpperCase();


        /*
         * 1. Stock master
         */
        Optional<Stock> stockOptional =
                stockRepository
                        .findBySymbolAndExchange(
                                normalizedSymbol,
                                normalizedExchange
                        );


        if (stockOptional.isEmpty()) {

            System.out.println(
                    "[SNAPSHOT] "
                            + normalizedSymbol
                            + " -> stock not found"
            );

            return Optional.empty();
        }


        Stock stock =
                stockOptional.get();


        /*
         * 2. Fundamental
         */
        List<FinancialStatementData> statements =
                fundamentalDataProvider
                        .getAsOf(
                                normalizedSymbol,
                                normalizedExchange,
                                observationDate
                        );


        if (statements.isEmpty()) {

            System.out.println(
                    "[SNAPSHOT] "
                            + normalizedSymbol
                            + " -> no fundamental statements as of "
                            + observationDate
            );

            return Optional.empty();
        }


        /*
         * 3. Fundamental Analyzer
         */
        List<QuarterlyFinancials> quarterlyFinancials =
                statements.stream()

                        .map(
                                data ->
                                        new QuarterlyFinancials(

                                                data.fiscalYear(),
                                                data.fiscalQuarter(),

                                                data.revenue(),
                                                data.operatingIncome(),
                                                data.netIncome(),

                                                data.filedDate()
                                        )
                        )

                        .toList();


        List<QuarterlyFact> revenueFacts =
                statements.stream()

                        .filter(
                                data ->
                                        data.revenue() != null
                        )

                        .map(
                                data ->
                                        new QuarterlyFact(

                                                "Revenue",

                                                data.revenue(),

                                                null,
                                                data.periodEndDate(),
                                                data.filedDate(),

                                                data.fiscalYear(),
                                                data.fiscalQuarter(),

                                                false
                                        )
                        )

                        .toList();


        PointInTimeFundamentalContext context =
                new PointInTimeFundamentalContext(

                        stock.getId(),
                        normalizedSymbol,
                        observationDate,
                        quarterlyFinancials
                );


        Optional<PointInTimeFundamentalAnalysis>
                fundamentalOptional =
                fundamentalAnalysisService
                        .analyze(
                                context,
                                revenueFacts
                        );


        if (fundamentalOptional.isEmpty()) {

            System.out.println(
                    "[SNAPSHOT] "
                            + normalizedSymbol
                            + " -> fundamental analysis unavailable"
            );

            return Optional.empty();
        }


        PointInTimeFundamentalAnalysis fundamental =
                fundamentalOptional.get();


        TtmFinancials ttmFinancials =
                fundamental.ttmFinancials();


        /*
         * 4. Cash Flow -> TTM FCF
         */
        List<QuarterlyCashFlow> quarterlyCashFlows =
                statements.stream()

                        .filter(
                                data ->
                                        data.operatingCashFlow() != null
                                                && data.capitalExpenditure() != null
                        )

                        .map(
                                data -> {

                                    BigDecimal freeCashFlow =
                                            data.operatingCashFlow()
                                                    .subtract(
                                                            data.capitalExpenditure()
                                                    );


                                    return new QuarterlyCashFlow(

                                            data.fiscalYear(),
                                            data.fiscalQuarter(),

                                            data.operatingCashFlow(),
                                            data.capitalExpenditure(),
                                            freeCashFlow,

                                            data.filedDate()
                                    );
                                }
                        )

                        .toList();


        Optional<TtmCashFlow> ttmCashFlowOptional =
                ttmCashFlowCalculator
                        .calculate(
                                quarterlyCashFlows
                        )

                        .stream()

                        .filter(
                                cashFlow ->
                                        cashFlow.fiscalYear()
                                                .equals(
                                                        ttmFinancials
                                                                .fiscalYear()
                                                )
                        )

                        .filter(
                                cashFlow ->
                                        cashFlow.fiscalQuarter()
                                                .equals(
                                                        ttmFinancials
                                                                .fiscalQuarter()
                                                )
                        )

                        .max(
                                Comparator.comparing(
                                        TtmCashFlow::filedDate
                                )
                        );


        if (ttmCashFlowOptional.isEmpty()) {

            System.out.println(
                    "[SNAPSHOT] "
                            + normalizedSymbol
                            + " -> TTM cash flow unavailable"
                            + " | target="
                            + ttmFinancials.fiscalYear()
                            + " "
                            + ttmFinancials.fiscalQuarter()
                            + " | quarterlyCashFlows="
                            + quarterlyCashFlows.size()
            );

            return Optional.empty();
        }


        /*
         * 5. PIT Shares Outstanding
         *
         * null뿐 아니라 0 이하도 valuation 입력으로 사용할 수 없다.
         */
        Optional<BigDecimal> sharesOptional =
                statements.stream()

                        .filter(
                                data ->
                                        data.sharesOutstanding() != null
                        )

                        .filter(
                                data ->
                                        data.sharesOutstanding()
                                                .signum() > 0
                        )

                        .max(
                                Comparator
                                        .comparing(
                                                FinancialStatementData::filedDate
                                        )
                                        .thenComparing(
                                                FinancialStatementData::periodEndDate
                                        )
                        )

                        .map(
                                FinancialStatementData::sharesOutstanding
                        );


        if (sharesOptional.isEmpty()) {

            System.out.println(
                    "[SNAPSHOT] "
                            + normalizedSymbol
                            + " -> valid shares outstanding unavailable"
            );

            return Optional.empty();
        }


        /*
         * 6. Market Price
         */
        Optional<StockPrice> priceOptional =
                marketPriceProvider
                        .getAsOf(
                                normalizedSymbol,
                                normalizedExchange,
                                observationDate
                        );


        if (priceOptional.isEmpty()) {

            System.out.println(
                    "[SNAPSHOT] "
                            + normalizedSymbol
                            + " -> market price unavailable"
            );

            return Optional.empty();
        }


        StockPrice price =
                priceOptional.get();


        /*
         * 7. Valuation
         */
        ValuationMetrics valuation =
                valuationMetricsCalculator
                        .calculate(

                                ttmFinancials,

                                ttmCashFlowOptional.get(),

                                price.getTradeDate(),

                                price.getClose(),

                                sharesOptional.get()
                        );


        /*
         * 8. Peer Snapshot Input
         */
        Optional<ValuationPeerSnapshotInput> snapshotOptional =
                snapshotInputFactory
                        .create(

                                stock.getId(),
                                normalizedSymbol,
                                observationDate,

                                valuation,

                                fundamental.growth(),
                                fundamental.profitability()
                        );


        if (snapshotOptional.isEmpty()) {

            System.out.println(
                    "[SNAPSHOT] "
                            + normalizedSymbol
                            + " -> snapshot factory rejected input"
            );
        }


        return snapshotOptional;
    }

}