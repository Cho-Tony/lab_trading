package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PointInTimeFundamentalContext;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class PointInTimeFundamentalContextFactory {

    private final PointInTimeQuarterlyFinancialsSelector selector;


    public PointInTimeFundamentalContextFactory(
            PointInTimeQuarterlyFinancialsSelector selector
    ) {

        this.selector =
                selector;
    }


    public Optional<PointInTimeFundamentalContext> create(
            Long stockId,
            String symbol,
            LocalDate asOfDate,
            List<QuarterlyFinancials> financials
    ) {

        if (stockId == null
                || symbol == null
                || symbol.isBlank()
                || asOfDate == null
                || financials == null) {

            return Optional.empty();
        }


        List<QuarterlyFinancials> availableFinancials =
                selector.select(
                        financials,
                        asOfDate
                );


        /*
         * 당시 공개된 분기 재무정보가 하나도 없다면
         * 분석 자체를 시작할 수 없다.
         */
        if (availableFinancials.isEmpty()) {

            return Optional.empty();
        }


        return Optional.of(
                new PointInTimeFundamentalContext(

                        stockId,
                        symbol,

                        asOfDate,

                        List.copyOf(
                                availableFinancials
                        )
                )
        );
    }
}