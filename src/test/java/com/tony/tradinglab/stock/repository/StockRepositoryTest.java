package com.tony.tradinglab.stock.repository;

import com.tony.tradinglab.stock.domain.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class StockRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @Test
    void saveAndFindStock() {
        Stock stock = new Stock(
                "AAPL",
                "Apple Inc.",
                "NASDAQ",
                "US",
                "USD"
        );

        stockRepository.save(stock);

        Stock found = stockRepository
                .findBySymbolAndExchange("AAPL", "NASDAQ")
                .orElseThrow();

        assertThat(found.getSymbol()).isEqualTo("AAPL");
        assertThat(found.getName()).isEqualTo("Apple Inc.");
        assertThat(found.getCurrency()).isEqualTo("USD");
    }
}
