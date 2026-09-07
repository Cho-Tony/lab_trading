package com.tony.tradinglab.stock.classification.service;

import com.tony.tradinglab.stock.classification.domain.StockClassification;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationEntity;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StockClassificationService {

    private final StockClassificationRepository repository;
    private final StockClassificationMapper mapper;
    private final StockClassificationResolver resolver;


    public StockClassificationService(
            StockClassificationRepository repository,
            StockClassificationMapper mapper,
            StockClassificationResolver resolver
    ) {

        this.repository = repository;
        this.mapper = mapper;
        this.resolver = resolver;
    }


    public Optional<StockClassification> findAsOf(
            Long stockId,
            LocalDate asOfDate
    ) {

        if (stockId == null
                || asOfDate == null) {

            return Optional.empty();
        }


        List<StockClassificationEntity> entities =
                repository
                        .findByStockIdOrderByEffectiveFromAsc(
                                stockId
                        );


        List<StockClassification> classifications =
                entities.stream()

                        .map(
                                mapper::toDomain
                        )

                        .toList();


        return resolver.resolve(
                stockId,
                asOfDate,
                classifications
        );
    }
}