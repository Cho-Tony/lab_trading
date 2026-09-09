package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationSnapshotAnalysisReport;
import com.tony.tradinglab.price.domain.StockPrice;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ValuationBacktestSnapshotService {

    private final ValuationBacktestObservationFactory observationFactory;


    public ValuationBacktestSnapshotService(
            ValuationBacktestObservationFactory observationFactory
    ) {

        this.observationFactory =
                observationFactory;
    }


    public ValuationBacktestSnapshotReport create(
            ValuationSnapshotAnalysisReport valuationReport,
            Map<Long, List<StockPrice>> stockPricesByStockId,
            String benchmarkSymbol,
            List<StockPrice> benchmarkPrices
    ) {

        if (valuationReport == null
                || valuationReport.observationDate() == null
                || stockPricesByStockId == null
                || benchmarkSymbol == null
                || benchmarkSymbol.isBlank()
                || benchmarkPrices == null) {

            return new ValuationBacktestSnapshotReport(
                    valuationReport != null
                            ? valuationReport.observationDate()
                            : null,
                    0,
                    0,
                    List.of()
            );
        }


        LocalDate observationDate =
                valuationReport.observationDate();


        List<ValuationComparisonResult> valuationResults =
                valuationReport.results();


        if (valuationResults == null
                || valuationResults.isEmpty()) {

            return new ValuationBacktestSnapshotReport(
                    observationDate,
                    0,
                    0,
                    List.of()
            );
        }


        List<ValuationBacktestObservation> observations =
                new ArrayList<>();


        for (ValuationComparisonResult valuationResult
                : valuationResults) {

            if (valuationResult == null
                    || valuationResult.stockId() == null
                    || valuationResult.symbol() == null) {

                continue;
            }


            List<StockPrice> stockPrices =
                    stockPricesByStockId.get(
                            valuationResult.stockId()
                    );


            /*
             * 해당 종목의 가격 이력이 없으면
             * 미래수익률을 계산할 수 없으므로 제외.
             */
            if (stockPrices == null
                    || stockPrices.isEmpty()) {

                continue;
            }


            ValuationBacktestObservationInput input =
                    new ValuationBacktestObservationInput(

                            valuationResult.stockId(),

                            valuationResult.symbol(),

                            observationDate,

                            valuationResult,

                            stockPrices,

                            benchmarkSymbol,
                            benchmarkPrices
                    );


            observationFactory.create(
                            input
                    )

                    .ifPresent(
                            observations::add
                    );
        }


        return new ValuationBacktestSnapshotReport(

                observationDate,

                valuationResults.size(),

                observations.size(),

                List.copyOf(
                        observations
                )
        );
    }
}