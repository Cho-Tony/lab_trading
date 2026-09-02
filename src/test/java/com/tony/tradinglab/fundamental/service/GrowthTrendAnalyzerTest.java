package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.GrowthAcceleration;
import com.tony.tradinglab.fundamental.domain.GrowthTrend;
import com.tony.tradinglab.fundamental.domain.GrowthTrendAnalysis;
import com.tony.tradinglab.fundamental.domain.RevenueGrowth;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrowthTrendAnalyzerTest {

    @Test
    void analyzeAcceleratingGrowth() {

        List<RevenueGrowth> growths = List.of(
                createGrowth(2025, "Q1", "5.00"),
                createGrowth(2025, "Q2", "14.00"),
                createGrowth(2025, "Q3", "27.00")
        );

        GrowthAccelerationCalculator accelerationCalculator =
                new GrowthAccelerationCalculator();

        GrowthTrendAnalyzer trendAnalyzer =
                new GrowthTrendAnalyzer();

        List<GrowthAcceleration> accelerations =
                accelerationCalculator.calculate(growths);

        GrowthTrendAnalysis analysis =
                trendAnalyzer.analyze(accelerations);

        System.out.println("Trend = " + analysis.trend());
        System.out.println(
                "Latest YoY = "
                        + analysis.latestYoyGrowthPct()
                        + "%"
        );
        System.out.println(
                "Average Acceleration = "
                        + analysis.averageAccelerationPctPoint()
                        + "%p"
        );

        assertThat(analysis.trend())
                .isEqualTo(GrowthTrend.ACCELERATING);

        assertThat(
                analysis.averageAccelerationPctPoint()
        ).isEqualByComparingTo("11.00");
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