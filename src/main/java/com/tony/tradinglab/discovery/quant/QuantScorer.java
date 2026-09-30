package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;

public interface QuantScorer {

    QuantScoreBreakdown score(
            ValuationPeerSnapshotInput snapshot,
            ValuationComparisonResult valuationComparison
    );
}