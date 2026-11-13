package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.price.repository.StockPriceRepository;
import com.tony.tradinglab.price.service.MarketPriceProvider;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Real factory/provider with an in-memory price fixture behind mocked repositories. */
class QuantBacktestSampleFactoryTest {

    private static final LocalDate AS_OF = LocalDate.of(2021, 8, 31);
    private static final QuantScoreBreakdown SCORE = new QuantScoreBreakdown(
            BigDecimal.valueOf(60), BigDecimal.valueOf(70), BigDecimal.valueOf(80), true, BigDecimal.valueOf(69));
    private StockRepository stocks;
    private StockPriceRepository prices;
    private MarketPriceProvider provider;
    private QuantBacktestSampleFactory factory;
    private final List<StockPrice> stored = new ArrayList<>();
    private Stock stockA;
    private Stock stockB;

    @BeforeEach
    void setUp() {
        stocks = mock(StockRepository.class);
        prices = mock(StockPriceRepository.class);
        provider = new MarketPriceProvider(stocks, prices);
        factory = new QuantBacktestSampleFactory(provider);
        stockA = stock(1L, "ABC");
        stockB = stock(2L, "ABC");
        when(prices.findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(anyLong(), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    LocalDate date = invocation.getArgument(1);
                    return stored.stream()
                            .filter(price -> price.getStock().getId().equals(id))
                            .filter(price -> !price.getTradeDate().isAfter(date))
                            .max(Comparator.comparing(StockPrice::getTradeDate));
                });
    }

    @Test
    void reusedTickerKeepsOriginalStockIdForEntryAndAllThreeHorizons() {
        addPrice(stockA, AS_OF.minusDays(1), "100");
        addPrice(stockA, AS_OF.plusMonths(3).minusDays(1), "110");
        addPrice(stockA, AS_OF.plusMonths(6).minusDays(2), "90");
        addPrice(stockA, AS_OF.plusMonths(12), "125");
        addReplacementPrices();

        QuantBacktestSample sample = factory.create(candidate(1L), "NASDAQ").orElseThrow();

        assertThat(stockA.getSymbol()).isEqualTo(stockB.getSymbol());
        assertThat(stockA.getId()).isNotEqualTo(stockB.getId());
        assertThat(sample.stockId()).isEqualTo(1L);
        assertThat(sample.symbol()).isEqualTo("ABC");
        assertThat(sample.observationDate()).isEqualTo(AS_OF);
        assertThat(sample.score()).isSameAs(SCORE);
        assertThat(sample.entryPriceDate()).isEqualTo(AS_OF.minusDays(1));
        assertThat(sample.entryPrice()).isEqualByComparingTo("100");
        assertThat(sample.threeMonthPriceDate()).isEqualTo(AS_OF.plusMonths(3).minusDays(1));
        assertThat(sample.threeMonthPrice()).isEqualByComparingTo("110");
        assertThat(sample.threeMonthReturnPct()).isEqualTo(new BigDecimal("10.00"));
        assertThat(sample.sixMonthPriceDate()).isEqualTo(AS_OF.plusMonths(6).minusDays(2));
        assertThat(sample.sixMonthPrice()).isEqualByComparingTo("90");
        assertThat(sample.sixMonthReturnPct()).isEqualTo(new BigDecimal("-10.00"));
        assertThat(sample.twelveMonthPriceDate()).isEqualTo(AS_OF.plusMonths(12));
        assertThat(sample.twelveMonthPrice()).isEqualByComparingTo("125");
        assertThat(sample.twelveMonthReturnPct()).isEqualTo(new BigDecimal("25.00"));
        verifyOnlyCandidatePriceQueries();
    }

