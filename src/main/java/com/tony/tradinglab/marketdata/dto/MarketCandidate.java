package com.tony.tradinglab.marketdata.dto;

import java.math.BigDecimal;

public record MarketCandidate(
        String symbol,
        String name,
        String exchange,
        String market,
        BigDecimal price,
        BigDecimal changePercent,
        Long volume
) {
}