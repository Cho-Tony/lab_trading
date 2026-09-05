package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TtmFinancials(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal revenue,
        BigDecimal operatingIncome,
        BigDecimal netIncome,

        LocalDate filedDate

) {
}