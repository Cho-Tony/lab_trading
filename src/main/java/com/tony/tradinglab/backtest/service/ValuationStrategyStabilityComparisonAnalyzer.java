package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValuationStrategyStabilityComparisonAnalyzer {

    private final ValuationBacktestStabilityAnalyzer stabilityAnalyzer;


    public ValuationStrategyStabilityComparisonAnalyzer(
            ValuationBacktestStabilityAnalyzer stabilityAnalyzer
    ) {

        this.stabilityAnalyzer =
                stabilityAnalyzer;
    }


    public ValuationStrategyStabilityComparisonReport analyze(
            List<ValuationBacktestObservation> observations
    ) {

        List<ValuationBacktestStabilitySummary> summaries =
                new ArrayList<>();


        for (ValuationBacktestMethod method
                : ValuationBacktestMethod.values()) {

            for (BacktestHorizon horizon
                    : BacktestHorizon.values()) {

                ValuationBacktestStabilitySummary summary =
                        stabilityAnalyzer.analyze(

                                observations,

                                method,

                                horizon
                        );


                summaries.add(
                        summary
                );
            }
        }


        return new ValuationStrategyStabilityComparisonReport(
                List.copyOf(summaries)
        );
    }
}