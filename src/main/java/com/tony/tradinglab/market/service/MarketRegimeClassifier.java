package com.tony.tradinglab.market.service;

import com.tony.tradinglab.market.domain.MarketRegime;
import com.tony.tradinglab.market.domain.MarketRegimeSnapshot;
import com.tony.tradinglab.price.domain.StockPrice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class MarketRegimeClassifier {

    private static final int MOVING_AVERAGE_DAYS = 200;

    private static final int MOMENTUM_DAYS = 63;

    private static final int SCALE = 2;


    public Optional<MarketRegimeSnapshot> classify(
            String benchmarkSymbol,
            LocalDate asOfDate,
            List<StockPrice> benchmarkPrices
    ) {

        if (asOfDate == null
                || benchmarkPrices == null) {

            return Optional.empty();
        }


        List<StockPrice> availablePrices =
                benchmarkPrices.stream()

                        /*
                         * 핵심 PIT 조건.
                         *
                         * 미래 가격은 사용하지 않는다.
                         */
                        .filter(
                                price ->
                                        price.getTradeDate() != null
                                                && !price.getTradeDate()
                                                .isAfter(asOfDate)
                        )

                        .filter(
                                price ->
                                        effectivePrice(price) != null
                                                && effectivePrice(price)
                                                .compareTo(
                                                        BigDecimal.ZERO
                                                ) > 0
                        )

                        .sorted(
                                Comparator.comparing(
                                        StockPrice::getTradeDate
                                )
                        )

                        .toList();


        /*
         * 200D MA 계산에 필요한 데이터 부족
         */
        if (availablePrices.size()
                < MOVING_AVERAGE_DAYS) {

            return Optional.empty();
        }


        int lastIndex =
                availablePrices.size() - 1;


        StockPrice current =
                availablePrices.get(
                        lastIndex
                );


        BigDecimal currentPrice =
                effectivePrice(
                        current
                );


        BigDecimal movingAverage200 =
                calculateMovingAverage(
                        availablePrices
                );


        /*
         * 63 거래일 전
         */
        int momentumBaseIndex =
                lastIndex - MOMENTUM_DAYS;


        if (momentumBaseIndex < 0) {

            return Optional.empty();
        }


        BigDecimal momentumBasePrice =
                effectivePrice(
                        availablePrices.get(
                                momentumBaseIndex
                        )
                );


        BigDecimal return63dPct =
                calculateReturnPct(
                        momentumBasePrice,
                        currentPrice
                );


        MarketRegime regime =
                determineRegime(

                        currentPrice,
                        movingAverage200,
                        return63dPct
                );


        return Optional.of(
                new MarketRegimeSnapshot(

                        benchmarkSymbol,

                        asOfDate,

                        current.getTradeDate(),

                        currentPrice,

                        movingAverage200,

                        return63dPct,

                        regime
                )
        );
    }


    private MarketRegime determineRegime(
            BigDecimal currentPrice,
            BigDecimal movingAverage200,
            BigDecimal return63dPct
    ) {

        boolean aboveMovingAverage =
                currentPrice.compareTo(
                        movingAverage200
                ) > 0;


        boolean belowMovingAverage =
                currentPrice.compareTo(
                        movingAverage200
                ) < 0;


        boolean positiveMomentum =
                return63dPct.compareTo(
                        BigDecimal.ZERO
                ) > 0;


        boolean negativeMomentum =
                return63dPct.compareTo(
                        BigDecimal.ZERO
                ) < 0;


        if (aboveMovingAverage
                && positiveMomentum) {

            return MarketRegime.BULL;
        }


        if (belowMovingAverage
                && negativeMomentum) {

            return MarketRegime.BEAR;
        }


        return MarketRegime.NORMAL;
    }


    private BigDecimal calculateMovingAverage(
            List<StockPrice> prices
    ) {

        int startIndex =
                prices.size()
                        - MOVING_AVERAGE_DAYS;


        BigDecimal sum =
                BigDecimal.ZERO;


        for (int i = startIndex;
             i < prices.size();
             i++) {

            sum =
                    sum.add(
                            effectivePrice(
                                    prices.get(i)
                            )
                    );
        }


        return sum.divide(

                BigDecimal.valueOf(
                        MOVING_AVERAGE_DAYS
                ),

                SCALE,

                RoundingMode.HALF_UP
        );
    }


    private BigDecimal calculateReturnPct(
            BigDecimal startPrice,
            BigDecimal endPrice
    ) {

        return endPrice
                .subtract(
                        startPrice
                )

                .divide(
                        startPrice,
                        8,
                        RoundingMode.HALF_UP
                )

                .multiply(
                        new BigDecimal("100")
                )

                .setScale(
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }


    private BigDecimal effectivePrice(
            StockPrice price
    ) {

        if (price.getAdjustedClose() != null) {

            return price.getAdjustedClose();
        }

        return price.getClose();
    }
}