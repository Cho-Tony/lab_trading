package com.tony.tradinglab.fundamental.service;

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
            List<Target> targets
    ) {

        if (targets == null
                || targets.isEmpty()) {

            return List.of();
        }


        List<SyncResult> results =
                new ArrayList<>();


        for (Target target : targets) {

            if (target == null) {

                throw new IllegalArgumentException(
                        "target은 null일 수 없습니다."
                );
            }


            /*
             * 1. stocks 테이블에 해당 종목이
             *    반드시 존재하도록 보장.
             */
            Stock stock =
                    stockMasterService.ensureStock(

                            target.symbol(),
                            target.name(),
                            target.exchange(),
                            target.market(),
                            target.currency()
                    );


            /*
             * 2. 실제 SEC API 데이터를 받아
             *    financial_statements에 저장.
             *
             * 이미 저장된 공시는 중복 INSERT하지 않는다.
             */
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


    /*
     * 동기화할 종목 기본정보.
     */
    public record Target(

            String symbol,
            String name,
            String exchange,
            String market,
            String currency

    ) {
    }


    /*
     * 종목별 sync 결과.
     */
    public record SyncResult(

            Long stockId,
            String symbol,
            String exchange,
            int insertedStatements

    ) {
    }
}