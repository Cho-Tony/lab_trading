package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.market.domain.MarketRegime;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MarketRegimeValuationBacktestAnalyzer {

    private final ValuationBacktestAnalyzer backtestAnalyzer;


    public MarketRegimeValuationBacktestAnalyzer(
            ValuationBacktestAnalyzer backtestAnalyzer
    ) {

        this.backtestAnalyzer =
                backtestAnalyzer;
    }


    public MarketRegimeValuationBacktestReport analyze(
            List<ValuationBacktestObservation> observations
    ) {

        List<MarketRegimeValuationBacktestSummary> summaries =
                new ArrayList<>();


        for (MarketRegime regime
                : MarketRegime.values()) {

            List<ValuationBacktestObservation> regimeObservations =
                    observations.stream()

                            .filter(
                                    observation ->
                                            observation != null
                                                    && observation.marketRegime() != null
                            )

                            .filter(
                                    observation ->
                                            observation
                                                    .marketRegime()
                                                    .regime()
                                                    == regime
                            )

                            .toList();


            for (ValuationBacktestMethod method
                    : ValuationBacktestMethod.values()) {

                for (BacktestHorizon horizon
                        : BacktestHorizon.values()) {

                    ValuationMethodBacktestSummary summary =
                            backtestAnalyzer.analyze(

                                    regimeObservations,

                                    method,

                                    horizon
                            );


                    summaries.add(
                            new MarketRegimeValuationBacktestSummary(

                                    regime,
                                    method,
                                    horizon,
                                    summary
                            )
                    );
                }
            }
        }


        return new MarketRegimeValuationBacktestReport(
                List.copyOf(summaries)
        );
    }
}