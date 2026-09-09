package com.tony.tradinglab.backtest.domain;

import java.time.LocalDate;
import java.util.List;

public record ValuationBacktestSnapshotReport(

        LocalDate observationDate,

        int valuationResultCount,
        int observationCount,

        List<ValuationBacktestObservation> observations

) {
}