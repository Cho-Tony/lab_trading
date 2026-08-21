package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SecFactPoint(

        String tag,
        BigDecimal value,

        LocalDate startDate,
        LocalDate endDate,
        LocalDate filedDate,

        Integer fiscalYear,
        String fiscalPeriod,
        String form,
        String accessionNumber

) {
}