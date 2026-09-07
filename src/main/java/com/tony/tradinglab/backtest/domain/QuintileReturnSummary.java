package com.tony.tradinglab.backtest.domain;

import java.math.BigDecimal;

public record QuintileReturnSummary(

        int quintile,

        int observationCount,

        BigDecimal averageExcessReturnPct

) {
}