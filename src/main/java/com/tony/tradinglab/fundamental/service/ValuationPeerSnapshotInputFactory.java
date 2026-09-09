package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.GrowthTrendAnalysis;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrendAnalysis;
import com.tony.tradinglab.fundamental.domain.ValuationMetrics;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class ValuationPeerSnapshotInputFactory {

    public Optional<ValuationPeerSnapshotInput> create(
            Long stockId,
            String symbol,
            LocalDate observationDate,
            ValuationMetrics valuation,
            GrowthTrendAnalysis growth,
            ProfitabilityTrendAnalysis profitability
    ) {

        if (stockId == null
                || symbol == null
                || symbol.isBlank()
                || observationDate == null
                || valuation == null) {

            return Optional.empty();
        }


        /*
         * valuation 자체가 다른 날짜의 가격을 기준으로
         * 계산된 값이면 사용하면 안 된다.
         *
         * 예:
         *
         * observationDate = 2025-06-30
         * valuation.priceDate = 2025-09-30
         *
         * → 미래 valuation이므로 제외.
         */
        if (valuation.priceDate() == null
                || !valuation.priceDate()
                .equals(observationDate)) {

            return Optional.empty();
        }


        /*
         * valuation 계산에 사용한 재무정보가
         * observationDate 이후에 공시된 것이면
         * look-ahead bias.
         */
        if (valuation.filedDate() != null
                && valuation.filedDate()
                .isAfter(observationDate)) {

            return Optional.empty();
        }


        return Optional.of(
                new ValuationPeerSnapshotInput(

                        stockId,
                        symbol,

                        observationDate,

                        valuation,

                        growth,
                        profitability
                )
        );
    }
}