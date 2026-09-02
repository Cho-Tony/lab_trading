package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LiquidityMetrics(

        Integer fiscalYear,
        String fiscalQuarter,

        BigDecimal cash,

        BigDecimal ttmOperatingCashFlow,
        BigDecimal ttmFreeCashFlow,

        BigDecimal annualCashBurn,

        BigDecimal cashRunwayYears,
        BigDecimal cashRunwayQuarters,

        boolean operatingCashFlowPositive,
        boolean freeCashFlowPositive,

        boolean capexDrivenNegativeFcf,

        LocalDate filedDate

) {
}