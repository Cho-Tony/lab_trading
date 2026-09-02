package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GrowthAcceleration(

        Integer fiscalYear,
        String fiscalQuarter,

        // 현재 분기의 YoY 성장률
        BigDecimal yoyGrowthPct,

        // 직전 분기의 YoY 성장률
        BigDecimal previousQuarterYoyGrowthPct,

        // 현재 YoY - 직전 분기 YoY
        BigDecimal accelerationPctPoint,

        LocalDate filedDate

) {
}