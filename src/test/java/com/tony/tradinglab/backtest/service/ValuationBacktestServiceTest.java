package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.price.domain.StockPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValuationBacktestServiceTest {

    private ValuationBacktestRequestPlanner requestPlanner;

    private ValuationBacktestDatasetBuilder datasetBuilder;

    private ValuationStrategyComparisonAnalyzer comparisonAnalyzer;

    private ValuationStrategyStabilityComparisonAnalyzer
            stabilityComparisonAnalyzer;

    private MarketRegimeValuationBacktestAnalyzer
            marketRegimeAnalyzer;

    private ValuationBacktestService service;


    @BeforeEach
    void setUp() {

        requestPlanner =
                mock(
                        ValuationBacktestRequestPlanner.class
                );


        datasetBuilder =
                mock(
                        ValuationBacktestDatasetBuilder.class
                );


        comparisonAnalyzer =
                mock(
                        ValuationStrategyComparisonAnalyzer.class
                );


        stabilityComparisonAnalyzer =
                mock(
                        ValuationStrategyStabilityComparisonAnalyzer.class
                );


        marketRegimeAnalyzer =
                mock(
                        MarketRegimeValuationBacktestAnalyzer.class
                );


        service =
                new ValuationBacktestService(

                        requestPlanner,

                        datasetBuilder,

                        comparisonAnalyzer,

                        stabilityComparisonAnalyzer,

                        marketRegimeAnalyzer
                );
    }


    @Test
    void runCompleteValuationBacktestPipeline() {

        LocalDate startDate =
                LocalDate.of(
                        2023,
                        1,
                        1
                );


        LocalDate endDate =
                LocalDate.of(
                        2025,
                        12,
                        31
                );


        List<ValuationPeerSnapshotInput> valuationInputs =
                List.of(
                        mock(
                                ValuationPeerSnapshotInput.class
                        )
                );


        Map<Long, List<StockPrice>> stockPricesByStockId =
                Map.of();


        List<StockPrice> benchmarkPrices =
                List.of(
                        mock(
                                StockPrice.class
                        )
                );


        ValuationBacktestSnapshotRequest snapshotRequest =
                mock(
                        ValuationBacktestSnapshotRequest.class
                );


        ValuationBacktestRequestPlan requestPlan =
                new ValuationBacktestRequestPlan(

                        startDate,
                        endDate,

                        BacktestObservationFrequency.QUARTERLY,

                        1,
                        1,

                        List.of(
                                snapshotRequest
                        )
                );


        when(
                requestPlanner.plan(

                        startDate,
                        endDate,

                        BacktestObservationFrequency.QUARTERLY,

                        valuationInputs,

                        stockPricesByStockId,

                        "QQQ",

                        benchmarkPrices
                )
        )
                .thenReturn(
                        requestPlan
                );


        ValuationBacktestObservation observation1 =
                mock(
                        ValuationBacktestObservation.class
                );


        ValuationBacktestObservation observation2 =
                mock(
                        ValuationBacktestObservation.class
                );


        List<ValuationBacktestObservation> observations =
                List.of(
                        observation1,
                        observation2
                );


        ValuationBacktestDataset dataset =
                new ValuationBacktestDataset(

                        1,
                        1,
                        2,

                        observations
                );


        when(
                datasetBuilder.build(
                        requestPlan.requests()
                )
        )
                .thenReturn(
                        dataset
                );


        ValuationStrategyComparisonReport overallComparison =
                mock(
                        ValuationStrategyComparisonReport.class
                );


        ValuationStrategyStabilityComparisonReport stabilityComparison =
                mock(
                        ValuationStrategyStabilityComparisonReport.class
                );


        MarketRegimeValuationBacktestReport marketRegimeComparison =
                mock(
                        MarketRegimeValuationBacktestReport.class
                );


        when(
                comparisonAnalyzer.analyze(
                        observations
                )
        )
                .thenReturn(
                        overallComparison
                );


        when(
                stabilityComparisonAnalyzer.analyze(
                        observations
                )
        )
                .thenReturn(
                        stabilityComparison
                );


        when(
                marketRegimeAnalyzer.analyze(
                        observations
                )
        )
                .thenReturn(
                        marketRegimeComparison
                );


        ValuationBacktestRunReport result =
                service.run(

                        startDate,
                        endDate,

                        BacktestObservationFrequency.QUARTERLY,

                        valuationInputs,

                        stockPricesByStockId,

                        "QQQ",

                        benchmarkPrices
                );


        assertThat(
                result.requestPlan()
        ).isSameAs(
                requestPlan
        );


        assertThat(
                result.dataset()
        ).isSameAs(
                dataset
        );


        assertThat(
                result.overallComparison()
        ).isSameAs(
                overallComparison
        );


        assertThat(
                result.stabilityComparison()
        ).isSameAs(
                stabilityComparison
        );


        assertThat(
                result.marketRegimeComparison()
        ).isSameAs(
                marketRegimeComparison
        );


        InOrder inOrder =
                inOrder(

                        requestPlanner,

                        datasetBuilder,

                        comparisonAnalyzer,

                        stabilityComparisonAnalyzer,

                        marketRegimeAnalyzer
                );


        inOrder.verify(
                        requestPlanner
                )
                .plan(

                        startDate,
                        endDate,

                        BacktestObservationFrequency.QUARTERLY,

                        valuationInputs,

                        stockPricesByStockId,

                        "QQQ",

                        benchmarkPrices
                );


        inOrder.verify(
                        datasetBuilder
                )
                .build(
                        requestPlan.requests()
                );


        inOrder.verify(
                        comparisonAnalyzer
                )
                .analyze(
                        observations
                );


        inOrder.verify(
                        stabilityComparisonAnalyzer
                )
                .analyze(
                        observations
                );


        inOrder.verify(
                        marketRegimeAnalyzer
                )
                .analyze(
                        observations
                );
    }




}

