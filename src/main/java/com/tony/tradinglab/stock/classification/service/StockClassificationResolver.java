package com.tony.tradinglab.stock.classification.service;

import com.tony.tradinglab.stock.classification.domain.StockClassification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class StockClassificationResolver {

    public Optional<StockClassification> resolve(
            Long stockId,
            LocalDate asOfDate,
            List<StockClassification> classifications
    ) {

        if (stockId == null
                || asOfDate == null
                || classifications == null) {

            return Optional.empty();
        }


        return classifications.stream()

                /*
                 * 해당 Stock만
                 */
                .filter(
                        classification ->
                                classification != null
                                        && stockId.equals(
                                        classification.stockId()
                                )
                )

                /*
                 * 시작일이 미래면 사용 불가능
                 */
                .filter(
                        classification ->
                                classification.effectiveFrom() != null
                                        && !classification
                                        .effectiveFrom()
                                        .isAfter(asOfDate)
                )

                /*
                 * 종료일 처리
                 *
                 * effectiveTo == null
                 * → 현재까지 유효
                 *
                 * 또는
                 *
                 * asOfDate < effectiveTo
                 */
                .filter(
                        classification ->
                                classification.effectiveTo() == null

                                        || asOfDate.isBefore(
                                        classification.effectiveTo()
                                )
                )

                /*
                 * 여러 개가 걸리면
                 * 가장 최근에 시작된 분류 선택
                 */
                .max(
                        Comparator.comparing(
                                StockClassification::effectiveFrom
                        )
                );
    }
}