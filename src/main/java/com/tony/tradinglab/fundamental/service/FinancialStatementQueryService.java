package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.persistence.FinancialStatementEntity;
import com.tony.tradinglab.fundamental.persistence.FinancialStatementRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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


        return financialStatementRepository
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
}