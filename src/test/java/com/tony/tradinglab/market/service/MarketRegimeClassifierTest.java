package com.tony.tradinglab.market.service;

import com.tony.tradinglab.market.domain.MarketRegime;
import com.tony.tradinglab.market.domain.MarketRegimeSnapshot;
import com.tony.tradinglab.price.domain.StockPrice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarketRegimeClassifierTest {

    private final MarketRegimeClassifier classifier =
            new MarketRegimeClassifier();


    @Test
    void classifyBullMarket() {

        LocalDate start =
                LocalDate.of(
                        2024,
                        1,
                        1
                );


        List<StockPrice> prices =
                new ArrayList<>();


        /*
         * 계속 상승하는 가격
         *
         * 100 → 299
         */
        for (int i = 0;
             i < 200;
             i++) {

            prices.add(
                    price(

                            start.plusDays(i),

                            new BigDecimal(
                                    100 + i
                            )
                    )
            );
        }


        MarketRegimeSnapshot result =
                classifier.classify(

                                "QQQ",

                                start.plusDays(199),

                                prices
                        )
                        .orElseThrow();


        System.out.println(
                "Price = "
                        + result.benchmarkPrice()
        );

        System.out.println(
                "MA200 = "
                        + result.movingAverage200()
        );

        System.out.println(
                "63D Return = "
                        + result.return63dPct()
        );

        System.out.println(
                "Regime = "
                        + result.regime()
        );


        assertThat(
                result.regime()
        ).isEqualTo(
                MarketRegime.BULL
        );


        assertThat(
                result.benchmarkPrice()
        ).isGreaterThan(
                result.movingAverage200()
        );


        assertThat(
                result.return63dPct()
        ).isPositive();
    }


    @Test
    void classifyBearMarket() {

        LocalDate start =
                LocalDate.of(
                        2024,
                        1,
                        1
                );


        List<StockPrice> prices =
                new ArrayList<>();


        /*
         * 계속 하락
         *
         * 300 → 101
         */
        for (int i = 0;
             i < 200;
             i++) {

            prices.add(
                    price(

                            start.plusDays(i),

                            new BigDecimal(
                                    300 - i
                            )
                    )
            );
        }


        MarketRegimeSnapshot result =
                classifier.classify(

                                "QQQ",

                                start.plusDays(199),

                                prices
                        )
                        .orElseThrow();


        assertThat(
                result.regime()
        ).isEqualTo(
                MarketRegime.BEAR
        );


        assertThat(
                result.benchmarkPrice()
        ).isLessThan(
                result.movingAverage200()
        );


        assertThat(
                result.return63dPct()
        ).isNegative();
    }


    private StockPrice price(
            LocalDate date,
            BigDecimal price
    ) {

        return new StockPrice(

                null,

                date,

                price,
                price,
                price,
                price,

                price,

                1_000_000L
        );
    }
}