package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Profitability(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal revenue,
        BigDecimal operatingIncome,
        BigDecimal netIncome,

        BigDecimal operatingMarginPct,
        BigDecimal netMarginPct,

        LocalDate filedDate

) {
}