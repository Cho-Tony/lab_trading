package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.CapitalInvestmentMetrics;
import com.tony.tradinglab.fundamental.domain.QuarterlyCashFlow;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CapitalInvestmentCalculator {

    private static final BigDecimal HUNDRED =
            new BigDecimal("100");

    public List<CapitalInvestmentMetrics> calculate(
            List<QuarterlyCashFlow> cashFlows,
            List<QuarterlyFinancials> financials
    ) {

        Map<String, QuarterlyCashFlow> cashFlowMap =
                cashFlows.stream()
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        cashFlow -> cashFlow
                                )
                        );

        Map<String, QuarterlyFinancials> financialMap =
                financials.stream()
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        financial -> financial
                                )
                        );

        return cashFlows.stream()
                .map(current -> {

                    QuarterlyFinancials financial =
                            financialMap.get(
                                    createKey(current)
                            );

                    if (financial == null) {
                        return null;
                    }

                    QuarterlyCashFlow previousYear =
                            cashFlowMap.get(
                                    createKey(
                                            current.fiscalYear() - 1,
                                            current.fiscalQuarter()
                                    )
                            );

                    BigDecimal previousYearCapex =
                            previousYear != null
                                    ? previousYear.capitalExpenditure()
                                    : null;

                    BigDecimal capexGrowthYoY =
                            calculateGrowth(
                                    previousYearCapex,
                                    current.capitalExpenditure()
                            );

                    BigDecimal capexToRevenue =
                            calculatePercent(
                                    current.capitalExpenditure(),
                                    financial.revenue()
                            );

                    return new CapitalInvestmentMetrics(

                            current.fiscalYear(),
                            current.fiscalQuarter(),

                            financial.revenue(),

                            current.capitalExpenditure(),
                            previousYearCapex,

                            capexGrowthYoY,
                            capexToRevenue,

                            latestFiledDate(
                                    current.filedDate(),
                                    financial.filedDate()
                            )
                    );
                })
                .filter(metrics -> metrics != null)
                .sorted(
                        Comparator
                                .comparing(
                                        CapitalInvestmentMetrics::fiscalYear
                                )
                                .thenComparing(
                                        metrics ->
                                                quarterOrder(
                                                        metrics.fiscalQuarter()
                                                )
                                )
                )
                .toList();
    }

    private BigDecimal calculateGrowth(
            BigDecimal previous,
            BigDecimal current
    ) {

        if (previous == null
                || current == null
                || previous.compareTo(BigDecimal.ZERO) <= 0) {

            return null;
        }

        return current
                .subtract(previous)
                .divide(
                        previous,
                        6,
                        RoundingMode.HALF_UP
                )
                .multiply(HUNDRED)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculatePercent(
            BigDecimal value,
            BigDecimal base
    ) {

        if (value == null
                || base == null
                || base.compareTo(BigDecimal.ZERO) == 0) {

            return null;
        }

        return value
                .divide(
                        base,
                        6,
                        RoundingMode.HALF_UP
                )
                .multiply(HUNDRED)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private String createKey(
            QuarterlyCashFlow cashFlow
    ) {

        return createKey(
                cashFlow.fiscalYear(),
                cashFlow.fiscalQuarter()
        );
    }

    private String createKey(
            QuarterlyFinancials financial
    ) {

        return createKey(
                financial.fiscalYear(),
                financial.fiscalQuarter()
        );
    }

    private String createKey(
            Integer fiscalYear,
            String fiscalQuarter
    ) {

        return fiscalYear
                + "-"
                + fiscalQuarter;
    }

    private LocalDate latestFiledDate(
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

    private int quarterOrder(
            String quarter
    ) {

        return switch (quarter) {

            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;

            default ->
                    throw new IllegalArgumentException(
                            "알 수 없는 quarter: "
                                    + quarter
                    );
        };
    }
}