    @Test
    void delistedStockWithOnlyReplacementFuturePricesKeepsNullHorizons() {
        stockA.applyHistoricalMasterData(null, null, null, false, "TEST", "A", "STOCK",
                AS_OF.minusYears(5), AS_OF.plusDays(1), "delisted");
        stockB.applyHistoricalMasterData(null, null, null, true, "TEST", "B", "STOCK",
                AS_OF.plusDays(2), null, null);
        addPrice(stockA, AS_OF, "100");
        addReplacementPrices();

        // B's fresh prices really are available, but must never fill A's missing horizons.
        for (int months : new int[]{3, 6, 12}) {
            assertThat(provider.getAsOf(2L, AS_OF.plusMonths(months))).isPresent();
        }
        clearInvocations(prices);

        QuantBacktestSample sample = factory.create(candidate(1L), "NASDAQ").orElseThrow();

        assertThat(sample.stockId()).isEqualTo(1L);
        assertThat(sample.entryPrice()).isEqualByComparingTo("100");
        assertAllHorizonsMissing(sample);
        verifyOnlyCandidatePriceQueries();
    }

    @Test
    void missingSingleHorizonDoesNotDiscardOtherReturnsOrSubstituteReplacementPrice() {
        addPrice(stockA, AS_OF, "100");
        addPrice(stockA, AS_OF.plusMonths(3), "110");
        addPrice(stockA, AS_OF.plusMonths(12), "125");
        addReplacementPrices();

        QuantBacktestSample sample = factory.create(candidate(1L), "NASDAQ").orElseThrow();

        assertThat(sample.threeMonthReturnPct()).isEqualTo(new BigDecimal("10.00"));
        assertThat(sample.sixMonthPriceDate()).isNull();
        assertThat(sample.sixMonthPrice()).isNull();
        assertThat(sample.sixMonthReturnPct()).isNull();
        assertThat(sample.twelveMonthReturnPct()).isEqualTo(new BigDecimal("25.00"));
        verifyOnlyCandidatePriceQueries();
    }

    @Test
    void uniqueTickerMatchesExistingSymbolApiAndPreservesRawCloseRoundingAndMonthEndDates() {
        Stock unique = stock(3L, "UNIQUE");
        addPrice(unique, AS_OF.minusDays(7), "3");
        addPrice(unique, AS_OF.plusMonths(3), "4");
        addPrice(unique, AS_OF.plusMonths(6), "2");
        addPrice(unique, AS_OF.plusMonths(12), "5");
        when(stocks.findListedBySymbolAndExchangeAsOf(eq("UNIQUE"), eq("NASDAQ"), any(LocalDate.class)))
                .thenReturn(Optional.of(unique));

        StockPrice legacyEntry = provider.getAsOf("UNIQUE", "NASDAQ", AS_OF).orElseThrow();
        StockPrice legacyThree = provider.getAsOf("UNIQUE", "NASDAQ", AS_OF.plusMonths(3)).orElseThrow();
        StockPrice legacySix = provider.getAsOf("UNIQUE", "NASDAQ", AS_OF.plusMonths(6)).orElseThrow();
        StockPrice legacyTwelve = provider.getAsOf("UNIQUE", "NASDAQ", AS_OF.plusMonths(12)).orElseThrow();
        clearInvocations(stocks, prices);

        QuantBacktestSample sample = factory.create(
                new QuantDiscoveryCandidate(3L, "UNIQUE", AS_OF, SCORE), "NASDAQ").orElseThrow();

        assertThat(sample.entryPrice()).isEqualTo(legacyEntry.getClose());
        assertThat(sample.entryPriceDate()).isEqualTo(legacyEntry.getTradeDate());
        assertThat(sample.threeMonthPrice()).isEqualTo(legacyThree.getClose());
        assertThat(sample.sixMonthPrice()).isEqualTo(legacySix.getClose());
        assertThat(sample.twelveMonthPrice()).isEqualTo(legacyTwelve.getClose());
        assertThat(sample.threeMonthPriceDate()).isEqualTo(LocalDate.of(2021, 11, 30));
        assertThat(sample.sixMonthPriceDate()).isEqualTo(LocalDate.of(2022, 2, 28));
        assertThat(sample.twelveMonthPriceDate()).isEqualTo(LocalDate.of(2022, 8, 31));
        assertThat(sample.threeMonthReturnPct()).isEqualTo(new BigDecimal("33.33"));
        assertThat(sample.sixMonthReturnPct()).isEqualTo(new BigDecimal("-33.33"));
        assertThat(sample.twelveMonthReturnPct()).isEqualTo(new BigDecimal("66.67"));
        verifyNoInteractions(stocks);
    }

