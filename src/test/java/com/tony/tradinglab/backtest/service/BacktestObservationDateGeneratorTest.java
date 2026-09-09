package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.BacktestObservationFrequency;
import com.tony.tradinglab.price.domain.StockPrice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BacktestObservationDateGeneratorTest {

    private final BacktestObservationDateGenerator generator =
            new BacktestObservationDateGenerator();


    @Test
    void generateQuarterlyObservationDatesUsingLastTradingDay() {

        List<StockPrice> benchmarkPrices =
                List.of(

                        price(
                                LocalDate.of(
                                        2024,
                                        3,
                                        28
                                )
                        ),

                        price(
                                LocalDate.of(
                                        2024,
                                        3,
                                        29
                                )
                        ),

                        /*
                         * 3/31은 일요일이므로 데이터 없음
                         */


                        price(
                                LocalDate.of(
                                        2024,
                                        6,
                                        27
                                )
                        ),

                        price(
                                LocalDate.of(
                                        2024,
                                        6,
                                        28
                                )
                        ),

                        /*
                         * 6/30도 일요일
                         */


                        price(
                                LocalDate.of(
                                        2024,
                                        9,
                                        30
                                )
                        ),

                        price(
                                LocalDate.of(
                                        2024,
                                        12,
                                        31
                                )
                        )
                );


        List<LocalDate> result =
                generator.generate(

                        LocalDate.of(
                                2024,
                                1,
                                1
                        ),

                        LocalDate.of(
                                2024,
                                12,
                                31
                        ),

                        BacktestObservationFrequency.QUARTERLY,

                        benchmarkPrices
                );


        assertThat(
                result
        )
                .containsExactly(

                        LocalDate.of(
                                2024,
                                3,
                                29
                        ),

                        LocalDate.of(
                                2024,
                                6,
                                28
                        ),

                        LocalDate.of(
                                2024,
                                9,
                                30
                        ),

                        LocalDate.of(
                                2024,
                                12,
                                31
                        )
                );
    }


    @Test
    void generateMonthlyObservationDates() {

        List<StockPrice> benchmarkPrices =
                List.of(

                        price(
                                LocalDate.of(
                                        2025,
                                        1,
                                        31
                                )
                        ),

                        price(
                                LocalDate.of(
                                        2025,
                                        2,
                                        28
                                )
                        ),

                        price(
                                LocalDate.of(
                                        2025,
                                        3,
                                        31
                                )
                        )
                );


        List<LocalDate> result =
                generator.generate(

                        LocalDate.of(
                                2025,
                                1,
                                1
                        ),

                        LocalDate.of(
                                2025,
                                3,
                                31
                        ),

                        BacktestObservationFrequency.MONTHLY,

                        benchmarkPrices
                );


        assertThat(
                result
        )
                .containsExactly(

                        LocalDate.of(
                                2025,
                                1,
                                31
                        ),

                        LocalDate.of(
                                2025,
                                2,
                                28
                        ),

                        LocalDate.of(
                                2025,
                                3,
                                31
                        )
                );
    }


    private StockPrice price(
            LocalDate date
    ) {

        BigDecimal value =
                new BigDecimal("100");


        return new StockPrice(

                null,

                date,

                value,
                value,
                value,
                value,

                value,

                1_000_000L
        );
    }
}