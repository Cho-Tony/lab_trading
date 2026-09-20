package com.tony.tradinglab.universe;

public record UniverseTarget(

        String symbol,
        String name,

        String exchange,
        String market,
        String currency,

        String sector,
        String industry

) {
}