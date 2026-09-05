package com.tony.tradinglab.fundamental.sec;

import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SecCashFlowQuarterNormalizer {

    public List<QuarterlyFact> normalize(
            List<SecFactPoint> facts
    ) {

        Map<Integer, List<SecFactPoint>> factsByFiscalYear =
                facts.stream()
                        .filter(this::isValid)
                        .collect(
                                Collectors.groupingBy(
                                        SecFactPoint::fiscalYear
                                )
                        );

        List<QuarterlyFact> result =
                new ArrayList<>();

        for (Map.Entry<Integer, List<SecFactPoint>> entry
                : factsByFiscalYear.entrySet()) {

            Integer fiscalYear =
                    entry.getKey();

            List<SecFactPoint> fiscalYearFacts =
                    entry.getValue();

            SecFactPoint q1 =
                    findByDuration(
                            fiscalYearFacts,
                            70,
                            120
                    );

            SecFactPoint halfYear =
                    findByDuration(
                            fiscalYearFacts,
                            150,
                            210
                    );

            SecFactPoint nineMonths =
                    findByDuration(
                            fiscalYearFacts,
                            240,
                            300
                    );

            SecFactPoint annual =
                    findByDuration(
                            fiscalYearFacts,
                            330,
                            400
                    );

            if (q1 != null) {

                result.add(
                        createQuarter(
                                q1,
                                fiscalYear,
                                "Q1",
                                q1.value(),
                                false
                        )
                );
            }

            if (q1 != null && halfYear != null) {

                BigDecimal q2Value =
                        halfYear.value()
                                .subtract(
                                        q1.value()
                                );

                result.add(
                        createQuarter(
                                halfYear,
                                fiscalYear,
                                "Q2",
                                q2Value,
                                true
                        )
                );
            }

            if (halfYear != null
                    && nineMonths != null) {

                BigDecimal q3Value =
                        nineMonths.value()
                                .subtract(
                                        halfYear.value()
                                );

                result.add(
                        createQuarter(
                                nineMonths,
                                fiscalYear,
                                "Q3",
                                q3Value,
                                true
                        )
                );
            }

            if (nineMonths != null
                    && annual != null) {

                BigDecimal q4Value =
                        annual.value()
                                .subtract(
                                        nineMonths.value()
                                );

                result.add(
                        createQuarter(
                                annual,
                                fiscalYear,
                                "Q4",
                                q4Value,
                                true
                        )
                );
            }
        }

        return result.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        QuarterlyFact::fiscalYear
                                )
                                .thenComparing(
                                        fact ->
                                                quarterOrder(
                                                        fact.fiscalQuarter()
                                                )
                                )
                )
                .toList();
    }

    private QuarterlyFact createQuarter(
            SecFactPoint source,
            Integer fiscalYear,
            String fiscalQuarter,
            BigDecimal value,
            boolean derived
    ) {

        return new QuarterlyFact(
                source.tag(),
                value,
                source.startDate(),
                source.endDate(),
                source.filedDate(),
                fiscalYear,
                fiscalQuarter,
                derived
        );
    }

    private SecFactPoint findByDuration(
            List<SecFactPoint> facts,
            long minimumDays,
            long maximumDays
    ) {

        return facts.stream()
                .filter(fact -> {

                    long days =
                            ChronoUnit.DAYS.between(
                                    fact.startDate(),
                                    fact.endDate()
                            );

                    return days >= minimumDays
                            && days <= maximumDays;
                })
                .min(
                        Comparator.comparing(
                                SecFactPoint::filedDate
                        )
                )
                .orElse(null);
    }

    private boolean isValid(
            SecFactPoint fact
    ) {

        return fact.fiscalYear() != null
                && fact.startDate() != null
                && fact.endDate() != null
                && fact.filedDate() != null
                && fact.value() != null;
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