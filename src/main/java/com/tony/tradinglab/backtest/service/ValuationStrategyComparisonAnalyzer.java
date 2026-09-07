package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValuationStrategyComparisonAnalyzer {

    private final ValuationBacktestAnalyzer backtestAnalyzer;


    public ValuationStrategyComparisonAnalyzer(
            ValuationBacktestAnalyzer backtestAnalyzer
    ) {

        this.backtestAnalyzer =
                backtestAnalyzer;
    }


    public ValuationStrategyComparisonReport analyze(
            List<ValuationBacktestObservation> observations
    ) {

        List<ValuationMethodBacktestSummary> summaries =
                new ArrayList<>();


        for (ValuationBacktestMethod method
                : ValuationBacktestMethod.values()) {

            for (BacktestHorizon horizon
                    : BacktestHorizon.values()) {

                ValuationMethodBacktestSummary summary =
                        backtestAnalyzer.analyze(
                                observations,
                                method,
                                horizon
                        );


                summaries.add(
                        summary
                );
            }
        }


        return new ValuationStrategyComparisonReport(
                List.copyOf(summaries)
        );
    }
}