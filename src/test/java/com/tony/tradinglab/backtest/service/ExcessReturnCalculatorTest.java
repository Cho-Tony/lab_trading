package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.ExcessReturnMetrics;
import com.tony.tradinglab.backtest.domain.ForwardReturnMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ExcessReturnCalculatorTest {

    private final ExcessReturnCalculator calculator =
            new ExcessReturnCalculator();


    @Test
    void calculateExcessReturnsAgainstBenchmark() {

        ForwardReturnMetrics stockReturns =
                new ForwardReturnMetrics(

                        LocalDate.of(
                                2025,
                                1,
                                2
                        ),

                        new BigDecimal("100"),

                        LocalDate.of(
                                2025,
                                4,
                                2
                        ),

                        new BigDecimal("20.00"),

                        LocalDate.of(
                                2025,
                                7,
                                2
                        ),

                        new BigDecimal("35.00"),

                        LocalDate.of(
                                2026,
                                1,
                                2
                        ),

                        new BigDecimal("50.00")
                );


        ForwardReturnMetrics benchmarkReturns =
                new ForwardReturnMetrics(

                        LocalDate.of(
                                2025,
                                1,
                                2
                        ),

                        new BigDecimal("500"),

                        LocalDate.of(
                                2025,
                                4,
                                2
                        ),

                        new BigDecimal("12.00"),

                        LocalDate.of(
                                2025,
                                7,
                                2
                        ),

                        new BigDecimal("25.00"),

                        LocalDate.of(
                                2026,
                                1,
                                2
                        ),

                        new BigDecimal("30.00")
                );


        ExcessReturnMetrics result =
                calculator.calculate(
                        "QQQ",
                        stockReturns,
                        benchmarkReturns
                );


        System.out.println(
                "63D Excess = "
                        + result.excessReturn63dPct()
        );

        System.out.println(
                "126D Excess = "
                        + result.excessReturn126dPct()
        );

        System.out.println(
                "252D Excess = "
                        + result.excessReturn252dPct()
        );


        assertThat(
                result.excessReturn63dPct()
        ).isEqualByComparingTo(
                "8.00"
        );


        assertThat(
                result.excessReturn126dPct()
        ).isEqualByComparingTo(
                "10.00"
        );


        assertThat(
                result.excessReturn252dPct()
        ).isEqualByComparingTo(
                "20.00"
        );
    }


    @Test
    void returnNullWhenFutureBenchmarkDataIsUnavailable() {

        ForwardReturnMetrics stockReturns =
                new ForwardReturnMetrics(

                        LocalDate.of(
                                2025,
                                1,
                                2
                        ),

                        new BigDecimal("100"),

                        LocalDate.of(
                                2025,
                                4,
                                2
                        ),

                        new BigDecimal("20.00"),

                        null,
                        null,

                        null,
                        null
                );


        ForwardReturnMetrics benchmarkReturns =
                new ForwardReturnMetrics(

                        LocalDate.of(
                                2025,
                                1,
                                2
                        ),

                        new BigDecimal("500"),

                        LocalDate.of(
                                2025,
                                4,
                                2
                        ),

                        new BigDecimal("10.00"),

                        null,
                        null,

                        null,
                        null
                );


        ExcessReturnMetrics result =
                calculator.calculate(
                        "QQQ",
                        stockReturns,
                        benchmarkReturns
                );


        assertThat(
                result.excessReturn63dPct()
        ).isEqualByComparingTo(
                "10.00"
        );


        assertThat(
                result.excessReturn126dPct()
        ).isNull();


        assertThat(
                result.excessReturn252dPct()
        ).isNull();
    }
}