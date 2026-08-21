package com.tony.tradinglab.marketdata.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyPrice(
        LocalDate tradeDate,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal adjustedClose,
        Long volume
) {
}