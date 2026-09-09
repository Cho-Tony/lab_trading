package com.tony.tradinglab.fundamental.domain;

import java.time.LocalDate;
import java.util.List;

public record PointInTimeFundamentalContext(

        Long stockId,
        String symbol,

        LocalDate asOfDate,

        List<QuarterlyFinancials> quarterlyFinancials

) {
}