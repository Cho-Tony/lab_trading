package com.tony.tradinglab.marketdata.service;

import com.tony.tradinglab.price.service.MarketDataSyncService;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketPriceUniverseSyncService {

    private final StockRepository stockRepository;

    private final MarketDataSyncService marketDataSyncService;


    public List<SyncResult> sync(
            List<Target> targets,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (targets == null
                || targets.isEmpty()) {

            return List.of();
        }


        if (startDate == null
                || endDate == null
                || startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "Invalid price sync date range"
            );
        }


        List<SyncResult> results =
                new ArrayList<>();


        for (Target target : targets) {

            String symbol =
                    target.symbol()
                            .trim()
                            .toUpperCase();

            String exchange =
                    target.exchange()
                            .trim()
                            .toUpperCase();


            Stock stock =
                    stockRepository
                            .findBySymbolAndExchange(
                                    symbol,
                                    exchange
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Stock not found: "
                                                            + symbol
                                                            + " / "
                                                            + exchange
                                            )
                            );


            /*
             * Universe 최초 구축 / 과거 누락 보충이므로
             * 일반 incremental sync()가 아니라
             * syncRange()를 사용한다.
             */
            int inserted =
                    marketDataSyncService.syncRange(
                            symbol,
                            exchange,
                            startDate,
                            endDate
                    );


            results.add(
                    new SyncResult(
                            stock.getId(),
                            symbol,
                            exchange,
                            inserted
                    )
            );
        }


        return results;
    }


    public record Target(
            String symbol,
            String exchange
    ) {
    }


    public record SyncResult(
            Long stockId,
            String symbol,
            String exchange,
            int insertedPrices
    ) {
    }
}