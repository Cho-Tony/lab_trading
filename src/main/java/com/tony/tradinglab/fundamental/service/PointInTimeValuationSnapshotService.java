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
         *
         * DB 우선.
         * DB가 완전히 비어 있으면 Provider가
         * SEC sync 후 DB에서 다시 읽는다.
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
         * 3. 기존 Fundamental Analyzer용 입력
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
         * 4. Cash Flow → TTM FCF
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

                        .max(
                                Comparator.comparing(
                                        FinancialStatementData::filedDate
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
         *
         * DB에 usable price가 있으면 API 호출 X.
         * 없으면 MarketPriceProvider가 backfill한다.
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
         *
         * 아직 sector / industry는 여기서 붙이지 않는다.
         * 다음 Assembler 단계에서 PIT classification을 결합한다.
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