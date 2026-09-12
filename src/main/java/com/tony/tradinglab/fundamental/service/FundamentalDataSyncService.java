package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.FundamentalDataClient;
import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.persistence.FinancialStatementEntity;
import com.tony.tradinglab.fundamental.persistence.FinancialStatementRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FundamentalDataSyncService {

    private final FundamentalDataClient fundamentalDataClient;

    private final StockRepository stockRepository;

    private final FinancialStatementRepository
            financialStatementRepository;


    public FundamentalDataSyncService(
            FundamentalDataClient fundamentalDataClient,
            StockRepository stockRepository,
            FinancialStatementRepository financialStatementRepository
    ) {

        this.fundamentalDataClient =
                fundamentalDataClient;

        this.stockRepository =
                stockRepository;

        this.financialStatementRepository =
                financialStatementRepository;
    }


    @Transactional
    public int sync(
            String symbol,
            String exchange
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


        String normalizedSymbol =
                symbol.trim()
                        .toUpperCase();

        String normalizedExchange =
                exchange.trim()
                        .toUpperCase();


        /*
         * Stock master는 먼저 DB에 존재해야 한다.
         */
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
         * 1. SEC에서 실제 재무데이터 조회
         *
         * 내부적으로 SecFundamentalDataClient가
         * SEC Company Facts를 가져와
         * 분기 재무정보로 정규화한다.
         */
        List<FinancialStatementData> statements =
                fundamentalDataClient
                        .getFinancialStatements(
                                normalizedSymbol
                        );


        if (statements == null
                || statements.isEmpty()) {

            return 0;
        }


        /*
         * 2. 현재 DB에 이미 저장되어 있는
         *    공시 key들을 가져온다.
         *
         * 매 statement마다 exists query를 날리지 않고,
         * 한 번 읽어서 Set으로 비교한다.
         */
        List<FinancialStatementEntity> existing =
                financialStatementRepository
                        .findByStockIdOrderByPeriodEndDateAscFiledDateAsc(
                                stock.getId()
                        );


        Set<FinancialStatementKey> existingKeys =
                new HashSet<>();


        for (FinancialStatementEntity entity : existing) {

            existingKeys.add(
                    new FinancialStatementKey(
                            entity.getPeriodEndDate(),
                            entity.getFiledDate()
                    )
            );
        }


        /*
         * 3. 아직 DB에 없는 공시만 Entity로 변환
         */
        List<FinancialStatementEntity> newEntities =
                new ArrayList<>();


        for (FinancialStatementData data : statements) {

            if (data == null) {
                continue;
            }


            /*
             * DB NOT NULL 컬럼에 필요한 핵심 정보.
             */
            if (data.periodEndDate() == null
                    || data.filedDate() == null
                    || data.fiscalYear() == null
                    || data.fiscalQuarter() == null
                    || data.fiscalQuarter().isBlank()) {

                continue;
            }


            FinancialStatementKey key =
                    new FinancialStatementKey(
                            data.periodEndDate(),
                            data.filedDate()
                    );


            /*
             * DB에 이미 있거나,
             * 이번 API 응답 내부에 동일 key가
             * 중복되어 있으면 저장하지 않는다.
             */
            if (!existingKeys.add(key)) {
                continue;
            }


            FinancialStatementEntity entity =
                    new FinancialStatementEntity(

                            stock,

                            data.periodEndDate(),
                            data.filedDate(),

                            data.fiscalYear(),
                            data.fiscalQuarter(),

                            data.revenue(),
                            data.operatingIncome(),
                            data.netIncome(),

                            data.totalAssets(),
                            data.totalEquity(),
                            data.totalDebt(),
                            data.cash(),

                            data.operatingCashFlow(),
                            data.capitalExpenditure(),

                            data.sharesOutstanding()
                    );


            newEntities.add(
                    entity
            );
        }


        if (newEntities.isEmpty()) {

            return 0;
        }


        /*
         * 4. MySQL에 실제 저장
         */
        financialStatementRepository.saveAll(
                newEntities
        );


        return newEntities.size();
    }


    /*
     * financial_statements의 unique 기준과 동일.
     *
     * stockId는 이 sync 메소드 안에서는 모두
     * 동일 종목이므로 key에서 생략 가능하다.
     */
    private record FinancialStatementKey(
            LocalDate periodEndDate,
            LocalDate filedDate
    ) {
    }
}