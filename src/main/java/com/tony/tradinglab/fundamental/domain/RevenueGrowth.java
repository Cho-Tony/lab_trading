package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueGrowth(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal revenue,
        BigDecimal previousYearRevenue,

        BigDecimal yoyGrowthPct,

        LocalDate filedDate

) {
}