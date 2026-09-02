package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyDebt;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TotalDebtCalculator {

    public List<QuarterlyDebt> calculate(
            List<QuarterlyFact> shortTermDebts,
            List<QuarterlyFact> currentLongTermDebts,
            List<QuarterlyFact> longTermDebts
    ) {

        Map<String, QuarterlyFact> shortTermMap =
                toQuarterMap(shortTermDebts);

        Map<String, QuarterlyFact> currentLongTermMap =
                toQuarterMap(currentLongTermDebts);

        Map<String, QuarterlyFact> longTermMap =
                toQuarterMap(longTermDebts);

        Set<String> keys =
                new HashSet<>();

        keys.addAll(shortTermMap.keySet());
        keys.addAll(currentLongTermMap.keySet());
        keys.addAll(longTermMap.keySet());

        return keys.stream()
                .map(key -> {

                    QuarterlyFact shortTerm =
                            shortTermMap.get(key);

                    QuarterlyFact currentLongTerm =
                            currentLongTermMap.get(key);

                    QuarterlyFact longTerm =
                            longTermMap.get(key);

                    QuarterlyFact reference =
                            Stream.of(
                                            shortTerm,
                                            currentLongTerm,
                                            longTerm
                                    )
                                    .filter(fact -> fact != null)
                                    .findFirst()
                                    .orElseThrow();

                    BigDecimal shortTermValue =
                            valueOrZero(shortTerm);

                    BigDecimal currentLongTermValue =
                            valueOrZero(currentLongTerm);

                    BigDecimal longTermValue =
                            valueOrZero(longTerm);

                    BigDecimal totalDebt =
                            shortTermValue
                                    .add(currentLongTermValue)
                                    .add(longTermValue);

                    return new QuarterlyDebt(

                            reference.fiscalYear(),
                            reference.fiscalQuarter(),

                            shortTerm != null
                                    ? shortTerm.value()
                                    : null,

                            currentLongTerm != null
                                    ? currentLongTerm.value()
                                    : null,

                            longTerm != null
                                    ? longTerm.value()
                                    : null,

                            totalDebt,

                            latestFiledDate(
                                    shortTerm,
                                    currentLongTerm,
                                    longTerm
                            )
                    );
                })
                .sorted(
                        Comparator
                                .comparing(
                                        QuarterlyDebt::fiscalYear
                                )
                                .thenComparing(
                                        debt ->
                                                quarterOrder(
                                                        debt.fiscalQuarter()
                                                )
                                )
                )
                .toList();
    }

    private Map<String, QuarterlyFact> toQuarterMap(
            List<QuarterlyFact> facts
    ) {

        return facts.stream()
                .collect(
                        Collectors.toMap(
                                this::createKey,
                                fact -> fact,
                                this::selectPreferredFact
                        )
                );
    }

    private BigDecimal valueOrZero(
            QuarterlyFact fact
    ) {

        if (fact == null) {
            return BigDecimal.ZERO;
        }

        return fact.value();
    }

    private QuarterlyFact selectPreferredFact(
            QuarterlyFact first,
            QuarterlyFact second
    ) {

        if (first.filedDate()
                .isBefore(second.filedDate())) {

            return first;
        }

        return second;
    }

    private String createKey(
            QuarterlyFact fact
    ) {

        return fact.fiscalYear()
                + "-"
                + fact.fiscalQuarter();
    }

    private LocalDate latestFiledDate(
            QuarterlyFact... facts
    ) {

        return Stream.of(facts)
                .filter(fact -> fact != null)
                .map(QuarterlyFact::filedDate)
                .filter(date -> date != null)
                .max(LocalDate::compareTo)
                .orElse(null);
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