package com.tony.tradinglab.backtest.domain;

import java.time.LocalDate;
import java.util.List;

public record ValuationBacktestRequestPlan(

        LocalDate startDate,
        LocalDate endDate,

        BacktestObservationFrequency frequency,

        int generatedObservationDateCount,
        int plannedRequestCount,

        List<ValuationBacktestSnapshotRequest> requests

) {
}