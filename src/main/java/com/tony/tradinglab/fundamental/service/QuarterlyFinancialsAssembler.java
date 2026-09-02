package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class QuarterlyFinancialsAssembler {

    public List<QuarterlyFinancials> assemble(
            List<QuarterlyFact> revenues,
            List<QuarterlyFact> operatingIncomes,
            List<QuarterlyFact> netIncomes
    ) {

        Map<String, QuarterlyFact> revenueMap =
                toQuarterMap(revenues);

        Map<String, QuarterlyFact> operatingIncomeMap =
                toQuarterMap(operatingIncomes);

        Map<String, QuarterlyFact> netIncomeMap =
                toQuarterMap(netIncomes);

        return revenueMap.values()
                .stream()
                .map(revenue -> {

                    String key = createKey(revenue);

                    QuarterlyFact operatingIncome =
                            operatingIncomeMap.get(key);

                    QuarterlyFact netIncome =
                            netIncomeMap.get(key);

                    LocalDate filedDate =
                            latestFiledDate(
                                    revenue,
                                    operatingIncome,
                                    netIncome
                            );

                    return new QuarterlyFinancials(
                            revenue.fiscalYear(),
                            revenue.fiscalQuarter(),

                            revenue.value(),

                            operatingIncome != null
                                    ? operatingIncome.value()
                                    : null,

                            netIncome != null
                                    ? netIncome.value()
                                    : null,

                            filedDate
                    );
                })
                .sorted(
                        Comparator
                                .comparing(
                                        QuarterlyFinancials::fiscalYear
                                )
                                .thenComparing(
                                        q -> quarterOrder(
                                                q.fiscalQuarter()
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

    private String createKey(
            QuarterlyFact fact
    ) {

        return fact.fiscalYear()
                + "-"
                + fact.fiscalQuarter();
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

    private LocalDate latestFiledDate(
            QuarterlyFact... facts
    ) {

        LocalDate latest = null;

        for (QuarterlyFact fact : facts) {

            if (fact == null) {
                continue;
            }

            if (latest == null
                    || fact.filedDate().isAfter(latest)) {

                latest = fact.filedDate();
            }
        }

        return latest;
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
                            "알 수 없는 quarter: " + quarter
                    );
        };
    }
}