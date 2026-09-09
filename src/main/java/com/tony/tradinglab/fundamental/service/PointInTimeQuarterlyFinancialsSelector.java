package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PointInTimeQuarterlyFinancialsSelector {

    public List<QuarterlyFinancials> select(
            List<QuarterlyFinancials> financials,
            LocalDate asOfDate
    ) {

        if (financials == null
                || financials.isEmpty()
                || asOfDate == null) {

            return List.of();
        }


        /*
         * 1.
         * asOfDate 당시 실제로 공시된 데이터만 허용
         */
        List<QuarterlyFinancials> available =
                financials.stream()

                        .filter(
                                Objects::nonNull
                        )

                        .filter(
                                financial ->
                                        financial.filedDate() != null
                                                && !financial
                                                .filedDate()
                                                .isAfter(asOfDate)
                        )

                        .toList();


        /*
         * 2.
         * 동일 FY / Quarter가 여러 번 공시된 경우
         * 당시 사용 가능한 가장 최근 filing을 선택한다.
         *
         * 예:
         *
         * 2024 Q2
         * 2024-08-01 original
         * 2024-09-10 amended
         *
         * asOf = 2024-08-20
         * → 8/1 사용
         *
         * asOf = 2024-10-01
         * → 9/10 사용
         */
        Map<FiscalQuarterKey, QuarterlyFinancials> latestByQuarter =
                available.stream()

                        .collect(
                                Collectors.toMap(

                                        financial ->
                                                new FiscalQuarterKey(
                                                        financial.fiscalYear(),
                                                        financial.fiscalQuarter()
                                                ),

                                        financial ->
                                                financial,

                                        this::selectLaterFiling
                                )
                        );


        /*
         * 3.
         * 오래된 분기 → 최신 분기 순으로 정렬
         */
        return latestByQuarter.values()
                .stream()

                .sorted(
                        Comparator
                                .comparing(
                                        QuarterlyFinancials::fiscalYear
                                )
                                .thenComparing(
                                        QuarterlyFinancials::fiscalQuarter
                                )
                )

                .toList();
    }


    private QuarterlyFinancials selectLaterFiling(
            QuarterlyFinancials first,
            QuarterlyFinancials second
    ) {

        if (second.filedDate()
                .isAfter(
                        first.filedDate()
                )) {

            return second;
        }

        return first;
    }


    private record FiscalQuarterKey(
            Integer fiscalYear,
            String quarter
    ) {
    }
}