package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValuationBacktestDatasetBuilder {

    private final ValuationBacktestSnapshotService snapshotService;


    public ValuationBacktestDatasetBuilder(
            ValuationBacktestSnapshotService snapshotService
    ) {

        this.snapshotService =
                snapshotService;
    }


    public ValuationBacktestDataset build(
            List<ValuationBacktestSnapshotRequest> requests
    ) {

        if (requests == null
                || requests.isEmpty()) {

            return new ValuationBacktestDataset(
                    0,
                    0,
                    0,
                    List.of()
            );
        }


        List<ValuationBacktestObservation> observations =
                new ArrayList<>();


        int processedSnapshotCount =
                0;


        for (ValuationBacktestSnapshotRequest request
                : requests) {

            if (request == null
                    || request.valuationReport() == null) {

                continue;
            }


            ValuationBacktestSnapshotReport snapshotReport =
                    snapshotService.create(

                            request.valuationReport(),

                            request.stockPricesByStockId(),

                            request.benchmarkSymbol(),

                            request.benchmarkPrices()
                    );


            /*
             * observationDate 자체가 유효한 Snapshot만
             * 처리된 날짜로 계산.
             */
            if (snapshotReport.observationDate() == null) {

                continue;
            }


            processedSnapshotCount++;


            observations.addAll(
                    snapshotReport.observations()
            );
        }


        return new ValuationBacktestDataset(

                requests.size(),

                processedSnapshotCount,

                observations.size(),

                List.copyOf(
                        observations
                )
        );
    }
}