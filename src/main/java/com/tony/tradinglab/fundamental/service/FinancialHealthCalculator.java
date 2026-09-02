package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FinancialHealthMetrics;
import com.tony.tradinglab.fundamental.domain.QuarterlyBalanceSheet;
import com.tony.tradinglab.fundamental.domain.QuarterlyDebt;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FinancialHealthCalculator {

    public List<FinancialHealthMetrics> calculate(
            List<QuarterlyBalanceSheet> balanceSheets,
            List<QuarterlyDebt> debts
    ) {

        Map<String, QuarterlyDebt> debtMap =
                debts.stream()
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        debt -> debt
                                )
                        );

        return balanceSheets.stream()
                .map(balanceSheet -> {

                    String key =
                            createKey(balanceSheet);

                    QuarterlyDebt debt =
                            debtMap.get(key);

                    if (debt == null) {
                        return null;
                    }

                    BigDecimal netDebt =
                            calculateNetDebt(
                                    debt.totalDebt(),
                                    balanceSheet.cash()
                            );

                    BigDecimal debtToEquity =
                            calculateDebtToEquity(
                                    debt.totalDebt(),
                                    balanceSheet.totalEquity()
                            );

                    BigDecimal cashToDebt =
                            calculateCashToDebt(
                                    balanceSheet.cash(),
                                    debt.totalDebt()
                            );

                    return new FinancialHealthMetrics(

                            balanceSheet.fiscalYear(),
                            balanceSheet.fiscalQuarter(),

                            balanceSheet.cash(),
                            balanceSheet.totalAssets(),
                            balanceSheet.totalEquity(),
                            debt.totalDebt(),

                            netDebt,

                            debtToEquity,
                            cashToDebt,

                            latestFiledDate(
                                    balanceSheet.filedDate(),
                                    debt.filedDate()
                            )
                    );
                })
                .filter(metrics -> metrics != null)
                .sorted(
                        Comparator
                                .comparing(
                                        FinancialHealthMetrics::fiscalYear
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

    private BigDecimal calculateNetDebt(
            BigDecimal totalDebt,
            BigDecimal cash
    ) {

        if (totalDebt == null || cash == null) {
            return null;
        }

        return totalDebt.subtract(cash);
    }

    private BigDecimal calculateDebtToEquity(
            BigDecimal totalDebt,
            BigDecimal equity
    ) {

        if (totalDebt == null
                || equity == null
                || equity.compareTo(BigDecimal.ZERO) <= 0) {

            return null;
        }

        return totalDebt.divide(
                equity,
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculateCashToDebt(
            BigDecimal cash,
            BigDecimal totalDebt
    ) {

        if (cash == null
                || totalDebt == null
                || totalDebt.compareTo(BigDecimal.ZERO) == 0) {

            return null;
        }

        return cash.divide(
                totalDebt,
                2,
                RoundingMode.HALF_UP
        );
    }

    private String createKey(
            QuarterlyBalanceSheet balanceSheet
    ) {

        return createKey(
                balanceSheet.fiscalYear(),
                balanceSheet.fiscalQuarter()
        );
    }

    private String createKey(
            QuarterlyDebt debt
    ) {

        return createKey(
                debt.fiscalYear(),
                debt.fiscalQuarter()
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