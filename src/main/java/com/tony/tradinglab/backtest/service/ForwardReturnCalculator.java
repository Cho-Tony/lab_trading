package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.ForwardReturnMetrics;
import com.tony.tradinglab.price.domain.StockPrice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class ForwardReturnCalculator {

    private static final int RETURN_SCALE = 2;

    /*
     * v1 백테스트 horizon
     */
    private static final int RETURN_63D = 63;
    private static final int RETURN_126D = 126;
    private static final int RETURN_252D = 252;


    public Optional<ForwardReturnMetrics> calculate(
            LocalDate observationDate,
            List<StockPrice> prices
    ) {

        if (observationDate == null
                || prices == null
                || prices.isEmpty()) {

            return Optional.empty();
        }


        List<StockPrice> sorted =
                prices.stream()

                        .filter(
                                price ->
                                        price.getTradeDate() != null
                                                && !price.getTradeDate()
                                                .isBefore(observationDate)
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


        if (sorted.isEmpty()) {
            return Optional.empty();
        }


        /*
         * observationDate가 휴장일인 경우에도
         * 그 이후 첫 거래일을 기준점으로 사용.
         */
        StockPrice start =
                sorted.get(0);


        BigDecimal startPrice =
                effectivePrice(start);


        ReturnPoint return63 =
                calculateReturnPoint(
                        sorted,
                        startPrice,
                        RETURN_63D
                );


        ReturnPoint return126 =
                calculateReturnPoint(
                        sorted,
                        startPrice,
                        RETURN_126D
                );


        ReturnPoint return252 =
                calculateReturnPoint(
                        sorted,
                        startPrice,
                        RETURN_252D
                );


        return Optional.of(
                new ForwardReturnMetrics(

                        start.getTradeDate(),

                        startPrice,

                        return63.date(),
                        return63.returnPct(),

                        return126.date(),
                        return126.returnPct(),

                        return252.date(),
                        return252.returnPct()
                )
        );
    }


    private ReturnPoint calculateReturnPoint(
            List<StockPrice> prices,
            BigDecimal startPrice,
            int tradingDays
    ) {

        /*
         * index 0이 시작일이므로
         * 63 거래일 "후"는 index 63.
         */
        if (prices.size() <= tradingDays) {

            return new ReturnPoint(
                    null,
                    null
            );
        }


        StockPrice future =
                prices.get(
                        tradingDays
                );


        BigDecimal futurePrice =
                effectivePrice(
                        future
                );


        BigDecimal returnPct =
                futurePrice
                        .subtract(startPrice)

                        .divide(
                                startPrice,
                                8,
                                RoundingMode.HALF_UP
                        )

                        .multiply(
                                new BigDecimal("100")
                        )

                        .setScale(
                                RETURN_SCALE,
                                RoundingMode.HALF_UP
                        );


        return new ReturnPoint(
                future.getTradeDate(),
                returnPct
        );
    }


    private BigDecimal effectivePrice(
            StockPrice price
    ) {

        /*
         * 백테스트 수익률에서는
         * adjusted close를 우선 사용.
         */
        if (price.getAdjustedClose() != null) {

            return price.getAdjustedClose();
        }

        return price.getClose();
    }


    private record ReturnPoint(

            LocalDate date,

            BigDecimal returnPct

    ) {
    }
}