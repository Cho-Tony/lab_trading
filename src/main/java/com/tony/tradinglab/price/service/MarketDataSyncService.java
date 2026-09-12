package com.tony.tradinglab.price.service;

import com.tony.tradinglab.marketdata.client.MarketDataClient;
import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.price.repository.StockPriceRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MarketDataSyncService {

    private final MarketDataClient marketDataClient;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;


    public MarketDataSyncService(
            MarketDataClient marketDataClient,
            StockRepository stockRepository,
            StockPriceRepository stockPriceRepository
    ) {

        this.marketDataClient =
                marketDataClient;

        this.stockRepository =
                stockRepository;

        this.stockPriceRepository =
                stockPriceRepository;
    }


    public int sync(
            String symbol,
            String exchange,
            LocalDate startDate,
            LocalDate endDate
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


        if (startDate == null
                || endDate == null) {

            throw new IllegalArgumentException(
                    "startDate와 endDate는 필수입니다."
            );
        }


        if (startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "startDate는 endDate보다 늦을 수 없습니다."
            );
        }


        Stock stock =
                stockRepository
                        .findBySymbolAndExchange(
                                symbol,
                                exchange
                        )

                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Stock이 DB에 없습니다. "
                                                        + symbol
                                                        + " / "
                                                        + exchange
                                        )
                        );


        /*
         * DB에 저장된 가장 최근 가격 확인
         */
        LocalDate apiStartDate =
                stockPriceRepository
                        .findTopByStockIdOrderByTradeDateDesc(
                                stock.getId()
                        )

                        .map(
                                latest ->
                                        latest.getTradeDate()
                                                .plusDays(1)
                        )

                        .map(
                                nextDate ->
                                        nextDate.isAfter(startDate)
                                                ? nextDate
                                                : startDate
                        )

                        .orElse(
                                startDate
                        );


        /*
         * 이미 요청 기간 전체가 DB에 있다면
         * API 자체를 호출하지 않는다.
         */
        if (apiStartDate.isAfter(endDate)) {

            return 0;
        }


        /*
         * Twelve Data의 end_date 경계 때문에
         * 원하는 endDate까지 포함시키기 위해
         * 하루 뒤 날짜까지 요청한다.
         */
        LocalDate apiEndDate =
                endDate.plusDays(1);


        var dailyPrices =
                marketDataClient.getDailyPrices(
                        symbol,
                        apiStartDate,
                        apiEndDate
                );


        if (dailyPrices == null
                || dailyPrices.isEmpty()) {

            return 0;
        }


        /*
         * 혹시 API가 범위 밖 데이터를 반환하더라도
         * 우리가 요청한 실제 기간 안의 데이터만 저장.
         */
        List<StockPrice> newPrices =
                dailyPrices.stream()

                        .filter(
                                price ->
                                        price != null
                        )

                        .filter(
                                price ->
                                        price.tradeDate() != null
                        )

                        .filter(
                                price ->
                                        !price.tradeDate()
                                                .isBefore(
                                                        apiStartDate
                                                )
                        )

                        .filter(
                                price ->
                                        !price.tradeDate()
                                                .isAfter(
                                                        endDate
                                                )
                        )

                        .map(
                                price ->
                                        new StockPrice(

                                                stock,

                                                price.tradeDate(),

                                                price.open(),
                                                price.high(),
                                                price.low(),
                                                price.close(),

                                                price.adjustedClose(),

                                                price.volume()
                                        )
                        )

                        .toList();


        if (newPrices.isEmpty()) {

            return 0;
        }


        stockPriceRepository.saveAll(
                newPrices
        );


        return newPrices.size();
    }
}