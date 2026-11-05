package com.tony.tradinglab.backtest.outcome;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FutureReturnResult(

        Long stockId,

        LocalDate observationDate,

        LocalDate targetDate,

        LocalDate entryPriceDate,

        LocalDate exitPriceDate,

        BigDecimal entryAdjustedClose,

        BigDecimal exitAdjustedClose,

        BigDecimal returnRate,

        boolean terminalBeforeTarget,

        TerminalOutcomeType terminalOutcomeType

) {
}