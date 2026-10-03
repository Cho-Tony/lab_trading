package com.tony.tradinglab.stock.repository;

import com.tony.tradinglab.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockRepository
        extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbolAndExchange(
            String symbol,
            String exchange
    );

    boolean existsBySymbolAndExchange(
            String symbol,
            String exchange
    );


    List<Stock> findAllByDataSourceAndSourceSecurityId(
            String dataSource,
            String sourceSecurityId
    );


    @Query("""
            SELECT s
            FROM Stock s
            WHERE s.listingStartDate IS NOT NULL
              AND s.listingStartDate <= :observationDate
              AND (
                    s.listingEndDate IS NULL
                    OR s.listingEndDate >= :observationDate
              )
              AND UPPER(s.securityType) = 'COMMON STOCK'
            ORDER BY s.symbol
            """)
    List<Stock> findCommonStocksListedAsOf(
            @Param("observationDate")
            LocalDate observationDate
    );

    List<Stock> findAllBySymbolAndExchange(
            String symbol,
            String exchange
    );
}