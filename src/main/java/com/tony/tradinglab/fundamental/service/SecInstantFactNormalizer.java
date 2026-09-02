package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SecInstantFactNormalizer {

    public List<QuarterlyFact> normalize(
            List<SecFactPoint> facts
    ) {

        Map<String, SecFactPoint> quarterMap =
                facts.stream()
                        .filter(this::isValid)
                        .filter(this::isQuarterlyOrAnnualFiling)
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        fact -> fact,
                                        this::selectPreferredFact
                                )
                        );

        return quarterMap.values()
                .stream()
                .map(this::toQuarterlyFact)
                .sorted(
                        Comparator
                                .comparing(
                                        QuarterlyFact::fiscalYear
                                )
                                .thenComparing(
                                        fact -> quarterOrder(
                                                fact.fiscalQuarter()
                                        )
                                )
                )
                .toList();
    }

    private QuarterlyFact toQuarterlyFact(
            SecFactPoint fact
    ) {

        return new QuarterlyFact(
                fact.tag(),
                fact.value(),

                null,
                fact.endDate(),

                fact.filedDate(),

                fact.fiscalYear(),
                normalizeQuarter(
                        fact.fiscalPeriod()
                ),

                false
        );
    }

    private boolean isValid(
            SecFactPoint fact
    ) {

        return fact.value() != null
                && fact.endDate() != null
                && fact.filedDate() != null
                && fact.fiscalYear() != null
                && fact.fiscalPeriod() != null;
    }

    private boolean isQuarterlyOrAnnualFiling(
            SecFactPoint fact
    ) {

        return switch (fact.fiscalPeriod()) {

            case "Q1", "Q2", "Q3", "FY" -> true;

            default -> false;
        };
    }

    private String createKey(
            SecFactPoint fact
    ) {

        return fact.fiscalYear()
                + "-"
                + normalizeQuarter(
                fact.fiscalPeriod()
        );
    }

    private SecFactPoint selectPreferredFact(
            SecFactPoint first,
            SecFactPoint second
    ) {

        /*
         * 백테스트에서 미래 데이터를 사용하지 않기 위해
         * 같은 분기 데이터가 여러 개라면
         * 가장 먼저 공개된 데이터를 선택한다.
         */
        if (first.filedDate()
                .isBefore(second.filedDate())) {

            return first;
        }

        return second;
    }

    private String normalizeQuarter(
            String fiscalPeriod
    ) {

        return switch (fiscalPeriod) {

            case "Q1" -> "Q1";
            case "Q2" -> "Q2";
            case "Q3" -> "Q3";

            /*
             * 10-K의 FY 값은 해당 회계연도 마지막 시점,
             * 즉 Q4 말 Balance Sheet로 사용
             */
            case "FY" -> "Q4";

            default ->
                    throw new IllegalArgumentException(
                            "알 수 없는 fiscal period: "
                                    + fiscalPeriod
                    );
        };
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