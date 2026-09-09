package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.fundamental.domain.ValuationSnapshotAnalysisReport;
import com.tony.tradinglab.fundamental.service.ValuationSnapshotAnalysisService;
import com.tony.tradinglab.price.domain.StockPrice;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ValuationBacktestRequestPlanner {

    private final BacktestObservationDateGenerator dateGenerator;
    private final ValuationSnapshotAnalysisService valuationAnalysisService;


    public ValuationBacktestRequestPlanner(
            BacktestObservationDateGenerator dateGenerator,
            ValuationSnapshotAnalysisService valuationAnalysisService
    ) {

        this.dateGenerator =
                dateGenerator;

        this.valuationAnalysisService =
                valuationAnalysisService;
    }


    public ValuationBacktestRequestPlan plan(
            LocalDate startDate,
            LocalDate endDate,
            BacktestObservationFrequency frequency,

            List<ValuationPeerSnapshotInput> valuationInputs,

            Map<Long, List<StockPrice>> stockPricesByStockId,

            String benchmarkSymbol,
            List<StockPrice> benchmarkPrices
    ) {

        if (startDate == null
                || endDate == null
                || frequency == null
                || valuationInputs == null
                || stockPricesByStockId == null
                || benchmarkSymbol == null
                || benchmarkSymbol.isBlank()
                || benchmarkPrices == null
                || startDate.isAfter(endDate)) {

            return new ValuationBacktestRequestPlan(

                    startDate,
                    endDate,

                    frequency,

                    0,
                    0,

                    List.of()
            );
        }


        /*
         * 실제 benchmark 거래일을 기준으로
         * observation date 생성.
         */
        List<LocalDate> observationDates =
                dateGenerator.generate(

                        startDate,
                        endDate,

                        frequency,

                        benchmarkPrices
                );


        /*
         * 모든 날짜마다 전체 valuationInputs를
         * 매번 처음부터 검색하지 않도록
         * 날짜별로 한 번 grouping 한다.
         */
        Map<LocalDate, List<ValuationPeerSnapshotInput>> inputsByDate =
                valuationInputs.stream()

                        .filter(
                                input ->
                                        input != null
                                                && input.observationDate() != null
                        )

                        .collect(
                                Collectors.groupingBy(
                                        ValuationPeerSnapshotInput
                                                ::observationDate
                                )
                        );


        List<ValuationBacktestSnapshotRequest> requests =
                observationDates.stream()

                        .map(
                                observationDate -> {

                                    List<ValuationPeerSnapshotInput> inputs =
                                            inputsByDate.getOrDefault(

                                                    observationDate,

                                                    List.of()
                                            );


                                    /*
                                     * 해당 날짜의 fundamental /
                                     * valuation 재료 자체가 없다면
                                     * request를 만들지 않는다.
                                     */
                                    if (inputs.isEmpty()) {

                                        return null;
                                    }


                                    ValuationSnapshotAnalysisReport valuationReport =
                                            valuationAnalysisService.analyze(

                                                    observationDate,

                                                    inputs
                                            );


                                    return new ValuationBacktestSnapshotRequest(

                                            valuationReport,

                                            stockPricesByStockId,

                                            benchmarkSymbol,

                                            benchmarkPrices
                                    );
                                }
                        )

                        .filter(
                                request ->
                                        request != null
                        )

                        .toList();


        return new ValuationBacktestRequestPlan(

                startDate,
                endDate,

                frequency,

                observationDates.size(),
                requests.size(),

                List.copyOf(
                        requests
                )
        );
    }
}