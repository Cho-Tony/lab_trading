package com.tony.tradinglab.stock.domain.repository;

import com.tony.tradinglab.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbolAndExchange(
            String symbol,
            String exchange
    );

    boolean existsBySymbolAndExchange(
            String symbol,
            String exchange
    );
}