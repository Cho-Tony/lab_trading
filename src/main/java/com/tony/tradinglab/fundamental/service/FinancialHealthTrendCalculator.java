package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FinancialHealthMetrics;
import com.tony.tradinglab.fundamental.domain.FinancialHealthTrend;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FinancialHealthTrendCalculator {

    public List<FinancialHealthTrend> calculate(
            List<FinancialHealthMetrics> metrics
    ) {

        Map<String, FinancialHealthMetrics> quarterMap =
                metrics.stream()
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        metric -> metric,
                                        this::selectPreferred
                                )
                        );

        return metrics.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        FinancialHealthMetrics::fiscalYear
                                )
                                .thenComparing(
                                        metric ->
                                                quarterOrder(
                                                        metric.fiscalQuarter()
                                                )
                                )
                )
                .map(current -> {

                    String previousQuarterKey =
                            createPreviousQuarterKey(
                                    current.fiscalYear(),
                                    current.fiscalQuarter()
                            );

                    FinancialHealthMetrics previous =
                            quarterMap.get(
                                    previousQuarterKey
                            );

                    if (previous == null) {
                        return null;
                    }

                    return new FinancialHealthTrend(

                            current.fiscalYear(),
                            current.fiscalQuarter(),

                            current.netDebt(),
                            previous.netDebt(),
                            calculateChange(
                                    previous.netDebt(),
                                    current.netDebt()
                            ),

                            current.debtToEquityRatio(),
                            previous.debtToEquityRatio(),
                            calculateChange(
                                    previous.debtToEquityRatio(),
                                    current.debtToEquityRatio()
                            ),

                            current.cashToDebtRatio(),
                            previous.cashToDebtRatio(),
                            calculateChange(
                                    previous.cashToDebtRatio(),
                                    current.cashToDebtRatio()
                            ),

                            latestFiledDate(
                                    previous.filedDate(),
                                    current.filedDate()
                            )
                    );
                })
                .filter(trend -> trend != null)
                .toList();
    }

    private BigDecimal calculateChange(
            BigDecimal previous,
            BigDecimal current
    ) {

        if (previous == null || current == null) {
            return null;
        }

        return current.subtract(previous);
    }

    private String createPreviousQuarterKey(
            Integer fiscalYear,
            String fiscalQuarter
    ) {

        return switch (fiscalQuarter) {

            case "Q1" ->
                    createKey(
                            fiscalYear - 1,
                            "Q4"
                    );

            case "Q2" ->
                    createKey(
                            fiscalYear,
                            "Q1"
                    );

            case "Q3" ->
                    createKey(
                            fiscalYear,
                            "Q2"
                    );

            case "Q4" ->
                    createKey(
                            fiscalYear,
                            "Q3"
                    );

            default ->
                    throw new IllegalArgumentException(
                            "알 수 없는 quarter: "
                                    + fiscalQuarter
                    );
        };
    }

    private String createKey(
            FinancialHealthMetrics metrics
    ) {

        return createKey(
                metrics.fiscalYear(),
                metrics.fiscalQuarter()
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

    private FinancialHealthMetrics selectPreferred(
            FinancialHealthMetrics first,
            FinancialHealthMetrics second
    ) {

        if (first.filedDate()
                .isBefore(second.filedDate())) {

            return first;
        }

        return second;
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