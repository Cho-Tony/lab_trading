package com.tony.tradinglab.market.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketRegimeSnapshot(

        String benchmarkSymbol,

        LocalDate asOfDate,
        LocalDate marketDate,

        BigDecimal benchmarkPrice,

        BigDecimal movingAverage200,

        BigDecimal return63dPct,

        MarketRegime regime

) {
}