package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.domain.SharesOutstandingSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class SharesOutstandingResolver {

    public Optional<SharesOutstandingSnapshot> resolve(
            List<SecFactPoint> facts,
            LocalDate asOfDate
    ) {

        if (asOfDate == null) {
            throw new IllegalArgumentException(
                    "asOfDate must not be null."
            );
        }


        return facts.stream()

                /*
                 * 잘못된 값 제외
                 */
                .filter(
                        fact ->
                                fact.value() != null
                                        && fact.value()
                                        .compareTo(BigDecimal.ZERO) > 0
                )

                /*
                 * 측정 날짜가 미래면 안 됨
                 */
                .filter(
                        fact ->
                                fact.endDate() != null
                                        && !fact.endDate()
                                        .isAfter(asOfDate)
                )

                /*
                 * 가장 중요:
                 * 당시 실제로 공개되어 있었던 정보만 사용
                 */
                .filter(
                        fact ->
                                fact.filedDate() != null
                                        && !fact.filedDate()
                                        .isAfter(asOfDate)
                )

                /*
                 * 가장 최근 측정일 우선.
                 *
                 * 동일 측정일에 수정 공시 등이 여러 개 있으면
                 * asOfDate까지 공개된 것 중 가장 최근 filing 사용.
                 */
                .max(
                        Comparator
                                .comparing(
                                        SecFactPoint::endDate
                                )
                                .thenComparing(
                                        SecFactPoint::filedDate
                                )
                )

                .map(
                        fact ->
                                new SharesOutstandingSnapshot(

                                        fact.value(),

                                        fact.endDate(),

                                        fact.filedDate(),

                                        fact.tag()
                                )
                );
    }
}