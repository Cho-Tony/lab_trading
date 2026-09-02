package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuarterlyBalanceSheet(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal cash,
        BigDecimal totalAssets,
        BigDecimal totalEquity,

        LocalDate periodEndDate,
        LocalDate filedDate

) {
}