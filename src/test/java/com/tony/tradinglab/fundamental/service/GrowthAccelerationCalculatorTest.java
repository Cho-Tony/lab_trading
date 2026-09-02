package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.GrowthAcceleration;
import com.tony.tradinglab.fundamental.domain.RevenueGrowth;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrowthAccelerationCalculatorTest {

    private final GrowthAccelerationCalculator calculator =
            new GrowthAccelerationCalculator();

    @Test
    void calculateGrowthAcceleration() {

        List<RevenueGrowth> growths = List.of(

                createGrowth(
                        2025,
                        "Q1",
                        "5.00"
                ),

                createGrowth(
                        2025,
                        "Q2",
                        "14.00"
                ),

                createGrowth(
                        2025,
                        "Q3",
                        "27.00"
                )
        );

        List<GrowthAcceleration> result =
                calculator.calculate(growths);

        assertThat(result).hasSize(2);

        assertThat(
                result.get(0).accelerationPctPoint()
        ).isEqualByComparingTo("9.00");

        assertThat(
                result.get(1).accelerationPctPoint()
        ).isEqualByComparingTo("13.00");

        result.forEach(growth ->
                System.out.println(
                        "FY" + growth.fiscalYear()
                                + " " + growth.fiscalQuarter()
                                + " | YoY="
                                + growth.yoyGrowthPct() + "%"
                                + " | Previous YoY="
                                + growth.previousQuarterYoyGrowthPct() + "%"
                                + " | Acceleration="
                                + growth.accelerationPctPoint() + "%p"
                                + " | filed="
                                + growth.filedDate()
                )
        );
    }

    private RevenueGrowth createGrowth(
            int fiscalYear,
            String quarter,
            String yoyGrowth
    ) {

        return new RevenueGrowth(
                fiscalYear,
                quarter,

                BigDecimal.ZERO,
                BigDecimal.ZERO,

                new BigDecimal(yoyGrowth),

                LocalDate.of(2025, 1, 1)
        );
    }
}