package com.tony.tradinglab.discovery.quant;

import java.math.BigDecimal;

public record QuantScoreBreakdown(

        BigDecimal growthScore,

        BigDecimal qualityScore,

        BigDecimal valuationScore,

        boolean valuationAvailable,

        BigDecimal totalScore

) {
}