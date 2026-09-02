package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialHealthTrend(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal netDebt,
        BigDecimal previousQuarterNetDebt,
        BigDecimal netDebtChange,

        BigDecimal debtToEquityRatio,
        BigDecimal previousQuarterDebtToEquityRatio,
        BigDecimal debtToEquityChange,

        BigDecimal cashToDebtRatio,
        BigDecimal previousQuarterCashToDebtRatio,
        BigDecimal cashToDebtChange,

        LocalDate filedDate

) {
}