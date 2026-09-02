package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CapitalInvestmentMetrics(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal revenue,

        BigDecimal capitalExpenditure,
        BigDecimal previousYearCapitalExpenditure,

        BigDecimal capexGrowthYoYPct,
        BigDecimal capexToRevenuePct,

        LocalDate filedDate

) {
}