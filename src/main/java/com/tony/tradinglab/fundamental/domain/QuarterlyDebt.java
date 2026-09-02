package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuarterlyDebt(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal shortTermDebt,
        BigDecimal currentLongTermDebt,
        BigDecimal longTermDebt,

        BigDecimal totalDebt,

        LocalDate filedDate

) {
}