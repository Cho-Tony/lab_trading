package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyCashFlow;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FreeCashFlowCalculator {

    public List<QuarterlyCashFlow> calculate(
            List<QuarterlyFact> operatingCashFlows,
            List<QuarterlyFact> capitalExpenditures
    ) {

        Map<String, QuarterlyFact> operatingCashFlowMap =
                toQuarterMap(operatingCashFlows);

        Map<String, QuarterlyFact> capexMap =
                toQuarterMap(capitalExpenditures);

        return operatingCashFlowMap.values()
                .stream()
                .map(operatingCashFlow -> {

                    String key =
                            createKey(operatingCashFlow);

                    QuarterlyFact capex =
                            capexMap.get(key);

                    if (capex == null) {
                        return null;
                    }

                    return new QuarterlyCashFlow(

                            operatingCashFlow.fiscalYear(),
                            operatingCashFlow.fiscalQuarter(),

                            operatingCashFlow.value(),
                            capex.value(),

                            operatingCashFlow.value()
                                    .subtract(
                                            capex.value()
                                    ),

                            latestFiledDate(
                                    operatingCashFlow.filedDate(),
                                    capex.filedDate()
                            )
                    );
                })
                .filter(cashFlow -> cashFlow != null)
                .sorted(
                        Comparator
                                .comparing(
                                        QuarterlyCashFlow::fiscalYear
                                )
                                .thenComparing(
                                        cashFlow ->
                                                quarterOrder(
                                                        cashFlow.fiscalQuarter()
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