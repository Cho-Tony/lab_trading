package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProfitabilityTrend(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal operatingMarginPct,
        BigDecimal previousQuarterOperatingMarginPct,
        BigDecimal operatingMarginChangePctPoint,

        BigDecimal netMarginPct,
        BigDecimal previousQuarterNetMarginPct,
        BigDecimal netMarginChangePctPoint,

        LocalDate filedDate

) {
}