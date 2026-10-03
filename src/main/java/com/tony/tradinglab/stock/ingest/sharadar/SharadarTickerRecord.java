package com.tony.tradinglab.stock.ingest.sharadar;

import java.time.LocalDate;

public record SharadarTickerRecord(

        String table,

        String permaticker,

        String ticker,

        String name,

        String exchange,

        boolean delisted,

        String category,

        String sector,

        String industry,

        String currency,

        String relatedTickers,

        LocalDate firstPriceDate,

        LocalDate lastPriceDate

) {
}