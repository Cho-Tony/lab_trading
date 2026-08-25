package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.RevenueGrowth;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RevenueGrowthCalculator {

    public List<RevenueGrowth> calculate(
            List<QuarterlyFact> revenues
    ) {

        Map<String, QuarterlyFact> quarterMap =
                revenues.stream()
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        fact -> fact,
                                        this::selectPreferredFact
                                )
                        );

        return revenues.stream()
                .sorted(
                        Comparator
                                .comparing(QuarterlyFact::fiscalYear)
                                .thenComparing(
                                        q -> quarterOrder(q.fiscalQuarter())
                                )
                )
                .map(current -> {

                    String previousYearKey =
                            createKey(
                                    current.fiscalYear() - 1,
                                    current.fiscalQuarter()
                            );

                    QuarterlyFact previous =
                            quarterMap.get(previousYearKey);

                    if (previous == null) {
                        return null;
                    }

                    BigDecimal yoyGrowth =
                            calculatePercentChange(
                                    previous.value(),
                                    current.value()
                            );

                    return new RevenueGrowth(
                            current.fiscalYear(),
                            current.fiscalQuarter(),

                            current.value(),
                            previous.value(),

                            yoyGrowth,

                            current.filedDate()
                    );
                })
                .filter(growth -> growth != null)
                .toList();
    }

    private BigDecimal calculatePercentChange(
            BigDecimal previous,
            BigDecimal current
    ) {

        if (previous == null
                || current == null
                || previous.compareTo(BigDecimal.ZERO) == 0) {

            return null;
        }

        return current
                .subtract(previous)
                .divide(
                        previous,
                        8,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private String createKey(
            QuarterlyFact fact
    ) {

        return createKey(
                fact.fiscalYear(),
                fact.fiscalQuarter()
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

    private QuarterlyFact selectPreferredFact(
            QuarterlyFact first,
            QuarterlyFact second
    ) {

        /*
         * 같은 FY/Q가 중복될 경우
         * 최초 시장 공개 데이터를 사용한다.
         *
         * 백테스트에서 미래 수정공시를
         * 실수로 사용하는 것을 방지하기 위함.
         */

        if (first.filedDate()
                .isBefore(second.filedDate())) {

            return first;
        }

        return second;
    }

    private int quarterOrder(
            String quarter
    ) {

        return switch (quarter) {

            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;

            default -> 99;
        };
    }
}