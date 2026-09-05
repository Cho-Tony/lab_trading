package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;

public record PercentileValuationAssessment(

        String symbol,

        BigDecimal pePercentile,
        BigDecimal psPercentile,
        BigDecimal priceToFcfPercentile,

        int validPePeerCount,
        int validPsPeerCount,
        int validPriceToFcfPeerCount

) {
}