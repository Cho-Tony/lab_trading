package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialHealthMetrics(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal cash,
        BigDecimal totalAssets,
        BigDecimal totalEquity,
        BigDecimal totalDebt,

        BigDecimal netDebt,

        BigDecimal debtToEquityRatio,
        BigDecimal cashToDebtRatio,

        LocalDate filedDate

) {
}