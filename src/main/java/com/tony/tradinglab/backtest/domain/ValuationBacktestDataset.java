package com.tony.tradinglab.backtest.domain;

import java.util.List;

public record ValuationBacktestDataset(

        int requestedSnapshotCount,

        int processedSnapshotCount,

        int observationCount,

        List<ValuationBacktestObservation> observations

) {
}