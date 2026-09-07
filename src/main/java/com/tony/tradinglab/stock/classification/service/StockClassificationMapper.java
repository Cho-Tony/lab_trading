package com.tony.tradinglab.stock.classification.service;

import com.tony.tradinglab.stock.classification.domain.StockClassification;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationEntity;
import org.springframework.stereotype.Component;

@Component
public class StockClassificationMapper {

    public StockClassification toDomain(
            StockClassificationEntity entity
    ) {

        return new StockClassification(

                entity.getStockId(),

                entity.getSector(),
                entity.getIndustry(),

                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),

                entity.getSource()
        );
    }
}