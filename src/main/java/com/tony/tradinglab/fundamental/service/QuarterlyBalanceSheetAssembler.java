package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyBalanceSheet;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class QuarterlyBalanceSheetAssembler {

    public List<QuarterlyBalanceSheet> assemble(
            List<QuarterlyFact> cashFacts,
            List<QuarterlyFact> assetFacts,
            List<QuarterlyFact> equityFacts
    ) {

        Map<String, QuarterlyFact> cashMap =
                toQuarterMap(cashFacts);

        Map<String, QuarterlyFact> assetMap =
                toQuarterMap(assetFacts);

        Map<String, QuarterlyFact> equityMap =
                toQuarterMap(equityFacts);

        Set<String> keys =
                new HashSet<>();

        keys.addAll(cashMap.keySet());
        keys.addAll(assetMap.keySet());
        keys.addAll(equityMap.keySet());

        return keys.stream()
                .map(key -> {

                    QuarterlyFact cash =
                            cashMap.get(key);

                    QuarterlyFact assets =
                            assetMap.get(key);

                    QuarterlyFact equity =
                            equityMap.get(key);

                    QuarterlyFact reference =
                            Stream.of(
                                            cash,
                                            assets,
                                            equity
                                    )
                                    .filter(fact -> fact != null)
                                    .findFirst()
                                    .orElseThrow();

                    return new QuarterlyBalanceSheet(

                            reference.fiscalYear(),
                            reference.fiscalQuarter(),

                            valueOf(cash),
                            valueOf(assets),
                            valueOf(equity),

                            latestEndDate(
                                    cash,
                                    assets,
                                    equity
                            ),

                            latestFiledDate(
                                    cash,
                                    assets,
                                    equity
                            )
                    );
                })
                .sorted(
                        Comparator
                                .comparing(
                                        QuarterlyBalanceSheet::fiscalYear
                                )
                                .thenComparing(
                                        balanceSheet ->
                                                quarterOrder(
                                                        balanceSheet.fiscalQuarter()
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

    private java.math.BigDecimal valueOf(
            QuarterlyFact fact
    ) {

        if (fact == null) {
            return null;
        }

        return fact.value();
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

    private LocalDate latestEndDate(
            QuarterlyFact... facts
    ) {

        return Stream.of(facts)
                .filter(fact -> fact != null)
                .map(QuarterlyFact::endDate)
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