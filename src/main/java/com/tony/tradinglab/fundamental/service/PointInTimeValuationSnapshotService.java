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

            return Optional.empty();
        }


        /*
         * PIT safety check.
         *
         * observationDate 이후에 공개된 재무 데이터가
         * 하나라도 섞여 있으면 look-ahead bias이므로
         * 즉시 실패시킨다.
         */
        boolean hasFutureStatement =
                statements.stream()
                        .anyMatch(
                                statement ->
                                        statement.filedDate() == null
                                                || statement.filedDate()
                                                .isAfter(
                                                        observationDate
                                                )
                        );


        if (hasFutureStatement) {

            throw new IllegalStateException(
                    "Future financial statement detected: "
                            + normalizedSymbol
                            + " / observationDate="
                            + observationDate
            );
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

            return Optional.empty();
        }


        /*
         * 5. PIT Shares Outstanding
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

            return Optional.empty();
        }


        StockPrice price =
                priceOptional.get();


        /*
         * PIT safety check.
         *
         * observationDate 이후의 가격을 사용하면
         * 미래 가격을 본 것이므로 즉시 실패시킨다.
         */
        if (price.getTradeDate() == null
                || price.getTradeDate()
                .isAfter(
                        observationDate
                )) {

            throw new IllegalStateException(
                    "Future market price detected: "
                            + normalizedSymbol
                            + " / observationDate="
                            + observationDate
                            + " / priceDate="
                            + price.getTradeDate()
            );
        }


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
        return snapshotInputFactory
                .create(

                        stock.getId(),
                        normalizedSymbol,
                        observationDate,

                        valuation,

                        fundamental.growth(),
                        fundamental.profitability()
                );
    }

}