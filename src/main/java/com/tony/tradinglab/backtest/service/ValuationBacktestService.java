package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.price.domain.StockPrice;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ValuationBacktestService {

    private final ValuationBacktestRequestPlanner requestPlanner;

    private final ValuationBacktestDatasetBuilder datasetBuilder;

    private final ValuationStrategyComparisonAnalyzer comparisonAnalyzer;

    private final ValuationStrategyStabilityComparisonAnalyzer
            stabilityComparisonAnalyzer;

    private final MarketRegimeValuationBacktestAnalyzer
            marketRegimeAnalyzer;


    public ValuationBacktestService(
            ValuationBacktestRequestPlanner requestPlanner,
            ValuationBacktestDatasetBuilder datasetBuilder,
            ValuationStrategyComparisonAnalyzer comparisonAnalyzer,
            ValuationStrategyStabilityComparisonAnalyzer stabilityComparisonAnalyzer,
            MarketRegimeValuationBacktestAnalyzer marketRegimeAnalyzer
    ) {

        this.requestPlanner =
                requestPlanner;

        this.datasetBuilder =
                datasetBuilder;

        this.comparisonAnalyzer =
                comparisonAnalyzer;

        this.stabilityComparisonAnalyzer =
                stabilityComparisonAnalyzer;

        this.marketRegimeAnalyzer =
                marketRegimeAnalyzer;
    }


    public ValuationBacktestRunReport run(
            LocalDate startDate,
            LocalDate endDate,

            BacktestObservationFrequency frequency,

            List<ValuationPeerSnapshotInput> valuationInputs,

            Map<Long, List<StockPrice>> stockPricesByStockId,

            String benchmarkSymbol,

            List<StockPrice> benchmarkPrices
    ) {

        /*
         * 1.
         * 실제 거래일 기준으로 observation date를 만들고
         * 날짜별 valuation 분석 요청을 준비한다.
         */
        ValuationBacktestRequestPlan requestPlan =
                requestPlanner.plan(

                        startDate,
                        endDate,

                        frequency,

                        valuationInputs,

                        stockPricesByStockId,

                        benchmarkSymbol,

                        benchmarkPrices
                );


        /*
         * 2.
         * 날짜별 valuation 결과에
         *
         * Forward Return
         * Excess Return
         * Market Regime
         *
         * 을 붙여 최종 observation dataset 생성.
         */
        ValuationBacktestDataset dataset =
                datasetBuilder.build(
                        requestPlan.requests()
                );


        List<ValuationBacktestObservation> observations =
                dataset.observations();


        /*
         * 3.
         * 전체 기간 기준
         *
         * Percentile
         * Robust Z
         * Regression
         *
         * 비교
         */
        ValuationStrategyComparisonReport overallComparison =
                comparisonAnalyzer.analyze(
                        observations
                );


        /*
         * 4.
         * 연도별 안정성 분석
         */
        ValuationStrategyStabilityComparisonReport stabilityComparison =
                stabilityComparisonAnalyzer.analyze(
                        observations
                );


        /*
         * 5.
         * BULL / NORMAL / BEAR
         * 시장 국면별 분석
         */
        MarketRegimeValuationBacktestReport marketRegimeComparison =
                marketRegimeAnalyzer.analyze(
                        observations
                );


        return new ValuationBacktestRunReport(

                requestPlan,

                dataset,

                overallComparison,

                stabilityComparison,

                marketRegimeComparison
        );
    }
}