package com.tony.tradinglab.fundamental.sec;

import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SecQuarterNormalizer {

    public List<QuarterlyFact> normalize(
            List<SecFactPoint> facts
    ) {

        if (facts == null
                || facts.isEmpty()) {

            return List.of();
        }


        List<SecFactPoint> validFacts =
                facts.stream()

                        .filter(
                                fact ->
                                        fact != null
                                                && fact.startDate() != null
                                                && fact.endDate() != null
                                                && fact.filedDate() != null
                                                && fact.value() != null
                        )

                        .toList();


        if (validFacts.isEmpty()) {

            return List.of();
        }


        /*
         * 실제 10-Q에 존재하는 standalone
         * 약 3개월짜리 fact만 추출한다.
         *
         * FY / FP 값은 여기서는 아직 신뢰하지 않는다.
         */
        List<SecFactPoint> standaloneFacts =
                extractStandaloneQuarterFacts(
                        validFacts
                );


        /*
         * 실제 10-K annual fact.
         */
        List<SecFactPoint> annualFacts =
                extractAnnualFacts(
                        validFacts
                );


        if (annualFacts.isEmpty()) {

            /*
             * annual boundary 자체가 없는 특수 케이스에서는
             * 기존 SEC metadata를 fallback으로 사용한다.
             */
            return fallbackStandaloneQuarters(
                    standaloneFacts
            );
        }


        /*
         * 회사의 fiscal year naming convention 결정.
         *
         * 예:
         *
         * annual end = 2025-09-27
         * SEC FY     = 2025
         *
         * offset = 0
         *
         * 일부 회사가 일관되게 다음/이전 year number를
         * 사용한다면 그 convention도 유지할 수 있다.
         *
         * 오래된 SEC metadata 오류의 영향을 줄이기 위해
         * 최근 annual facts의 다수 convention을 사용한다.
         */
        int fiscalYearOffset =
                resolveFiscalYearOffset(
                        annualFacts
                );


        List<QuarterlyFact> result =
                new ArrayList<>();


        SecFactPoint latestCompletedAnnual =
                null;

        int latestCompletedFiscalYear =
                Integer.MIN_VALUE;


        for (SecFactPoint annual : annualFacts) {

            /*
             * annual 기간 내부에 실제 10-Q standalone
             * quarter가 정확히 3개 존재하는지 찾는다.
             */
            List<SecFactPoint> quartersInYear =
                    standaloneFacts.stream()

                            .filter(
                                    quarter ->
                                            !quarter.startDate()
                                                    .isBefore(
                                                            annual.startDate()
                                                    )
                            )

                            .filter(
                                    quarter ->
                                            quarter.endDate()
                                                    .isBefore(
                                                            annual.endDate()
                                                    )
                            )

                            .sorted(
                                    Comparator.comparing(
                                            SecFactPoint::endDate
                                    )
                            )

                            .toList();


            /*
             * Q1/Q2/Q3 세 개가 확보되지 않은 annual은
             * 억지로 Q4를 계산하지 않는다.
             */
            if (quartersInYear.size() != 3) {

                continue;
            }


            int fiscalYear =
                    annual.endDate()
                            .getYear()
                            + fiscalYearOffset;


            /*
             * Q1 / Q2 / Q3
             *
             * SEC의 과거 fy/fp 값 대신
             * 실제 annual period 내 시간 순서를 사용한다.
             */
            for (int i = 0;
                 i < quartersInYear.size();
                 i++) {

                SecFactPoint quarter =
                        quartersInYear.get(i);


                result.add(
                        new QuarterlyFact(

                                quarter.tag(),
                                quarter.value(),

                                quarter.startDate(),
                                quarter.endDate(),

                                quarter.filedDate(),

                                fiscalYear,
                                "Q" + (i + 1),

                                false
                        )
                );
            }


            /*
             * Q4 =
             * annual - Q1 - Q2 - Q3
             */
            BigDecimal firstThreeQuarterTotal =
                    quartersInYear.stream()

                            .map(
                                    SecFactPoint::value
                            )

                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );


            BigDecimal fourthQuarterValue =
                    annual.value()
                            .subtract(
                                    firstThreeQuarterTotal
                            );


            SecFactPoint thirdQuarter =
                    quartersInYear.get(2);


            result.add(
                    new QuarterlyFact(

                            annual.tag(),

                            fourthQuarterValue,

                            thirdQuarter.endDate()
                                    .plusDays(1),

                            annual.endDate(),

                            /*
                             * Q4 값은 10-K 공시 시점부터
                             * 시장이 알 수 있다.
                             */
                            annual.filedDate(),

                            fiscalYear,
                            "Q4",

                            true
                    )
            );


            if (latestCompletedAnnual == null
                    || annual.endDate()
                    .isAfter(
                            latestCompletedAnnual.endDate()
                    )) {

                latestCompletedAnnual =
                        annual;

                latestCompletedFiscalYear =
                        fiscalYear;
            }
        }


        /*
         * 아직 10-K가 나오지 않은 현재 fiscal year.
         *
         * 예:
         *
         * 최신 annual = FY2026 Q4
         * 이후 10-Q:
         *
         * 2026-04-26 → FY2027 Q1
         * 2026-07-26 → FY2027 Q2
         */
        if (latestCompletedAnnual != null) {

            /*
             * lambda 안에서는 변경되는 지역변수를
             * 직접 참조할 수 없기 때문에
             *
             * 현재 값을 변경되지 않는 지역변수로
             * 한 번 복사해서 사용한다.
             */
            SecFactPoint completedAnnual =
                    latestCompletedAnnual;

            int completedFiscalYear =
                    latestCompletedFiscalYear;


            List<SecFactPoint> openYearQuarters =
                    standaloneFacts.stream()

                            .filter(
                                    quarter ->
                                            quarter.startDate()
                                                    .isAfter(
                                                            completedAnnual
                                                                    .endDate()
                                                    )
                            )

                            .sorted(
                                    Comparator.comparing(
                                            SecFactPoint::endDate
                                    )
                            )

                            /*
                             * 아직 10-K가 나오지 않은
                             * 현재 fiscal year에서는
                             * 최대 Q1 / Q2 / Q3까지만 존재한다.
                             */
                            .limit(3)

                            .toList();


            int openFiscalYear =
                    completedFiscalYear + 1;


            for (int i = 0;
                 i < openYearQuarters.size();
                 i++) {

                SecFactPoint quarter =
                        openYearQuarters.get(i);


                result.add(
                        new QuarterlyFact(

                                quarter.tag(),
                                quarter.value(),

                                quarter.startDate(),
                                quarter.endDate(),

                                quarter.filedDate(),

                                openFiscalYear,
                                "Q" + (i + 1),

                                false
                        )
                );
            }
        }


        /*
         * annual boundary를 하나도 정상적으로
         * 구성하지 못한 경우에만 fallback.
         */
        if (result.isEmpty()) {

            return fallbackStandaloneQuarters(
                    standaloneFacts
            );
        }


        return result.stream()

                .sorted(
                        Comparator.comparing(
                                QuarterlyFact::endDate
                        )
                )

                .toList();
    }


    private List<SecFactPoint> extractStandaloneQuarterFacts(
            List<SecFactPoint> facts
    ) {

        Map<String, SecFactPoint> uniquePeriods =
                new LinkedHashMap<>();


        facts.stream()

                .filter(
                        this::isQuarterlyFact
                )

                /*
                 * 같은 과거 quarter가 후속 10-Q에서
                 * 비교자료로 다시 등장한다.
                 *
                 * 최초 공시를 유지해야 PIT에 맞다.
                 */
                .sorted(
                        Comparator.comparing(
                                SecFactPoint::filedDate
                        )
                )

                .forEach(
                        fact -> {

                            String key =
                                    fact.startDate()
                                            + "_"
                                            + fact.endDate();


                            uniquePeriods.putIfAbsent(
                                    key,
                                    fact
                            );
                        }
                );


        return uniquePeriods.values()
                .stream()

                .sorted(
                        Comparator.comparing(
                                SecFactPoint::endDate
                        )
                )

                .toList();
    }


    private List<SecFactPoint> extractAnnualFacts(
            List<SecFactPoint> facts
    ) {

        Map<String, SecFactPoint> uniquePeriods =
                new LinkedHashMap<>();


        facts.stream()

                .filter(
                        this::isAnnualFact
                )

                /*
                 * 동일 annual period가 이후 10-K에서
                 * 비교자료로 반복되더라도
                 * 최초 공시본을 유지한다.
                 */
                .sorted(
                        Comparator.comparing(
                                SecFactPoint::filedDate
                        )
                )

                .forEach(
                        fact -> {

                            String key =
                                    fact.startDate()
                                            + "_"
                                            + fact.endDate();


                            uniquePeriods.putIfAbsent(
                                    key,
                                    fact
                            );
                        }
                );


        return uniquePeriods.values()
                .stream()

                .sorted(
                        Comparator.comparing(
                                SecFactPoint::endDate
                        )
                )

                .toList();
    }


    private int resolveFiscalYearOffset(
            List<SecFactPoint> annualFacts
    ) {

        List<SecFactPoint> recentAnnualFacts =
                annualFacts.stream()

                        .filter(
                                fact ->
                                        fact.fiscalYear() != null
                        )

                        .sorted(
                                Comparator.comparing(
                                                SecFactPoint::endDate
                                        )
                                        .reversed()
                        )

                        .limit(5)

                        .toList();


        if (recentAnnualFacts.isEmpty()) {

            return 0;
        }


        Map<Integer, Integer> counts =
                new HashMap<>();


        for (SecFactPoint annual : recentAnnualFacts) {

            int offset =
                    annual.fiscalYear()
                            - annual.endDate()
                            .getYear();


            /*
             * 일반적인 fiscal year naming 차이는
             * -1 / 0 / +1 범위 안에 있다.
             *
             * 그 이상은 metadata 이상치로 본다.
             */
            if (offset < -1
                    || offset > 1) {

                continue;
            }


            counts.merge(
                    offset,
                    1,
                    Integer::sum
            );
        }


        if (counts.isEmpty()) {

            return 0;
        }


        SecFactPoint latest =
                recentAnnualFacts.get(0);


        int latestOffset =
                latest.fiscalYear()
                        - latest.endDate()
                        .getYear();


        int bestOffset =
                latestOffset;

        int bestCount =
                counts.getOrDefault(
                        latestOffset,
                        0
                );


        for (Map.Entry<Integer, Integer> entry
                : counts.entrySet()) {

            if (entry.getValue() > bestCount) {

                bestOffset =
                        entry.getKey();

                bestCount =
                        entry.getValue();
            }
        }


        return bestOffset;
    }


    private List<QuarterlyFact> fallbackStandaloneQuarters(
            List<SecFactPoint> standaloneFacts
    ) {

        return standaloneFacts.stream()

                .filter(
                        fact ->
                                fact.fiscalYear() != null
                                        && fact.fiscalPeriod() != null
                )

                .map(
                        fact ->
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

                .sorted(
                        Comparator.comparing(
                                QuarterlyFact::endDate
                        )
                )

                .toList();
    }


    private boolean isQuarterlyFact(
            SecFactPoint fact
    ) {

        if (fact.form() == null
                || !fact.form()
                .startsWith("10-Q")) {

            return false;
        }


        long days =
                ChronoUnit.DAYS.between(
                        fact.startDate(),
                        fact.endDate()
                );


        /*
         * standalone quarter는 보통 약 13주.
         *
         * 6개월 / 9개월 누적값은 제외.
         */
        return days >= 70
                && days <= 120;
    }


    private boolean isAnnualFact(
            SecFactPoint fact
    ) {

        if (fact.form() == null
                || !fact.form()
                .startsWith("10-K")) {

            return false;
        }


        long days =
                ChronoUnit.DAYS.between(
                        fact.startDate(),
                        fact.endDate()
                );


        return days >= 300
                && days <= 400;
    }
}