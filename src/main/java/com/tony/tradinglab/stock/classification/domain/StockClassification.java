package com.tony.tradinglab.stock.classification.domain;

import java.time.LocalDate;

public record StockClassification(

        Long stockId,

        String sector,
        String industry,

        LocalDate effectiveFrom,
        LocalDate effectiveTo,

        ClassificationSource source

) {
}