package com.tony.tradinglab.backtest.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ForwardReturnMetrics(

        LocalDate observationDate,

        BigDecimal observationPrice,

        LocalDate return63dDate,
        BigDecimal return63dPct,

        LocalDate return126dDate,
        BigDecimal return126dPct,

        LocalDate return252dDate,
        BigDecimal return252dPct

) {
}