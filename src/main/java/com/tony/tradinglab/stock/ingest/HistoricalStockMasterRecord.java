package com.tony.tradinglab.stock.ingest;

import java.time.LocalDate;

public record HistoricalStockMasterRecord(

        String dataSource,

        String sourceSecurityId,

        String symbol,

        String name,

        String exchange,

        String market,

        String currency,

        String securityType,

        LocalDate listingStartDate,

        LocalDate listingEndDate,

        String delistingReason,

        String sector,

        String industry,

        boolean active

) {
}