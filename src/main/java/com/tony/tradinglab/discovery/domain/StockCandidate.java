package com.tony.tradinglab.discovery.domain;

import java.time.LocalDateTime;

public record StockCandidate(

        Long stockId,
        String symbol,

        DiscoverySource discoverySource,

        LocalDateTime discoveredAt

) {
}