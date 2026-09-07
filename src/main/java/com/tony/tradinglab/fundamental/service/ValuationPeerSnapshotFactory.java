package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.GrowthTrendAnalysis;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrendAnalysis;
import com.tony.tradinglab.fundamental.domain.ValuationMetrics;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import com.tony.tradinglab.stock.classification.domain.StockClassification;
import com.tony.tradinglab.stock.classification.service.StockClassificationService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class ValuationPeerSnapshotFactory {

    private final StockClassificationService classificationService;


    public ValuationPeerSnapshotFactory(
            StockClassificationService classificationService
    ) {

        this.classificationService =
                classificationService;
    }


    public Optional<ValuationPeerSnapshot> create(
            Long stockId,
            String symbol,
            LocalDate observationDate,
            ValuationMetrics valuation,
            GrowthTrendAnalysis growth,
            ProfitabilityTrendAnalysis profitability
    ) {

        if (stockId == null
                || symbol == null
                || symbol.isBlank()
                || observationDate == null
                || valuation == null) {

            return Optional.empty();
        }


        Optional<StockClassification> classification =
                classificationService.findAsOf(
                        stockId,
                        observationDate
                );


        if (classification.isEmpty()) {

            return Optional.empty();
        }


        StockClassification resolved =
                classification.get();


        return Optional.of(
                new ValuationPeerSnapshot(

                        stockId,
                        symbol,

                        resolved.sector(),
                        resolved.industry(),

                        valuation,
                        growth,
                        profitability
                )
        );
    }
}