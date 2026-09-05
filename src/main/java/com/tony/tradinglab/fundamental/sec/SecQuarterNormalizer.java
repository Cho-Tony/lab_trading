package com.tony.tradinglab.fundamental.sec;

import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SecQuarterNormalizer {

    public List<QuarterlyFact> normalize(
            List<SecFactPoint> facts
    ) {

        List<SecFactPoint> validFacts = facts.stream()
                .filter(f -> f.startDate() != null)
                .filter(f -> f.endDate() != null)
                .filter(f -> f.filedDate() != null)
                .toList();

        List<QuarterlyFact> standaloneQuarters =
                extractStandaloneQuarters(validFacts);

        List<QuarterlyFact> derivedFourthQuarters =
                deriveFourthQuarters(
                        validFacts,
                        standaloneQuarters
                );

        List<QuarterlyFact> result = new ArrayList<>();

        result.addAll(standaloneQuarters);
        result.addAll(derivedFourthQuarters);

        return result.stream()
                .sorted(
                        Comparator.comparing(
                                QuarterlyFact::endDate
                        )
                )
                .toList();
    }

    private List<QuarterlyFact> extractStandaloneQuarters(
            List<SecFactPoint> facts
    ) {

        Map<String, SecFactPoint> uniquePeriods =
                new LinkedHashMap<>();

        facts.stream()
                .filter(this::isQuarterlyFact)
                .sorted(
                        Comparator.comparing(
                                SecFactPoint::filedDate
                        )
                )
                .forEach(fact -> {

                    String key =
                            fact.startDate()
                                    + "_"
                                    + fact.endDate();

                    /*
                     * 같은 분기 데이터가 후속 공시에서
                     * 비교 데이터로 다시 등장할 수 있다.
                     *
                     * 최초 공시 데이터를 유지한다.
                     */
                    uniquePeriods.putIfAbsent(
                            key,
                            fact
                    );
                });

        return uniquePeriods.values()
                .stream()
                .map(fact ->
                        new QuarterlyFact(
                                fact.tag(),
                                fact.value(),
                                fact.startDate(),
                                fact.endDate(),
                                fact.filedDate(),
                                fact.fiscalYear(),
                                fact.fiscalPeriod(),
                                false
                        )
                )
                .toList();
    }

    private List<QuarterlyFact> deriveFourthQuarters(
            List<SecFactPoint> facts,
            List<QuarterlyFact> standaloneQuarters
    ) {

        List<SecFactPoint> annualFacts =
                extractAnnualFacts(facts);

        List<QuarterlyFact> result =
                new ArrayList<>();

        for (SecFactPoint annual : annualFacts) {

            List<QuarterlyFact> quartersInYear =
                    standaloneQuarters.stream()
                            .filter(q ->
                                    !q.startDate()
                                            .isBefore(
                                                    annual.startDate()
                                            )
                            )
                            .filter(q ->
                                    q.endDate()
                                            .isBefore(
                                                    annual.endDate()
                                            )
                            )
                            .filter(q ->
                                    !q.endDate()
                                            .isAfter(
                                                    annual.endDate()
                                            )
                            )
                            .sorted(
                                    Comparator.comparing(
                                            QuarterlyFact::endDate
                                    )
                            )
                            .toList();

            if (quartersInYear.size() != 3) {
                continue;
            }

            BigDecimal firstThreeQuarterTotal =
                    quartersInYear.stream()
                            .map(QuarterlyFact::value)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal fourthQuarterValue =
                    annual.value()
                            .subtract(
                                    firstThreeQuarterTotal
                            );

            QuarterlyFact thirdQuarter =
                    quartersInYear.get(2);

            result.add(
                    new QuarterlyFact(
                            annual.tag(),
                            fourthQuarterValue,

                            thirdQuarter.endDate()
                                    .plusDays(1),

                            annual.endDate(),

                            /*
                             * Q4 숫자는 10-K가 공시된 순간부터
                             * 시장이 알 수 있다.
                             */
                            annual.filedDate(),

                            annual.fiscalYear(),
                            "Q4",
                            true
                    )
            );
        }

        return result;
    }

    private List<SecFactPoint> extractAnnualFacts(
            List<SecFactPoint> facts
    ) {

        Map<String, SecFactPoint> uniquePeriods =
                new LinkedHashMap<>();

        facts.stream()
                .filter(this::isAnnualFact)
                .sorted(
                        Comparator.comparing(
                                SecFactPoint::filedDate
                        )
                )
                .forEach(fact -> {

                    String key =
                            fact.startDate()
                                    + "_"
                                    + fact.endDate();

                    uniquePeriods.putIfAbsent(
                            key,
                            fact
                    );
                });

        return uniquePeriods.values()
                .stream()
                .toList();
    }

    private boolean isQuarterlyFact(
            SecFactPoint fact
    ) {

        if (!fact.form().startsWith("10-Q")) {
            return false;
        }

        long days =
                ChronoUnit.DAYS.between(
                        fact.startDate(),
                        fact.endDate()
                );

        /*
         * 일반적인 분기는 약 13주.
         * 6개월/9개월 누적값을 제거하기 위한 범위.
         */
        return days >= 70 && days <= 120;
    }

    private boolean isAnnualFact(
            SecFactPoint fact
    ) {

        if (!fact.form().startsWith("10-K")) {
            return false;
        }

        long days =
                ChronoUnit.DAYS.between(
                        fact.startDate(),
                        fact.endDate()
                );

        return days >= 300 && days <= 400;
    }
}