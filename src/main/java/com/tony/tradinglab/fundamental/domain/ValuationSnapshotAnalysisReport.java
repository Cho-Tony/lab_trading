package com.tony.tradinglab.fundamental.domain;

import java.time.LocalDate;
import java.util.List;

public record ValuationSnapshotAnalysisReport(

        LocalDate observationDate,

        int requestedStockCount,
        int analyzableStockCount,

        List<ValuationComparisonResult> results

) {
}