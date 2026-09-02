package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.Profitability;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrend;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProfitabilityTrendCalculator {

    public List<ProfitabilityTrend> calculate(
            List<Profitability> profitabilities
    ) {

        Map<String, Profitability> quarterMap =
                profitabilities.stream()
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        profitability -> profitability,
                                        this::selectPreferred
                                )
                        );

        return profitabilities.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        Profitability::fiscalYear
                                )
                                .thenComparing(
                                        p -> quarterOrder(
                                                p.fiscalQuarter()
                                        )
                                )
                )
                .map(current -> {

                    String previousQuarterKey =
                            createPreviousQuarterKey(
                                    current.fiscalYear(),
                                    current.fiscalQuarter()
                            );

                    Profitability previous =
                            quarterMap.get(
                                    previousQuarterKey
                            );

                    if (previous == null) {
                        return null;
                    }

                    BigDecimal operatingMarginChange =
                            calculateChange(
                                    previous.operatingMarginPct(),
                                    current.operatingMarginPct()
                            );

                    BigDecimal netMarginChange =
                            calculateChange(
                                    previous.netMarginPct(),
                                    current.netMarginPct()
                            );

                    return new ProfitabilityTrend(
                            current.fiscalYear(),
                            current.fiscalQuarter(),

                            current.operatingMarginPct(),
                            previous.operatingMarginPct(),
                            operatingMarginChange,

                            current.netMarginPct(),
                            previous.netMarginPct(),
                            netMarginChange,

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
            Profitability profitability
    ) {

        return createKey(
                profitability.fiscalYear(),
                profitability.fiscalQuarter()
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

    private Profitability selectPreferred(
            Profitability first,
            Profitability second
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