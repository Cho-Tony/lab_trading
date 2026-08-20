package com.tony.tradinglab.price.repository;

import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class StockPriceRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockPriceRepository stockPriceRepository;

    @Test
    void saveAndFindPrices() {

        Stock stock = new Stock(
                "AAPL",
                "Apple Inc.",
                "NASDAQ",
                "US",
                "USD"
        );

        stockRepository.save(stock);

        StockPrice price1 = new StockPrice(
                stock,
                LocalDate.of(2026, 8, 19),
                new BigDecimal("230.00"),
                new BigDecimal("235.00"),
                new BigDecimal("228.00"),
                new BigDecimal("233.00"),
                new BigDecimal("233.00"),
                50_000_000L
        );

        StockPrice price2 = new StockPrice(
                stock,
                LocalDate.of(2026, 8, 20),
                new BigDecimal("233.00"),
                new BigDecimal("238.00"),
                new BigDecimal("231.00"),
                new BigDecimal("237.00"),
                new BigDecimal("237.00"),
                55_000_000L
        );

        stockPriceRepository.save(price1);
        stockPriceRepository.save(price2);

        List<StockPrice> prices =
                stockPriceRepository
                        .findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(
                                stock.getId(),
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 8, 31)
                        );

        assertThat(prices).hasSize(2);

        assertThat(prices.get(0).getTradeDate())
                .isEqualTo(LocalDate.of(2026, 8, 19));

        assertThat(prices.get(1).getTradeDate())
                .isEqualTo(LocalDate.of(2026, 8, 20));

        assertThat(prices.get(1).getClose())
                .isEqualByComparingTo("237.00");
    }
}