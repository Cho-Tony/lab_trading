package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuarterlyFact(

        String tag,
        BigDecimal value,

        LocalDate startDate,
        LocalDate endDate,

        // 시장에서 이 숫자를 알 수 있게 된 날짜
        LocalDate filedDate,

        Integer fiscalYear,
        String fiscalQuarter,

        // true면 10-K 연간값에서 Q1~Q3를 빼서 계산한 Q4
        boolean derived

) {
}