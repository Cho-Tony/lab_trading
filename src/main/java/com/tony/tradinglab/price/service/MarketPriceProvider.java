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
         * MarketPriceProvider는 조회만 담당한다.
         *
         * 가격 데이터 적재는
         * MarketDataSyncService.syncRange()의 책임이다.
         */
        Optional<StockPrice> stored =
                stockPriceRepository
                        .findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(
                                stock.getId(),
                                observationDate
                        );


        if (stored.isPresent()
                && isRecentEnough(
                stored.get(),
                observationDate
        )) {

            return stored;
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