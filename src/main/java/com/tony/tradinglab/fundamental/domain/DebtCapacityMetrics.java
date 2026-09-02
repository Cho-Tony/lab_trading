package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebtCapacityMetrics(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal totalDebt,
        BigDecimal cash,
        BigDecimal netDebt,

        BigDecimal ttmOperatingCashFlow,
        BigDecimal ttmFreeCashFlow,

        BigDecimal netDebtToTtmFcfRatio,
        BigDecimal debtToTtmFcfRatio,

        boolean netCash,

        LocalDate filedDate

) {
}