package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;

public record RobustZScoreMetric(

        BigDecimal targetValue,

        BigDecimal median,
        BigDecimal mad,

        BigDecimal robustZScore,

        int validPeerCount

) {
}