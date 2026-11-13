package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.fundamental.domain.FundamentalDataCollectionPolicy;
import java.time.LocalDate;
import java.util.List;

public interface QuantDiscoveryEngine {

    default List<QuantDiscoveryCandidate> discover(
            LocalDate observationDate
    ) {
        return discover(observationDate, FundamentalDataCollectionPolicy.AUTO_SYNC_IF_EMPTY);
    }

    List<QuantDiscoveryCandidate> discover(
            LocalDate observationDate,
            FundamentalDataCollectionPolicy collectionPolicy
    );
}
