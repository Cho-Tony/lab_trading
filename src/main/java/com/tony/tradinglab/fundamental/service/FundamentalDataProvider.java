package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.domain.FundamentalDataCollectionPolicy;
import com.tony.tradinglab.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundamentalDataProvider {

    private final FinancialStatementQueryService
            queryService;

    private final FundamentalDataSyncService
            syncService;


    public List<FinancialStatementData> getAsOf(
            String symbol,
            String exchange,
            LocalDate asOfDate
    ) {
        return getAsOf(symbol, exchange, asOfDate, FundamentalDataCollectionPolicy.AUTO_SYNC_IF_EMPTY);
    }

    public List<FinancialStatementData> getAsOf(
            String symbol,
            String exchange,
            LocalDate asOfDate,
            FundamentalDataCollectionPolicy collectionPolicy
    ) {

        if (asOfDate == null) {

            throw new IllegalArgumentException(
                    "asOfDate는 필수입니다."
            );
        }

        Objects.requireNonNull(collectionPolicy, "collectionPolicy is required");


        /*
         * 먼저 해당 종목의 재무 데이터가
         * DB에 하나라도 존재하는지 확인한다.
         */
        Stock stock = queryService.findStockAsOf(symbol, exchange, asOfDate);

        List<FinancialStatementData> stored =
                queryService.findAll(
                        stock.getId()
                );


        /*
         * Only an explicitly permitted collection path may bootstrap missing data.
         * Backtests use READ_ONLY regardless of the observation date.
         */
        if (stored.isEmpty()
                && collectionPolicy == FundamentalDataCollectionPolicy.AUTO_SYNC_IF_EMPTY) {

            syncService.sync(
                    symbol,
                    exchange
            );
        }


        /*
         * 실제 분석 데이터는 항상 DB에서 조회한다.
         *
         * 따라서 API 응답을 직접 분석에 사용하지 않는다.
         */
        List<FinancialStatementData> available = queryService.findAsOf(
                stock.getId(),
                asOfDate
        );

        if (collectionPolicy == FundamentalDataCollectionPolicy.READ_ONLY && available.isEmpty()) {
            log.debug("Financial data unavailable. reason={} stockId={} symbol={} asOfDate={} policy={}",
                    stored.isEmpty() ? "NO_STORED_FINANCIALS" : "NO_FINANCIALS_AS_OF",
                    stock.getId(), symbol, asOfDate, collectionPolicy);
        }
        return available;
    }
}
