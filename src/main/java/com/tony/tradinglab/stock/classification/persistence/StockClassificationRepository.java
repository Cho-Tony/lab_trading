package com.tony.tradinglab.stock.classification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockClassificationRepository
        extends JpaRepository<StockClassificationEntity, Long> {

    List<StockClassificationEntity>
    findByStockIdOrderByEffectiveFromAsc(
            Long stockId
    );
}