package com.tony.tradinglab.marketdata.service;

import com.tony.tradinglab.price.service.MarketDataSyncService;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import com.tony.tradinglab.universe.UniverseTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketPriceUniverseSyncService {

    private final StockRepository stockRepository;

    private final MarketDataSyncService marketDataSyncService;


    public List<SyncResult> sync(
            List<UniverseTarget> targets,
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
                new ArrayList<>(
                        targets.size()
                );


        for (UniverseTarget target : targets) {

            if (target == null) {

                throw new IllegalArgumentException(
                        "target은 null일 수 없습니다."
                );
            }


            results.add(
                    syncTarget(
                            target,
                            startDate,
                            endDate
                    )
            );
        }


        return List.copyOf(
                results
        );
    }


    private SyncResult syncTarget(
            UniverseTarget target,
            LocalDate startDate,
            LocalDate endDate
    ) {

        String symbol =
                target.symbol()
                        .trim()
                        .toUpperCase();

        String exchange =
                target.exchange()
                        .trim()
                        .toUpperCase();


        try {

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


            int insertedPrices =
                    marketDataSyncService.syncRange(
                            symbol,
                            exchange,
                            startDate,
                            endDate
                    );


            return new SyncResult(
                    stock.getId(),
                    symbol,
                    exchange,
                    insertedPrices,
                    SyncStatus.SUCCESS,
                    null
            );

        } catch (RuntimeException e) {

            log.error(
                    "Market price sync failed. symbol={}, exchange={}",
                    symbol,
                    exchange,
                    e
            );


            return new SyncResult(
                    null,
                    symbol,
                    exchange,
                    0,
                    SyncStatus.FAILED,
                    e.getMessage()
            );
        }
    }


    public enum SyncStatus {

        SUCCESS,
        FAILED
    }


    public record SyncResult(

            Long stockId,

            String symbol,
            String exchange,

            int insertedPrices,

            SyncStatus status,

            String errorMessage

    ) {
    }
}