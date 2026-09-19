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
public class SecCashFlowQuarterNormalizer {

    public List<QuarterlyFact> normalize(
            List<SecFactPoint> facts
    ) {

        if (facts == null
                || facts.isEmpty()) {

            return List.of();
        }


        List<SecFactPoint> validFacts =
                facts.stream()

                        .filter(this::isValid)

                        .toList();


        if (validFacts.isEmpty()) {

            return List.of();
        }


        /*
         * 동일 기간이 후속 공시에서 비교자료로
         * 반복될 수 있으므로 최초 공시를 유지한다.
         */
        List<SecFactPoint> uniqueFacts =
                deduplicateByPeriod(
                        validFacts
                );


        /*
         * 실제 10-K annual 기간을 fiscal year의
         * 기준점으로 사용한다.
         */
        List<SecFactPoint> annualFacts =
                uniqueFacts.stream()

                        .filter(this::isAnnualFact)

                        .sorted(
                                Comparator.comparing(
                                        SecFactPoint::endDate
                                )
                        )

                        .toList();


        if (annualFacts.isEmpty()) {

            return List.of();
        }


        /*
         * Revenue normalizer와 동일한 fiscal-year
         * naming convention을 사용한다.
         */
        int fiscalYearOffset =
                resolveFiscalYearOffset(
                        annualFacts
                );


        List<QuarterlyFact> result =
                new ArrayList<>();


        SecFactPoint latestAnnual =
                null;

        int latestFiscalYear =
                Integer.MIN_VALUE;


        for (SecFactPoint annual : annualFacts) {

            LocalDate fiscalStart =
                    annual.startDate();


            /*
             * 해당 annual과 정확히 같은 fiscal-year
             * 시작점을 가진 누적 Cash Flow만 사용한다.
             *
             * Q1:
             * fiscalStart ~ Q1 end
             *
             * H1:
             * fiscalStart ~ Q2 end
             *
             * 9M:
             * fiscalStart ~ Q3 end
             */
            List<SecFactPoint> sameFiscalYear =
                    uniqueFacts.stream()

                            .filter(
                                    fact ->
                                            fact.startDate()
                                                    .equals(
                                                            fiscalStart
                                                    )
                            )

                            .filter(
                                    fact ->
                                            !fact.endDate()
                                                    .isAfter(
                                                            annual.endDate()
                                                    )
                            )

                            .toList();


            SecFactPoint q1 =
                    findByDuration(
                            sameFiscalYear,
                            70,
                            120
                    );


            SecFactPoint halfYear =
                    findByDuration(
                            sameFiscalYear,
                            150,
                            210
                    );


            SecFactPoint nineMonths =
                    findByDuration(
                            sameFiscalYear,
                            240,
                            300
                    );


            /*
             * 같은 start/end 기간의 annual을
             * 그대로 사용한다.
             */
            SecFactPoint matchedAnnual =
                    sameFiscalYear.stream()

                            .filter(
                                    fact ->
                                            fact.endDate()
                                                    .equals(
                                                            annual.endDate()
                                                    )
                            )

                            .filter(this::isAnnualFact)

                            .min(
                                    Comparator.comparing(
                                            SecFactPoint::filedDate
                                    )
                            )

                            .orElse(
                                    annual
                            );


            int fiscalYear =
                    annual.endDate()
                            .getYear()
                            + fiscalYearOffset;


            /*
             * Q1
             */
            if (q1 != null) {

                result.add(
                        new QuarterlyFact(

                                q1.tag(),
                                q1.value(),

                                q1.startDate(),
                                q1.endDate(),

                                q1.filedDate(),

                                fiscalYear,
                                "Q1",

                                false
                        )
                );
            }


            /*
             * Q2 =
             * H1 cumulative - Q1 cumulative
             */
            if (q1 != null
                    && halfYear != null) {

                BigDecimal q2Value =
                        halfYear.value()
                                .subtract(
                                        q1.value()
                                );


                result.add(
                        new QuarterlyFact(

                                halfYear.tag(),
                                q2Value,

                                q1.endDate()
                                        .plusDays(1),

                                halfYear.endDate(),

                                halfYear.filedDate(),

                                fiscalYear,
                                "Q2",

                                true
                        )
                );
            }


            /*
             * Q3 =
             * 9M cumulative - H1 cumulative
             */
            if (halfYear != null
                    && nineMonths != null) {

                BigDecimal q3Value =
                        nineMonths.value()
                                .subtract(
                                        halfYear.value()
                                );


                result.add(
                        new QuarterlyFact(

                                nineMonths.tag(),
                                q3Value,

                                halfYear.endDate()
                                        .plusDays(1),

                                nineMonths.endDate(),

                                nineMonths.filedDate(),

                                fiscalYear,
                                "Q3",

                                true
                        )
                );
            }


            /*
             * Q4 =
             * Annual cumulative - 9M cumulative
             */
            if (nineMonths != null
                    && matchedAnnual != null) {

                BigDecimal q4Value =
                        matchedAnnual.value()
                                .subtract(
                                        nineMonths.value()
                                );


                result.add(
                        new QuarterlyFact(

                                matchedAnnual.tag(),
                                q4Value,

                                nineMonths.endDate()
                                        .plusDays(1),

                                matchedAnnual.endDate(),

                                matchedAnnual.filedDate(),

                                fiscalYear,
                                "Q4",

                                true
                        )
                );
            }


            if (latestAnnual == null
                    || annual.endDate()
                    .isAfter(
                            latestAnnual.endDate()
                    )) {

                latestAnnual =
                        annual;

                latestFiscalYear =
                        fiscalYear;
            }
        }


        /*
         * 아직 10-K가 나오지 않은
         * 현재 열린 fiscal year 처리.
         *
         * 예:
         *
         * FY2026 Q4가 최신 annual이고
         * FY2027 Q1/Q2만 나온 상태.
         */
        if (latestAnnual != null) {

            SecFactPoint completedAnnual =
                    latestAnnual;

            int completedFiscalYear =
                    latestFiscalYear;


            LocalDate openFiscalStart =
                    completedAnnual
                            .endDate()
                            .plusDays(1);


            List<SecFactPoint> openFacts =
                    uniqueFacts.stream()

                            .filter(
                                    fact ->
                                            fact.startDate()
                                                    .equals(
                                                            openFiscalStart
                                                    )
                            )

                            .filter(
                                    fact ->
                                            fact.endDate()
                                                    .isAfter(
                                                            completedAnnual
                                                                    .endDate()
                                                    )
                            )

                            .toList();


            SecFactPoint q1 =
                    findByDuration(
                            openFacts,
                            70,
                            120
                    );


            SecFactPoint halfYear =
                    findByDuration(
                            openFacts,
                            150,
                            210
                    );


            SecFactPoint nineMonths =
                    findByDuration(
                            openFacts,
                            240,
                            300
                    );


            int openFiscalYear =
                    completedFiscalYear + 1;


            if (q1 != null) {

                result.add(
                        new QuarterlyFact(

                                q1.tag(),
                                q1.value(),

                                q1.startDate(),
                                q1.endDate(),

                                q1.filedDate(),

                                openFiscalYear,
                                "Q1",

                                false
                        )
                );
            }


            if (q1 != null
                    && halfYear != null) {

                BigDecimal q2Value =
                        halfYear.value()
                                .subtract(
                                        q1.value()
                                );


                result.add(
                        new QuarterlyFact(

                                halfYear.tag(),
                                q2Value,

                                q1.endDate()
                                        .plusDays(1),

                                halfYear.endDate(),

                                halfYear.filedDate(),

                                openFiscalYear,
                                "Q2",

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
                        new QuarterlyFact(

                                nineMonths.tag(),
                                q3Value,

                                halfYear.endDate()
                                        .plusDays(1),

                                nineMonths.endDate(),

                                nineMonths.filedDate(),

                                openFiscalYear,
                                "Q3",

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
                                .thenComparingInt(
                                        fact ->
                                                quarterOrder(
                                                        fact.fiscalQuarter()
                                                )
                                )
                )

                .toList();
    }


    private List<SecFactPoint> deduplicateByPeriod(
            List<SecFactPoint> facts
    ) {

        Map<String, SecFactPoint> unique =
                new LinkedHashMap<>();


        facts.stream()

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


                            unique.putIfAbsent(
                                    key,
                                    fact
                            );
                        }
                );


        return unique.values()
                .stream()
                .toList();
    }


    private SecFactPoint findByDuration(
            List<SecFactPoint> facts,
            long minimumDays,
            long maximumDays
    ) {

        return facts.stream()

                .filter(
                        fact -> {

                            long days =
                                    ChronoUnit.DAYS.between(
                                            fact.startDate(),
                                            fact.endDate()
                                    );


                            return days >= minimumDays
                                    && days <= maximumDays;
                        }
                )

                /*
                 * 동일 duration 후보가 여러 개면
                 * 최초 공개된 데이터를 사용한다.
                 */
                .min(
                        Comparator.comparing(
                                SecFactPoint::filedDate
                        )
                )

                .orElse(null);
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


    private boolean isAnnualFact(
            SecFactPoint fact
    ) {

        long days =
                ChronoUnit.DAYS.between(
                        fact.startDate(),
                        fact.endDate()
                );


        return fact.form() != null
                && fact.form()
                .startsWith("10-K")
                && days >= 330
                && days <= 400;
    }


    private boolean isValid(
            SecFactPoint fact
    ) {

        return fact != null
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