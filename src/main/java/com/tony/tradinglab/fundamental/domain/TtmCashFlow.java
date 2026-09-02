package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TtmCashFlow(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal operatingCashFlow,
        BigDecimal capitalExpenditure,
        BigDecimal freeCashFlow,

        LocalDate filedDate

) {
}