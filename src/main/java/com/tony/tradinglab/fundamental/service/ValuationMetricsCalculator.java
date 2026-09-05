package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import com.tony.tradinglab.fundamental.domain.TtmFinancials;
import com.tony.tradinglab.fundamental.domain.ValuationMetrics;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class ValuationMetricsCalculator {

    private static final int RATIO_SCALE = 2;

    public ValuationMetrics calculate(
            TtmFinancials financials,
            TtmCashFlow cashFlow,
            LocalDate priceDate,
            BigDecimal sharePrice,
            BigDecimal sharesOutstanding
    ) {

        if (financials == null) {
            throw new IllegalArgumentException(
                    "TtmFinancials must not be null."
            );
        }

        if (priceDate == null) {
            throw new IllegalArgumentException(
                    "Price date must not be null."
            );
        }

        if (sharePrice == null
                || sharePrice.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Share price must be positive."
            );
        }

        if (sharesOutstanding == null
                || sharesOutstanding.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Shares outstanding must be positive."
            );
        }


        BigDecimal marketCap =
                sharePrice.multiply(
                        sharesOutstanding
                );


        BigDecimal peRatio =
                divideIfPositive(
                        marketCap,
                        financials.netIncome()
                );


        BigDecimal psRatio =
                divideIfPositive(
                        marketCap,
                        financials.revenue()
                );


        BigDecimal ttmFcf =
                cashFlow != null
                        ? cashFlow.freeCashFlow()
                        : null;


        BigDecimal priceToFcfRatio =
                divideIfPositive(
                        marketCap,
                        ttmFcf
                );


        LocalDate filedDate =
                latestDate(
                        financials.filedDate(),
                        cashFlow != null
                                ? cashFlow.filedDate()
                                : null
                );


        return new ValuationMetrics(

                financials.fiscalYear(),
                financials.fiscalQuarter(),

                priceDate,
                sharePrice,
                sharesOutstanding,
                marketCap,

                financials.revenue(),
                financials.netIncome(),
                ttmFcf,

                peRatio,
                psRatio,
                priceToFcfRatio,

                filedDate
        );
    }


    private BigDecimal divideIfPositive(
            BigDecimal numerator,
            BigDecimal denominator
    ) {

        if (numerator == null
                || denominator == null
                || denominator.compareTo(BigDecimal.ZERO) <= 0) {

            return null;
        }

        return numerator.divide(
                denominator,
                RATIO_SCALE,
                RoundingMode.HALF_UP
        );
    }


    private LocalDate latestDate(
            LocalDate first,
            LocalDate second
    ) {

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return first.isAfter(second)
                ? first
                : second;
    }
}