package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.persistence.FinancialStatementEntity;
import com.tony.tradinglab.fundamental.persistence.FinancialStatementRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialStatementQueryService {

    private final StockRepository stockRepository;

    private final FinancialStatementRepository
            financialStatementRepository;


    public List<FinancialStatementData> findAll(
            String symbol,
            String exchange
    ) {

        Stock stock =
                findStock(
                        symbol,
                        exchange
                );


        return financialStatementRepository
                .findByStockIdOrderByPeriodEndDateAscFiledDateAsc(
                        stock.getId()
                )

                .stream()

                .map(
                        entity ->
                                toData(
                                        stock.getSymbol(),
                                        entity
                                )
                )

                .toList();
    }

    public List<FinancialStatementData> findAsOf(
            String symbol,
            String exchange,
            LocalDate asOfDate
    ) {

        if (asOfDate == null) {

            throw new IllegalArgumentException(
                    "asOfDate는 필수입니다."
            );
        }


        Stock stock =
                findStock(
                        symbol,
                        exchange
                );


        List<FinancialStatementData> statements =
                financialStatementRepository
                        .findByStockIdAndFiledDateLessThanEqualOrderByPeriodEndDateAscFiledDateAsc(
                                stock.getId(),
                                asOfDate
                        )

                        .stream()

                        .map(
                                entity ->
                                        toData(
                                                stock.getSymbol(),
                                                entity
                                        )
                        )

                        .filter(
                                data ->
                                        data.fiscalYear() != null
                                                && data.fiscalQuarter() != null
                        )

                        .toList();


        Map<String, FinancialStatementData> latestByQuarter =
                statements.stream()

                        .collect(
                                Collectors.toMap(

                                        data ->
                                                data.fiscalYear()
                                                        + "-"
                                                        + data.fiscalQuarter(),

                                        data ->
                                                data,

                                        this::selectLatestStatement
                                )
                        );


        return latestByQuarter.values()
                .stream()

                .sorted(
                        Comparator
                                .comparing(
                                        FinancialStatementData::fiscalYear
                                )

                                .thenComparingInt(
                                        data ->
                                                quarterNumber(
                                                        data.fiscalQuarter()
                                                )
                                )
                )

                .toList();
    }

    private Stock findStock(
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


        return stockRepository
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
    }


    private FinancialStatementData toData(
            String symbol,
            FinancialStatementEntity entity
    ) {

        return new FinancialStatementData(

                symbol,

                entity.getPeriodEndDate(),
                entity.getFiledDate(),

                entity.getFiscalYear(),
                entity.getFiscalQuarter(),

                entity.getRevenue(),
                entity.getOperatingIncome(),
                entity.getNetIncome(),

                entity.getTotalAssets(),
                entity.getTotalEquity(),
                entity.getTotalDebt(),
                entity.getCash(),

                entity.getOperatingCashFlow(),
                entity.getCapitalExpenditure(),

                entity.getSharesOutstanding()
        );
    }

    private FinancialStatementData selectLatestStatement(
            FinancialStatementData first,
            FinancialStatementData second
    ) {

        int filedDateComparison =
                first.filedDate()
                        .compareTo(
                                second.filedDate()
                        );


        /*
         * 더 늦게 공개된 공시가
         * 해당 시점에서 더 최신 정보다.
         */
        if (filedDateComparison < 0) {
            return second;
        }


        if (filedDateComparison > 0) {
            return first;
        }


        /*
         * filedDate가 동일한 경우,
         * 같은 filing 안에 포함된 전년 동기 비교값일 수 있다.
         *
         * 이 경우 실제 해당 FY/Q에 가까운
         * 더 최신 periodEndDate를 사용한다.
         */
        if (first.periodEndDate()
                .isBefore(
                        second.periodEndDate()
                )) {

            return second;
        }


        return first;
    }

    private int quarterNumber(
            String fiscalQuarter
    ) {

        return switch (fiscalQuarter) {

            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;

            default ->
                    throw new IllegalArgumentException(
                            "지원하지 않는 fiscalQuarter: "
                                    + fiscalQuarter
                    );
        };
    }
}