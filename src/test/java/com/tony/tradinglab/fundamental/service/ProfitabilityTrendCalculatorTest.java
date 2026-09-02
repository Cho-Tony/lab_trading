package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.Profitability;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrend;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfitabilityTrendCalculatorTest {

    private final ProfitabilityTrendCalculator calculator =
            new ProfitabilityTrendCalculator();

    @Test
    void calculateProfitabilityTrend() {

        List<Profitability> profitabilities =
                List.of(
                        createProfitability(
                                2025,
                                "Q1",
                                "10.00",
                                "6.00"
                        ),
                        createProfitability(
                                2025,
                                "Q2",
                                "14.00",
                                "9.00"
                        ),
                        createProfitability(
                                2025,
                                "Q3",
                                "19.00",
                                "13.00"
                        )
                );

        List<ProfitabilityTrend> result =
                calculator.calculate(
                        profitabilities
                );

        result.forEach(trend -> {

            System.out.println(
                    "FY"
                            + trend.fiscalYear()
                            + " "
                            + trend.fiscalQuarter()

                            + " | OperatingMargin="
                            + trend.operatingMarginPct()
                            + "%"

                            + " | Previous="
                            + trend.previousQuarterOperatingMarginPct()
                            + "%"

                            + " | Change="
                            + trend.operatingMarginChangePctPoint()
                            + "%p"

                            + " | NetMargin="
                            + trend.netMarginPct()
                            + "%"

                            + " | NetChange="
                            + trend.netMarginChangePctPoint()
                            + "%p"
            );
        });

        assertThat(result)
                .hasSize(2);

        assertThat(
                result.get(0)
                        .operatingMarginChangePctPoint()
        ).isEqualByComparingTo("4.00");

        assertThat(
                result.get(1)
                        .operatingMarginChangePctPoint()
        ).isEqualByComparingTo("5.00");

        assertThat(
                result.get(1)
                        .netMarginChangePctPoint()
        ).isEqualByComparingTo("4.00");
    }

    private Profitability createProfitability(
            int fiscalYear,
            String fiscalQuarter,
            String operatingMargin,
            String netMargin
    ) {

        return new Profitability(
                fiscalYear,
                fiscalQuarter,

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,

                new BigDecimal(
                        operatingMargin
                ),

                new BigDecimal(
                        netMargin
                ),

                LocalDate.of(
                        2025,
                        1,
                        1
                )
        );
    }
}