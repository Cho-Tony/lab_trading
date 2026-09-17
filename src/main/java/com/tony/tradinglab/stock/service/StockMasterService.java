package com.tony.tradinglab.stock.service;

import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockMasterService {

    private final StockRepository stockRepository;


    public Stock ensureStock(
            String symbol,
            String name,
            String exchange,
            String market,
            String currency
    ) {

        if (symbol == null
                || symbol.isBlank()) {

            throw new IllegalArgumentException(
                    "symbol은 필수입니다."
            );
        }


        if (name == null
                || name.isBlank()) {

            throw new IllegalArgumentException(
                    "name은 필수입니다."
            );
        }


        if (exchange == null
                || exchange.isBlank()) {

            throw new IllegalArgumentException(
                    "exchange는 필수입니다."
            );
        }


        if (currency == null
                || currency.isBlank()) {

            throw new IllegalArgumentException(
                    "currency는 필수입니다."
            );
        }


        String normalizedSymbol =
                symbol.trim()
                        .toUpperCase();

        String normalizedExchange =
                exchange.trim()
                        .toUpperCase();

        String normalizedCurrency =
                currency.trim()
                        .toUpperCase();


        /*
         * 이미 등록된 종목이면 기존 Stock 사용.
         */
        return stockRepository
                .findBySymbolAndExchange(
                        normalizedSymbol,
                        normalizedExchange
                )

                .orElseGet(
                        () ->
                                stockRepository.save(
                                        new Stock(

                                                normalizedSymbol,
                                                name.trim(),

                                                normalizedExchange,

                                                market == null
                                                        ? null
                                                        : market.trim(),

                                                normalizedCurrency
                                        )
                                )
                );
    }
}