package com.tony.tradinglab.discovery.quant;

import java.time.LocalDate;

public record QuantDiscoveryCandidate(

        Long stockId,

        String symbol,

        LocalDate observationDate,

        QuantScoreBreakdown score

) {
}