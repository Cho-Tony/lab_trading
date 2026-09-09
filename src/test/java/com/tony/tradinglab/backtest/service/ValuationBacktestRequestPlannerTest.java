package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.fundamental.domain.ValuationSnapshotAnalysisReport;
import com.tony.tradinglab.fundamental.service.ValuationSnapshotAnalysisService;
import com.tony.tradinglab.price.domain.StockPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValuationBacktestRequestPlannerTest {

    private BacktestObservationDateGenerator dateGenerator;

    private ValuationSnapshotAnalysisService valuationAnalysisService;

    private ValuationBacktestRequestPlanner planner;


    @BeforeEach
    void setUp() {

        dateGenerator =
                mock(
                        BacktestObservationDateGenerator.class
                );


        valuationAnalysisService =
                mock(
                        ValuationSnapshotAnalysisService.class
                );


        planner =
                new ValuationBacktestRequestPlanner(

                        dateGenerator,
                        valuationAnalysisService
                );
    }


    @Test
    void planBacktestRequestsForGeneratedDates() {

        LocalDate startDate =
                LocalDate.of(
                        2024,
                        1,
                        1
                );

        LocalDate endDate =
                LocalDate.of(
                        2024,
                        12,
                        31
                );


        LocalDate date1 =
                LocalDate.of(
                        2024,
                        3,
                        29
                );

        LocalDate date2 =
                LocalDate.of(
                        2024,
                        6,
                        28
                );


        List<StockPrice> benchmarkPrices =
                List.of(
                        mock(StockPrice.class)
                );


        when(
                dateGenerator.generate(

                        startDate,
                        endDate,

                        BacktestObservationFrequency.QUARTERLY,

                        benchmarkPrices
                )
        )
                .thenReturn(
                        List.of(
                                date1,
                                date2
                        )
                );


        ValuationPeerSnapshotInput input1 =
                mock(
                        ValuationPeerSnapshotInput.class
                );

        ValuationPeerSnapshotInput input2 =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        when(
                input1.observationDate()
        )
                .thenReturn(
                        date1
                );


        when(
                input2.observationDate()
        )
                .thenReturn(
                        date2
                );


        ValuationSnapshotAnalysisReport report1 =
                mock(
                        ValuationSnapshotAnalysisReport.class
                );

        ValuationSnapshotAnalysisReport report2 =
                mock(
                        ValuationSnapshotAnalysisReport.class
                );


        when(
                valuationAnalysisService.analyze(

                        date1,
                        List.of(input1)
                )
        )
                .thenReturn(
                        report1
                );


        when(
                valuationAnalysisService.analyze(

                        date2,
                        List.of(input2)
                )
        )
                .thenReturn(
                        report2
                );


        Map<Long, List<StockPrice>> stockPricesByStockId =
                Map.of();


        ValuationBacktestRequestPlan result =
                planner.plan(

                        startDate,
                        endDate,

                        BacktestObservationFrequency.QUARTERLY,

                        List.of(
                                input1,
                                input2
                        ),

                        stockPricesByStockId,

                        "QQQ",

                        benchmarkPrices
                );


        assertThat(
                result.generatedObservationDateCount()
        ).isEqualTo(
                2
        );


        assertThat(
                result.plannedRequestCount()
        ).isEqualTo(
                2
        );


        assertThat(
                result.requests()
        ).hasSize(
                2
        );


        assertThat(
                result.requests().get(0)
                        .valuationReport()
        ).isSameAs(
                report1
        );


        assertThat(
                result.requests().get(1)
                        .valuationReport()
        ).isSameAs(
                report2
        );


        verify(
                valuationAnalysisService
        )
                .analyze(
                        date1,
                        List.of(input1)
                );


        verify(
                valuationAnalysisService
        )
                .analyze(
                        date2,
                        List.of(input2)
                );
    }

    @Test
    void skipObservationDateWhenValuationInputsAreMissing() {

        LocalDate startDate =
                LocalDate.of(
                        2024,
                        1,
                        1
                );

        LocalDate endDate =
                LocalDate.of(
                        2024,
                        6,
                        30
                );


        LocalDate date1 =
                LocalDate.of(
                        2024,
                        3,
                        29
                );

        LocalDate date2 =
                LocalDate.of(
                        2024,
                        6,
                        28
                );


        List<StockPrice> benchmarkPrices =
                List.of(
                        mock(StockPrice.class)
                );


        when(
                dateGenerator.generate(

                        startDate,
                        endDate,

                        BacktestObservationFrequency.QUARTERLY,

                        benchmarkPrices
                )
        )
                .thenReturn(
                        List.of(
                                date1,
                                date2
                        )
                );


        /*
         * date1 데이터만 존재.
         * date2 valuation input 없음.
         */
        ValuationPeerSnapshotInput input =
                mock(
                        ValuationPeerSnapshotInput.class
                );


        when(
                input.observationDate()
        )
                .thenReturn(
                        date1
                );


        ValuationSnapshotAnalysisReport report =
                mock(
                        ValuationSnapshotAnalysisReport.class
                );


        when(
                valuationAnalysisService.analyze(

                        date1,
                        List.of(input)
                )
        )
                .thenReturn(
                        report
                );


        ValuationBacktestRequestPlan result =
                planner.plan(

                        startDate,
                        endDate,

                        BacktestObservationFrequency.QUARTERLY,

                        List.of(input),

                        Map.of(),

                        "QQQ",

                        benchmarkPrices
                );


        /*
         * 날짜 자체는 2개 생성됐지만
         * valuation 자료가 있는 날짜는 1개.
         */
        assertThat(
                result.generatedObservationDateCount()
        ).isEqualTo(
                2
        );


        assertThat(
                result.plannedRequestCount()
        ).isEqualTo(
                1
        );


        assertThat(
                result.requests()
        ).hasSize(
                1
        );


        /*
         * date2는 분석 자체를 하면 안 된다.
         */
        verify(
                valuationAnalysisService,
                never()
        )
                .analyze(
                        eq(date2),
                        anyList()
                );
    }
}