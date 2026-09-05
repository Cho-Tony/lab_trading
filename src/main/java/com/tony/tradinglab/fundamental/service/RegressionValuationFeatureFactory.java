package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class RegressionValuationFeatureFactory {

    public Optional<RegressionValuationFeature> create(
            ValuationPeerSnapshot snapshot,
            RegressionValuationMetric metric
    ) {

        if (snapshot == null
                || snapshot.valuation() == null
                || snapshot.growth() == null
                || snapshot.profitability() == null) {

            return Optional.empty();
        }


        BigDecimal valuationMultiple =
                extractValuationMultiple(
                        snapshot,
                        metric
                );


        BigDecimal revenueGrowthPct =
                snapshot.growth()
                        .latestYoyGrowthPct();


        BigDecimal operatingMarginPct =
                snapshot.profitability()
                        .latestOperatingMarginPct();


        /*
         * 회귀에 필요한 핵심 feature가 없으면
         * 해당 회사는 해당 모델의 학습 row에서 제외.
         */
        if (valuationMultiple == null
                || valuationMultiple.compareTo(
                BigDecimal.ZERO
        ) <= 0
                || revenueGrowthPct == null
                || operatingMarginPct == null) {

            return Optional.empty();
        }


        return Optional.of(
                new RegressionValuationFeature(

                        snapshot.stockId(),
                        snapshot.symbol(),

                        snapshot.valuation()
                                .priceDate(),

                        metric,

                        valuationMultiple,

                        revenueGrowthPct,
                        operatingMarginPct
                )
        );
    }


    private BigDecimal extractValuationMultiple(
            ValuationPeerSnapshot snapshot,
            RegressionValuationMetric metric
    ) {

        return switch (metric) {

            case PS ->
                    snapshot.valuation()
                            .psRatio();

            case PE ->
                    snapshot.valuation()
                            .peRatio();

            case PRICE_TO_FCF ->
                    snapshot.valuation()
                            .priceToFcfRatio();
        };
    }
}