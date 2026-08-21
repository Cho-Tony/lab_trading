package com.tony.tradinglab.marketdata.client;

import com.tony.tradinglab.marketdata.dto.DailyPrice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TwelveDataClientTest {

    @Autowired
    private TwelveDataClient twelveDataClient;

    @Test
    void getDailyPrices() {

        List<DailyPrice> prices =
                twelveDataClient.getDailyPrices(
                        "AAPL",
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 10)
                );

        assertThat(prices).isNotEmpty();

        DailyPrice first = prices.get(0);

        assertThat(first.tradeDate()).isNotNull();
        assertThat(first.open()).isNotNull();
        assertThat(first.high()).isNotNull();
        assertThat(first.low()).isNotNull();
        assertThat(first.close()).isNotNull();
        assertThat(first.volume()).isNotNull();

        prices.forEach(System.out::println);
    }
}