    @Test
    void missingCandidateEntryDoesNotUseReplacementOrQueryFutureHorizons() {
        addPrice(stockB, AS_OF, "900");
        addReplacementPrices();

        assertThat(factory.create(candidate(1L), "NASDAQ")).isEmpty();

        verify(prices).findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(1L, AS_OF);
        verifyNoMoreInteractions(prices);
        verifyNoInteractions(stocks);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-1"})
    void invalidCandidateEntryCloseStillRejectsSample(String close) {
        addPrice(stockA, AS_OF, close);

        assertThat(factory.create(candidate(1L), "NASDAQ")).isEmpty();

        verify(prices).findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(1L, AS_OF);
        verifyNoMoreInteractions(prices);
        verifyNoInteractions(stocks);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-1"})
    void invalidHorizonClosesKeepNullResultsWithoutReplacement(String close) {
        addPrice(stockA, AS_OF, "100");
        for (int months : new int[]{3, 6, 12}) {
            addPrice(stockA, AS_OF.plusMonths(months), close);
        }
        addReplacementPrices();

        QuantBacktestSample sample = factory.create(candidate(1L), "NASDAQ").orElseThrow();

        assertAllHorizonsMissing(sample);
        verifyOnlyCandidatePriceQueries();
    }

    @Test
    void candidateWithoutStockIdIsRejectedWithoutTickerFallback() {
        assertThat(factory.create(candidate(null), "NASDAQ")).isEmpty();
        verifyNoInteractions(stocks, prices);
    }

    private void verifyOnlyCandidatePriceQueries() {
        for (int months : new int[]{0, 3, 6, 12}) {
            verify(prices).findTopByStockIdAndTradeDateLessThanEqualOrderByTradeDateDesc(1L, AS_OF.plusMonths(months));
        }
        verifyNoMoreInteractions(prices);
        verifyNoInteractions(stocks);
    }

    private void assertAllHorizonsMissing(QuantBacktestSample sample) {
        assertThat(sample.threeMonthPriceDate()).isNull();
        assertThat(sample.threeMonthPrice()).isNull();
        assertThat(sample.threeMonthReturnPct()).isNull();
        assertThat(sample.sixMonthPriceDate()).isNull();
        assertThat(sample.sixMonthPrice()).isNull();
        assertThat(sample.sixMonthReturnPct()).isNull();
        assertThat(sample.twelveMonthPriceDate()).isNull();
        assertThat(sample.twelveMonthPrice()).isNull();
        assertThat(sample.twelveMonthReturnPct()).isNull();
    }

    private QuantDiscoveryCandidate candidate(Long id) {
        return new QuantDiscoveryCandidate(id, "ABC", AS_OF, SCORE);
    }

    private Stock stock(Long id, String symbol) {
        Stock stock = new Stock(symbol, "Security " + id, "NASDAQ", "US", "USD");
        ReflectionTestUtils.setField(stock, "id", id);
        return stock;
    }

    private void addPrice(Stock stock, LocalDate date, String close) {
        BigDecimal rawClose = close == null ? null : new BigDecimal(close);
        // Deliberately different: returns must continue using raw close, not adjusted close.
        stored.add(new StockPrice(stock, date, rawClose, rawClose, rawClose, rawClose,
                BigDecimal.ONE, 100L));
    }

    private void addReplacementPrices() {
        for (int months : new int[]{3, 6, 12}) {
            addPrice(stockB, AS_OF.plusMonths(months), "900");
        }
    }
}
