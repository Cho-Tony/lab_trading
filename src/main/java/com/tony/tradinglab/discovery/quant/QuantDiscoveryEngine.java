package com.tony.tradinglab.discovery.quant;

import java.time.LocalDate;
import java.util.List;

public interface QuantDiscoveryEngine {

    List<QuantDiscoveryCandidate> discover(
            LocalDate observationDate
    );
}