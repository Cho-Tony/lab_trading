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
         * 테스트 편의를 위해
         * 253개 거래일 데이터를 만든다.
         *
         * 가격:
         * Day 0   = 100
         * Day 63  = 163
         * Day 126 = 226
         * Day 252 = 352
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


        /*
         * (163 - 100) / 100
         * = 63%
         */
        assertThat(
                result.return63dPct()
        ).isEqualByComparingTo(
                "63.00"
        );


        /*
         * 126%
         */
        assertThat(
                result.return126dPct()
        ).isEqualByComparingTo(
                "126.00"
        );


        /*
         * 252%
         */
        assertThat(
                result.return252dPct()
        ).isEqualByComparingTo(
                "252.00"
        );
    }


    private StockPrice price(
            LocalDate tradeDate,
            BigDecimal adjustedClose
    ) {

        StockPrice price =
                new StockPrice();

        price.setTradeDate(
                tradeDate
        );

        price.setClose(
                adjustedClose
        );

        price.setAdjustedClose(
                adjustedClose
        );

        return price;
    }
}