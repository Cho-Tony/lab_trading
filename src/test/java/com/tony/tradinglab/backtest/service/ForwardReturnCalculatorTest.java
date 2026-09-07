package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.ForwardReturnMetrics;
import com.tony.tradinglab.price.domain.StockPrice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForwardReturnCalculatorTest {

    private final ForwardReturnCalculator calculator =
            new ForwardReturnCalculator();


    @Test
    void calculateForwardReturnsByTradingDays() {

        LocalDate startDate =
                LocalDate.of(
                        2025,
                        1,
                        1
                );


        List<StockPrice> prices =
                new ArrayList<>();


        /*
         * index 0 ~ 252
         * 총 253개 가격 데이터
         */
        for (int i = 0; i <= 252; i++) {

            prices.add(
                    price(
                            startDate.plusDays(i),

                            new BigDecimal(
                                    100 + i
                            )
                    )
            );
        }


        ForwardReturnMetrics result =
                calculator.calculate(
                                startDate,
                                prices
                        )
                        .orElseThrow();


        assertThat(
                result.observationPrice()
        ).isEqualByComparingTo(
                "100"
        );


        assertThat(
                result.return63dPct()
        ).isEqualByComparingTo(
                "63.00"
        );


        assertThat(
                result.return126dPct()
        ).isEqualByComparingTo(
                "126.00"
        );


        assertThat(
                result.return252dPct()
        ).isEqualByComparingTo(
                "252.00"
        );
    }


    private StockPrice price(
            LocalDate tradeDate,
            BigDecimal price
    ) {

        return new StockPrice(

                /*
                 * 이 테스트에서는 Stock 객체를 사용하지 않음.
                 * DB에 persist하는 테스트도 아니므로 null 가능.
                 */
                null,

                tradeDate,

                price,   // open
                price,   // high
                price,   // low
                price,   // close

                price,   // adjustedClose

                1_000_000L
        );
    }
}