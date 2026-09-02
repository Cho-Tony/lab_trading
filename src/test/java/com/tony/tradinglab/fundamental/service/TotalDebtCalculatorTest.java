package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyDebt;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TotalDebtCalculatorTest {

    private final TotalDebtCalculator calculator =
            new TotalDebtCalculator();

    @Test
    void calculateTotalDebt() {

        List<QuarterlyFact> shortTermDebts =
                List.of(
                        createFact(
                                2025,
                                "Q4",
                                "7979000000"
                        )
                );

        List<QuarterlyFact> currentLongTermDebts =
                List.of(
                        createFact(
                                2025,
                                "Q4",
                                "12350000000"
                        )
                );

        List<QuarterlyFact> longTermDebts =
                List.of(
                        createFact(
                                2025,
                                "Q4",
                                "78328000000"
                        )
                );

        List<QuarterlyDebt> result =
                calculator.calculate(
                        shortTermDebts,
                        currentLongTermDebts,
                        longTermDebts
                );

        QuarterlyDebt debt =
                result.get(0);

        System.out.println(
                "FY"
                        + debt.fiscalYear()
                        + " "
                        + debt.fiscalQuarter()
                        + " | ShortTerm="
                        + debt.shortTermDebt()
                        + " | CurrentLT="
                        + debt.currentLongTermDebt()
                        + " | LongTerm="
                        + debt.longTermDebt()
                        + " | TotalDebt="
                        + debt.totalDebt()
        );

        assertThat(
                debt.totalDebt()
        ).isEqualByComparingTo(
                "98657000000"
        );
    }

    private QuarterlyFact createFact(
            int fiscalYear,
            String fiscalQuarter,
            String value
    ) {

        return new QuarterlyFact(
                "TEST",
                new BigDecimal(value),

                null,
                LocalDate.of(
                        2025,
                        9,
                        27
                ),

                LocalDate.of(
                        2025,
                        10,
                        31
                ),

                fiscalYear,
                fiscalQuarter,

                false
        );
    }
}