package com.tony.tradinglab.price.repository;

import com.tony.tradinglab.price.domain.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    Optional<StockPrice> findByStockIdAndTradeDate(
            Long stockId,
            LocalDate tradeDate
    );

    List<StockPrice> findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(
            Long stockId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<StockPrice> findTopByStockIdOrderByTradeDateDesc(
            Long stockId
    );
}
