package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.universe.UniverseTarget;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.service.StockMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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


            results.add(
                    new SyncResult(
                            stock.getId(),
                            stock.getSymbol(),
                            stock.getExchange(),
                            insertedStatements
                    )
            );
        }


        return List.copyOf(
                results
        );
    }

    public record SyncResult(
            Long stockId,
            String symbol,
            String exchange,
            int insertedStatements
    ) {
    }
}