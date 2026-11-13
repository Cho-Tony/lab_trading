package com.tony.tradinglab.price.service;

import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.price.repository.StockPriceRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketPriceProviderTest {

    private static final LocalDate AS_OF = LocalDate.of(2021, 6, 30);
    private StockRepository stocks;
    private StockPriceRepository prices;
    private MarketPriceProvider provider;

    @BeforeEach
    void setUp() {
        stocks = mock(StockRepository.class);
        prices = mock(StockPriceRepository.class);
        provider = new MarketPriceProvider(stocks, prices);
    }

    @ParameterizedTest
    @CsvSource({"0,true", "7,true", "8,false", "-1,false"})
    void stockIdLookupPreservesSevenCalendarDayLimitAndRejectsFuturePrices(int age, boolean available) {
        StockPrice price = mock(StockPrice.class);
        when(price.getTradeDate()).thenReturn(AS_OF.minusDays(age));
        when(prices.findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(1L, AS_OF))
                .thenReturn(Optional.of(price));

        assertThat(provider.getAsOf(1L, AS_OF))
                .isEqualTo(available ? Optional.of(price) : Optional.empty());

        verify(prices).findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(1L, AS_OF);
        verifyNoMoreInteractions(prices);
        verifyNoInteractions(stocks);
    }

    @Test
    void missingStockIdPriceDoesNotResolveTickerOrWriteData() {
        assertThat(provider.getAsOf(1L, AS_OF)).isEmpty();

        verify(prices).findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(1L, AS_OF);
        verifyNoMoreInteractions(prices);
        verifyNoInteractions(stocks);
    }

    @Test
    void symbolApiStillNormalizesAndResolvesStockAtRequestedDate() {
        Stock stock = mock(Stock.class);
        when(stock.getId()).thenReturn(2L);
        when(stocks.findListedBySymbolAndExchangeAsOf("ABC", "NASDAQ", AS_OF))
                .thenReturn(Optional.of(stock));
        StockPrice price = new StockPrice(stock, AS_OF, null, null, null,
                BigDecimal.TEN, BigDecimal.ONE, 100L);
        when(prices.findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(2L, AS_OF))
                .thenReturn(Optional.of(price));

        assertThat(provider.getAsOf(" abc ", " nasdaq ", AS_OF)).contains(price);

        verify(stocks).findListedBySymbolAndExchangeAsOf("ABC", "NASDAQ", AS_OF);
        verify(prices).findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(2L, AS_OF);
        verifyNoMoreInteractions(stocks, prices);
    }

    @Test
    void symbolApiRetainsMissingStockException() {
        assertThatThrownBy(() -> provider.getAsOf("ABC", "NASDAQ", AS_OF))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock이 DB에 없습니다.");
        verifyNoInteractions(prices);
    }

    @Test
    void requiredArgumentsAreValidatedBeforeQuerying() {
        assertThatIllegalArgumentException().isThrownBy(() -> provider.getAsOf((Long) null, AS_OF));
        assertThatIllegalArgumentException().isThrownBy(() -> provider.getAsOf(1L, null));
        assertThatIllegalArgumentException().isThrownBy(() -> provider.getAsOf(null, "NASDAQ", AS_OF));
        assertThatIllegalArgumentException().isThrownBy(() -> provider.getAsOf("ABC", " ", AS_OF));
        assertThatIllegalArgumentException().isThrownBy(() -> provider.getAsOf("ABC", "NASDAQ", null));
        verifyNoInteractions(stocks, prices);
    }
}
