package com.tony.tradinglab.backtest.domain;

import java.math.BigDecimal;

public record ExcessReturnMetrics(

        String benchmarkSymbol,

        BigDecimal stockReturn63dPct,
        BigDecimal benchmarkReturn63dPct,
        BigDecimal excessReturn63dPct,

        BigDecimal stockReturn126dPct,
        BigDecimal benchmarkReturn126dPct,
        BigDecimal excessReturn126dPct,

        BigDecimal stockReturn252dPct,
        BigDecimal benchmarkReturn252dPct,
        BigDecimal excessReturn252dPct

) {
}