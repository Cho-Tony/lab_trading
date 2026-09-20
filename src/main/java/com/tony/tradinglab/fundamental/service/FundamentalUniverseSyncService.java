package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.service.StockMasterService;
import com.tony.tradinglab.universe.UniverseTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundamentalUniverseSyncService {

    private final StockMasterService stockMasterService;

    private final FundamentalDataSyncService fundamentalDataSyncService;


    public List<SyncResult> sync(
            List<UniverseTarget> targets
    ) {

        if (targets == null
                || targets.isEmpty()) {

            return List.of();
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
                            target
                    )
            );
        }


        return List.copyOf(
                results
        );
    }


    private SyncResult syncTarget(
            UniverseTarget target
    ) {

        try {

            Stock stock =
                    stockMasterService.ensureStock(
                            target.symbol(),
                            target.name(),
                            target.exchange(),
                            target.market(),
                            target.currency()
                    );


            int insertedStatements =
                    fundamentalDataSyncService.sync(
                            stock.getSymbol(),
                            stock.getExchange()
                    );


            return new SyncResult(
                    stock.getId(),
                    stock.getSymbol(),
                    stock.getExchange(),
                    insertedStatements,
                    SyncStatus.SUCCESS,
                    null
            );

        } catch (RuntimeException e) {

            log.error(
                    "Fundamental sync failed. symbol={}, exchange={}",
                    target.symbol(),
                    target.exchange(),
                    e
            );


            return new SyncResult(
                    null,
                    target.symbol(),
                    target.exchange(),
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

            int insertedStatements,

            SyncStatus status,

            String errorMessage

    ) {
    }
}