package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.price.service.MarketPriceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuantBacktestSampleFactory {

    private static final BigDecimal HUNDRED =
            BigDecimal.valueOf(100);


    private final MarketPriceProvider marketPriceProvider;


    public Optional<QuantBacktestSample> create(
            QuantDiscoveryCandidate candidate,
            String exchange
    ) {

        if (candidate == null
                || candidate.symbol() == null
                || candidate.symbol().isBlank()
                || candidate.observationDate() == null
                || candidate.score() == null
                || exchange == null
                || exchange.isBlank()) {

            return Optional.empty();
        }


        String symbol =
                candidate.symbol()
                        .trim()
                        .toUpperCase();


        String normalizedExchange =
                exchange.trim()
                        .toUpperCase();


        /*
         * Entry Price
         *
         * 기준일 당일이 휴장일일 수도 있으므로
         * observationDate 이하의 가장 최근 가격을 사용한다.
         */
        var entryPriceOptional =
                marketPriceProvider
                        .getAsOf(
                                symbol,
                                normalizedExchange,
                                candidate.observationDate()
                        );


        if (entryPriceOptional.isEmpty()) {
            return Optional.empty();
        }


        var entryPriceEntity =
                entryPriceOptional.get();


        BigDecimal entryPrice =
                entryPriceEntity.getClose();


        if (entryPrice == null
                || entryPrice.signum() <= 0) {

            return Optional.empty();
        }


        /*
         * Future horizons
         */
        HorizonResult threeMonth =
                resolveHorizon(
                        symbol,
                        normalizedExchange,
                        candidate.observationDate()
                                .plusMonths(3),
                        entryPrice
                );


        HorizonResult sixMonth =
                resolveHorizon(
                        symbol,
                        normalizedExchange,
                        candidate.observationDate()
                                .plusMonths(6),
                        entryPrice
                );


        HorizonResult twelveMonth =
                resolveHorizon(
                        symbol,
                        normalizedExchange,
                        candidate.observationDate()
                                .plusMonths(12),
                        entryPrice
                );


        /*
         * 미래 데이터가 아직 없는 horizon은
         * null로 남긴다.
         *
         * 예:
         * 3M 데이터 있음
         * 6M 데이터 있음
         * 12M 데이터 없음
         *
         * 이런 경우도 sample 자체는 보존한다.
         */
        return Optional.of(
                new QuantBacktestSample(

                        candidate.stockId(),

                        candidate.symbol(),

                        candidate.observationDate(),

                        entryPriceEntity.getTradeDate(),

                        entryPrice,

                        candidate.score(),

                        threeMonth.priceDate(),
                        threeMonth.price(),
                        threeMonth.returnPct(),

                        sixMonth.priceDate(),
                        sixMonth.price(),
                        sixMonth.returnPct(),

                        twelveMonth.priceDate(),
                        twelveMonth.price(),
                        twelveMonth.returnPct()
                )
        );
    }


    private HorizonResult resolveHorizon(
            String symbol,
            String exchange,
            LocalDate targetDate,
            BigDecimal entryPrice
    ) {

        var priceOptional =
                marketPriceProvider
                        .getAsOf(
                                symbol,
                                exchange,
                                targetDate
                        );


        if (priceOptional.isEmpty()) {

            return HorizonResult.empty();
        }


        var priceEntity =
                priceOptional.get();


        BigDecimal price =
                priceEntity.getClose();


        if (price == null
                || price.signum() <= 0) {

            return HorizonResult.empty();
        }


        BigDecimal returnPct =
                calculateReturnPct(
                        entryPrice,
                        price
                );


        return new HorizonResult(
                priceEntity.getTradeDate(),
                price,
                returnPct
        );
    }


    private BigDecimal calculateReturnPct(
            BigDecimal entryPrice,
            BigDecimal exitPrice
    ) {

        if (entryPrice == null
                || exitPrice == null
                || entryPrice.signum() <= 0) {

            return null;
        }


        return exitPrice
                .subtract(
                        entryPrice
                )
                .divide(
                        entryPrice,
                        8,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        HUNDRED
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    private record HorizonResult(
            LocalDate priceDate,
            BigDecimal price,
            BigDecimal returnPct
    ) {

        private static HorizonResult empty() {

            return new HorizonResult(
                    null,
                    null,
                    null
            );
        }
    }
}