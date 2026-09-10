package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PointInTimeFundamentalContext;
import com.tony.tradinglab.fundamental.domain.TtmFinancials;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PointInTimeTtmFinancialsAnalyzer {

    private final TtmFinancialsCalculator calculator;


    public PointInTimeTtmFinancialsAnalyzer(
            TtmFinancialsCalculator calculator
    ) {

        this.calculator =
                calculator;
    }


    public Optional<TtmFinancials> analyze(
            PointInTimeFundamentalContext context
    ) {

        if (context == null
                || context.quarterlyFinancials() == null
                || context.quarterlyFinancials().isEmpty()) {

            return Optional.empty();
        }


        /*
         * Context에는 이미
         * asOfDate 당시 공개된 분기만 들어 있다.
         *
         * 따라서 Calculator에는
         * PIT-safe financials만 전달된다.
         */
        List<TtmFinancials> ttmFinancials =
                calculator.calculate(
                        context.quarterlyFinancials()
                );


        if (ttmFinancials == null
                || ttmFinancials.isEmpty()) {

            return Optional.empty();
        }


        /*
         * 여러 TTM window 중
         * 가장 최근 fiscal period를 선택.
         */
        return ttmFinancials.stream()

                .filter(
                        Objects::nonNull
                )

                .max(
                        Comparator
                                .comparing(
                                        TtmFinancials::fiscalYear
                                )
                                .thenComparing(
                                        TtmFinancials::fiscalQuarter
                                )
                );
    }
}