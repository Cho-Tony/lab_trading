package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

        if (asOfDate == null) {

            throw new IllegalArgumentException(
                    "asOfDate는 필수입니다."
            );
        }


        /*
         * 먼저 해당 종목의 재무 데이터가
         * DB에 하나라도 존재하는지 확인한다.
         */
        List<FinancialStatementData> stored =
                queryService.findAll(
                        symbol,
                        exchange
                );


        /*
         * DB가 완전히 비어 있을 때만
         * 최초 SEC API sync를 실행한다.
         */
        if (stored.isEmpty()) {

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
        return queryService.findAsOf(
                symbol,
                exchange,
                asOfDate
        );
    }
}