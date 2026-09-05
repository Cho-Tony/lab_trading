package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import com.tony.tradinglab.fundamental.domain.TtmFinancials;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class TtmFinancialsCalculator {

    private static final int TTM_QUARTERS = 4;

    public List<TtmFinancials> calculate(
            List<QuarterlyFinancials> quarterlyFinancials
    ) {

        List<QuarterlyFinancials> sorted =
                quarterlyFinancials.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                QuarterlyFinancials::fiscalYear
                                        )
                                        .thenComparingInt(
                                                financials ->
                                                        quarterNumber(
                                                                financials.fiscalQuarter()
                                                        )
                                        )
                        )
                        .toList();


        List<TtmFinancials> result =
                new ArrayList<>();


        for (int i = TTM_QUARTERS - 1;
             i < sorted.size();
             i++) {

            List<QuarterlyFinancials> window =
                    sorted.subList(
                            i - TTM_QUARTERS + 1,
                            i + 1
                    );


            /*
             * 반드시 연속된 4개 분기여야 한다.
             */
            if (!isConsecutive(window)) {
                continue;
            }


            /*
             * 핵심 값이 하나라도 없으면
             * 해당 TTM 구간은 만들지 않는다.
             */
            if (hasMissingFinancialData(window)) {
                continue;
            }


            QuarterlyFinancials latest =
                    window.get(window.size() - 1);


            BigDecimal revenue =
                    sumRevenue(window);

            BigDecimal operatingIncome =
                    sumOperatingIncome(window);

            BigDecimal netIncome =
                    sumNetIncome(window);

            LocalDate filedDate =
                    latestFiledDate(window);


            result.add(
                    new TtmFinancials(

                            latest.fiscalYear(),
                            latest.fiscalQuarter(),

                            revenue,
                            operatingIncome,
                            netIncome,

                            filedDate
                    )
            );
        }


        return List.copyOf(result);
    }


    private BigDecimal sumRevenue(
            List<QuarterlyFinancials> window
    ) {

        return window.stream()
                .map(
                        QuarterlyFinancials::revenue
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    private BigDecimal sumOperatingIncome(
            List<QuarterlyFinancials> window
    ) {

        return window.stream()
                .map(
                        QuarterlyFinancials::operatingIncome
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    private BigDecimal sumNetIncome(
            List<QuarterlyFinancials> window
    ) {

        return window.stream()
                .map(
                        QuarterlyFinancials::netIncome
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    private boolean hasMissingFinancialData(
            List<QuarterlyFinancials> window
    ) {

        return window.stream()
                .anyMatch(
                        financials ->
                                financials.revenue() == null
                                        || financials.operatingIncome() == null
                                        || financials.netIncome() == null
                );
    }


    private LocalDate latestFiledDate(
            List<QuarterlyFinancials> window
    ) {

        return window.stream()
                .map(
                        QuarterlyFinancials::filedDate
                )
                .filter(
                        filedDate ->
                                filedDate != null
                )
                .max(
                        LocalDate::compareTo
                )
                .orElse(null);
    }


    private boolean isConsecutive(
            List<QuarterlyFinancials> window
    ) {

        for (int i = 1;
             i < window.size();
             i++) {

            QuarterlyFinancials previous =
                    window.get(i - 1);

            QuarterlyFinancials current =
                    window.get(i);


            if (!isNextQuarter(
                    previous,
                    current
            )) {

                return false;
            }
        }

        return true;
    }


    private boolean isNextQuarter(
            QuarterlyFinancials previous,
            QuarterlyFinancials current
    ) {

        int previousQuarter =
                quarterNumber(
                        previous.fiscalQuarter()
                );

        int currentQuarter =
                quarterNumber(
                        current.fiscalQuarter()
                );


        /*
         * 같은 회계연도
         *
         * Q1 → Q2
         * Q2 → Q3
         * Q3 → Q4
         */
        if (current.fiscalYear()
                .equals(previous.fiscalYear())) {

            return currentQuarter
                    == previousQuarter + 1;
        }


        /*
         * 연도 변경
         *
         * 2024 Q4 → 2025 Q1
         */
        return previousQuarter == 4
                && currentQuarter == 1
                && current.fiscalYear()
                == previous.fiscalYear() + 1;
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
                            "Unknown fiscal quarter: "
                                    + fiscalQuarter
                    );
        };
    }
}