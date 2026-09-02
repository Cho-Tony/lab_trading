package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FreeCashFlowDirection;
import com.tony.tradinglab.fundamental.domain.FreeCashFlowMetrics;
import com.tony.tradinglab.fundamental.domain.FreeCashFlowTrendAnalysis;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FreeCashFlowTrendAnalyzerTest {

    private final FreeCashFlowTrendAnalyzer analyzer =
            new FreeCashFlowTrendAnalyzer();

    @Test
    void analyzeImprovingFreeCashFlow() {

        List<FreeCashFlowMetrics> metrics =
                List.of(

                        createMetrics(
                                2025,
                                "Q2",
                                "15000000000",
                                "15.00",
                                "18.00",
                                false
                        ),

                        createMetrics(
                                2025,
                                "Q3",
                                "19000000000",
                                "18.00",
                                "25.00",
                                false
                        ),

                        createMetrics(
                                2025,
                                "Q4",
                                "25000000000",
                                "21.00",
                                "31.00",
                                false
                        )
                );

        FreeCashFlowTrendAnalysis result =
                analyzer.analyze(metrics);

        System.out.println(
                "FCF Direction = "
                        + result.direction()
        );

        System.out.println(
                "Latest FCF = "
                        + result.latestFreeCashFlow()
        );

        System.out.println(
                "Latest FCF Margin = "
                        + result.latestFreeCashFlowMarginPct()
                        + "%"
        );

        System.out.println(
                "Latest FCF YoY = "
                        + result.latestGrowthYoYPct()
                        + "%"
        );


        assertThat(
                result.direction()
        ).isEqualTo(
                FreeCashFlowDirection.IMPROVING
        );

        assertThat(
                result.positiveGrowthCount()
        ).isEqualTo(3);

        assertThat(
                result.negativeGrowthCount()
        ).isZero();
    }

    private FreeCashFlowMetrics createMetrics(
            int fiscalYear,
            String fiscalQuarter,
            String freeCashFlow,
            String margin,
            String yoyGrowth,
            boolean turnaround
    ) {

        return new FreeCashFlowMetrics(

                fiscalYear,
                fiscalQuarter,

                new BigDecimal("100000000000"),

                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal(freeCashFlow),

                new BigDecimal(margin),

                BigDecimal.ZERO,
                new BigDecimal(yoyGrowth),

                turnaround,

                LocalDate.of(
                        2025,
                        10,
                        31
                )
        );
    }
}