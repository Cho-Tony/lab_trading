package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PointInTimeFundamentalContext;
import com.tony.tradinglab.fundamental.domain.Profitability;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrend;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrendAnalysis;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PointInTimeProfitabilityAnalyzer {

    private final ProfitabilityCalculator profitabilityCalculator;
    private final ProfitabilityTrendCalculator profitabilityTrendCalculator;
    private final ProfitabilityTrendAnalyzer profitabilityTrendAnalyzer;


    public PointInTimeProfitabilityAnalyzer(
            ProfitabilityCalculator profitabilityCalculator,
            ProfitabilityTrendCalculator profitabilityTrendCalculator,
            ProfitabilityTrendAnalyzer profitabilityTrendAnalyzer
    ) {

        this.profitabilityCalculator =
                profitabilityCalculator;

        this.profitabilityTrendCalculator =
                profitabilityTrendCalculator;

        this.profitabilityTrendAnalyzer =
                profitabilityTrendAnalyzer;
    }


    public Optional<ProfitabilityTrendAnalysis> analyze(
            PointInTimeFundamentalContext context
    ) {

        if (context == null
                || context.quarterlyFinancials() == null
                || context.quarterlyFinancials().isEmpty()) {

            return Optional.empty();
        }


        /*
         * context 안에는 이미
         * asOfDate까지 실제 공개된 재무정보만 존재한다.
         */
        List<Profitability> profitabilities =
                profitabilityCalculator.calculate(
                        context.quarterlyFinancials()
                );


        if (profitabilities == null
                || profitabilities.isEmpty()) {

            return Optional.empty();
        }


        List<ProfitabilityTrend> trends =
                profitabilityTrendCalculator.calculate(
                        profitabilities
                );


        if (trends == null
                || trends.isEmpty()) {

            return Optional.empty();
        }


        ProfitabilityTrendAnalysis analysis =
                profitabilityTrendAnalyzer.analyze(
                        trends
                );


        return Optional.ofNullable(
                analysis
        );
    }
}