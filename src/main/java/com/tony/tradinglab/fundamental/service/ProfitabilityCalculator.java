package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.Profitability;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ProfitabilityCalculator {

    private static final BigDecimal HUNDRED =
            new BigDecimal("100");

    public List<Profitability> calculate(
            List<QuarterlyFinancials> financials
    ) {

        return financials.stream()
                .map(this::calculate)
                .toList();
    }

    private Profitability calculate(
            QuarterlyFinancials financial
    ) {

        BigDecimal operatingMarginPct =
                calculateMargin(
                        financial.operatingIncome(),
                        financial.revenue()
                );

        BigDecimal netMarginPct =
                calculateMargin(
                        financial.netIncome(),
                        financial.revenue()
                );

        return new Profitability(
                financial.fiscalYear(),
                financial.fiscalQuarter(),

                financial.revenue(),
                financial.operatingIncome(),
                financial.netIncome(),

                operatingMarginPct,
                netMarginPct,

                financial.filedDate()
        );
    }

    private BigDecimal calculateMargin(
            BigDecimal income,
            BigDecimal revenue
    ) {

        if (income == null
                || revenue == null
                || revenue.compareTo(BigDecimal.ZERO) == 0) {

            return null;
        }

        return income
                .divide(
                        revenue,
                        6,
                        RoundingMode.HALF_UP
                )
                .multiply(HUNDRED)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }
}