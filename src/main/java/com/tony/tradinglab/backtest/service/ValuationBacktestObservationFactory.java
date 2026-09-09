package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.market.domain.MarketRegimeSnapshot;
import com.tony.tradinglab.market.service.MarketRegimeClassifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ValuationBacktestObservationFactory {

    private final ForwardReturnCalculator forwardReturnCalculator;
    private final ExcessReturnCalculator excessReturnCalculator;
    private final MarketRegimeClassifier marketRegimeClassifier;


    public ValuationBacktestObservationFactory(
            ForwardReturnCalculator forwardReturnCalculator,
            ExcessReturnCalculator excessReturnCalculator,
            MarketRegimeClassifier marketRegimeClassifier
    ) {

        this.forwardReturnCalculator =
                forwardReturnCalculator;

        this.excessReturnCalculator =
                excessReturnCalculator;

        this.marketRegimeClassifier =
                marketRegimeClassifier;
    }


    public Optional<ValuationBacktestObservation> create(
            ValuationBacktestObservationInput input
    ) {

        if (input == null
                || input.stockId() == null
                || input.symbol() == null
                || input.symbol().isBlank()
                || input.observationDate() == null
                || input.valuationComparison() == null
                || input.stockPrices() == null
                || input.benchmarkSymbol() == null
                || input.benchmarkSymbol().isBlank()
                || input.benchmarkPrices() == null) {

            return Optional.empty();
        }


        Optional<ForwardReturnMetrics> stockReturns =
                forwardReturnCalculator.calculate(

                        input.observationDate(),
                        input.stockPrices()
                );


        if (stockReturns.isEmpty()) {

            return Optional.empty();
        }


        Optional<ForwardReturnMetrics> benchmarkReturns =
                forwardReturnCalculator.calculate(

                        input.observationDate(),
                        input.benchmarkPrices()
                );


        if (benchmarkReturns.isEmpty()) {

            return Optional.empty();
        }


        ExcessReturnMetrics excessReturns =
                excessReturnCalculator.calculate(

                        input.benchmarkSymbol(),

                        stockReturns.get(),
                        benchmarkReturns.get()
                );


        /*
         * Regime 데이터가 부족해도 전체 valuation backtest
         * observation 자체는 버리지 않는다.
         *
         * 예:
         * benchmark 200D 이력이 부족한 초기 구간.
         *
         * MarketRegime별 분석에서는 기존 Analyzer가
         * marketRegime == null인 observation을 제외한다.
         */
        MarketRegimeSnapshot marketRegime =
                marketRegimeClassifier.classify(

                                input.benchmarkSymbol(),

                                input.observationDate(),

                                input.benchmarkPrices()
                        )

                        .orElse(null);


        return Optional.of(
                new ValuationBacktestObservation(

                        input.stockId(),

                        input.symbol(),

                        input.observationDate(),

                        input.valuationComparison(),

                        stockReturns.get(),

                        excessReturns,

                        marketRegime
                )
        );
    }
}