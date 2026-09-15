package com.tony.tradinglab.price.service;

import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.price.repository.StockPriceRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketPriceProvider {

    /*
     * observationDate 기준으로
     * 이 정도 이내의 이전 거래일 가격이면
     * 정상적인 최근 가격으로 인정한다.
     *
     * 주말 / 연휴 등을 고려.
     */
    private static final long MAX_STALE_DAYS = 7;


    private final StockRepository stockRepository;

    private final StockPriceRepository stockPriceRepository;

    private final MarketDataSyncService marketDataSyncService;


    public Optional<StockPrice> getAsOf(
            String symbol,
            String exchange,
            LocalDate observationDate
    ) {

        if (symbol == null
                || symbol.isBlank()) {

            throw new IllegalArgumentException(
                    "symbol은 필수입니다."
            );
        }


        if (exchange == null
                || exchange.isBlank()) {

            throw new IllegalArgumentException(
                    "exchange는 필수입니다."
            );
        }


        if (observationDate == null) {

            throw new IllegalArgumentException(
                    "observationDate는 필수입니다."
            );
        }


        String normalizedSymbol =
                symbol.trim()
                        .toUpperCase();

        String normalizedExchange =
                exchange.trim()
                        .toUpperCase();


        Stock stock =
                stockRepository
                        .findBySymbolAndExchange(
                                normalizedSymbol,
                                normalizedExchange
                        )

                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Stock이 DB에 없습니다. "
                                                        + normalizedSymbol
                                                        + " / "
                                                        + normalizedExchange
                                        )
                        );


        /*
         * 1.
         * 먼저 DB에서 observationDate 이하의
         * 가장 최근 가격을 찾는다.
         */
        Optional<StockPrice> stored =
                stockPriceRepository
                        .findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(
                                stock.getId(),
                                observationDate
                        );


        /*
         * 2.
         * 충분히 최근 데이터가 DB에 있다면
         * API를 호출하지 않고 바로 반환.
         *
         * 예:
         *
         * observationDate = 일요일
         * DB latest       = 금요일
         *
         * → 정상적인 가격이므로 API 호출 X
         */
        if (stored.isPresent()
                && isRecentEnough(
                stored.get(),
                observationDate
        )) {

            return stored;
        }


        /*
         * 3.
         * DB에 가격이 없거나
         * 너무 오래된 가격밖에 없다면
         *
         * observationDate 이전 10일 정도를
         * Twelve Data에서 backfill한다.
         */
        LocalDate backfillStart =
                observationDate.minusDays(
                        10
                );


        marketDataSyncService.syncRange(
                normalizedSymbol,
                normalizedExchange,
                backfillStart,
                observationDate
        );


        /*
         * 4.
         * API 응답을 직접 반환하지 않고
         * 반드시 DB에서 다시 읽는다.
         */
        Optional<StockPrice> refreshed =
                stockPriceRepository
                        .findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(
                                stock.getId(),
                                observationDate
                        );


        /*
         * API 호출 후에도
         * 너무 오래된 가격밖에 없다면
         * 사용할 수 없는 데이터로 본다.
         */
        if (refreshed.isPresent()
                && isRecentEnough(
                refreshed.get(),
                observationDate
        )) {

            return refreshed;
        }


        return Optional.empty();
    }


    private boolean isRecentEnough(
            StockPrice price,
            LocalDate observationDate
    ) {

        long days =
                ChronoUnit.DAYS.between(
                        price.getTradeDate(),
                        observationDate
                );


        return days >= 0
                && days <= MAX_STALE_DAYS;
    }
}