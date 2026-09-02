package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.Profitability;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfitabilityCalculatorTest {

    private final ProfitabilityCalculator calculator =
            new ProfitabilityCalculator();

    @Test
    void calculateProfitability() {

        QuarterlyFinancials financial =
                new QuarterlyFinancials(
                        2025,
                        "Q1",

                        new BigDecimal("124300000000"),
                        new BigDecimal("42832000000"),
                        new BigDecimal("36330000000"),

                        LocalDate.of(2025, 1, 31)
                );

        List<Profitability> result =
                calculator.calculate(
                        List.of(financial)
                );

        Profitability profitability =
                result.get(0);

        System.out.println(
                "Operating Margin = "
                        + profitability.operatingMarginPct()
                        + "%"
        );

        System.out.println(
                "Net Margin = "
                        + profitability.netMarginPct()
                        + "%"
        );

        assertThat(
                profitability.operatingMarginPct()
        ).isEqualByComparingTo("34.46");

        assertThat(
                profitability.netMarginPct()
        ).isEqualByComparingTo("29.23");
    }
}