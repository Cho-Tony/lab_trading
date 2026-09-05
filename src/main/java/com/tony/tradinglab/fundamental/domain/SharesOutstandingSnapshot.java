package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SharesOutstandingSnapshot(

        BigDecimal sharesOutstanding,

        LocalDate measuredDate,

        LocalDate filedDate,

        String tag

) {
}