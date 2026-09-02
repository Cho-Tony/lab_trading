package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FreeCashFlowMetrics(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal revenue,

        BigDecimal operatingCashFlow,
        BigDecimal capitalExpenditure,
        BigDecimal freeCashFlow,

        BigDecimal freeCashFlowMarginPct,

        BigDecimal previousYearFreeCashFlow,
        BigDecimal freeCashFlowGrowthYoYPct,

        boolean turnaround,

        LocalDate filedDate

) {
}