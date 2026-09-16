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
         * 가격 날짜가 observationDate보다 미래면
         * look-ahead bias.
         *
         * 주말 / 휴일에는 직전 거래일 가격을
         * 사용하는 것이 정상적이므로
         * observationDate와 반드시 같을 필요는 없다.
         */
        if (valuation.priceDate() == null
                || valuation.priceDate()
                .isAfter(observationDate)) {

            return Optional.empty();
        }


        /*
         * 재무정보 역시 observationDate 이후
         * 공개된 정보를 사용하면 안 된다.
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