package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ValuationMetrics(

        Integer fiscalYear,
        String fiscalQuarter,

        LocalDate priceDate,
        BigDecimal sharePrice,
        BigDecimal sharesOutstanding,
        BigDecimal marketCap,

        BigDecimal ttmRevenue,
        BigDecimal ttmNetIncome,
        BigDecimal ttmFreeCashFlow,

        BigDecimal peRatio,
        BigDecimal psRatio,
        BigDecimal priceToFcfRatio,

        LocalDate filedDate

) {
}