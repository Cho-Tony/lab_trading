package com.tony.tradinglab.stock.repository;

import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.ingest.HistoricalStockMasterImportService;
import com.tony.tradinglab.stock.ingest.HistoricalStockMasterRecord;
import com.tony.tradinglab.fundamental.persistence.FinancialStatementEntity;
import com.tony.tradinglab.fundamental.persistence.FinancialStatementRepository;
import com.tony.tradinglab.fundamental.service.FinancialStatementQueryService;
import com.tony.tradinglab.fundamental.service.FundamentalDataProvider;
import com.tony.tradinglab.fundamental.service.FundamentalDataSyncService;
import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.price.repository.StockPriceRepository;
import com.tony.tradinglab.price.service.MarketPriceProvider;
import com.tony.tradinglab.price.service.MarketDataSyncService;
import com.tony.tradinglab.marketdata.client.MarketDataClient;
import com.tony.tradinglab.marketdata.dto.DailyPrice;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:historical-ticker-reuse;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:db/migration/V1__create_stocks_table.sql,"
                + "classpath:db/migration/V2__create_stock_prices.sql,"
                + "classpath:db/migration/V4__create_financial_statements.sql,"
                + "classpath:db/historical-stock-metadata-h2.sql,"
                + "classpath:db/migration/V6__allow_historical_ticker_reuse.sql"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = HistoricalTickerReuseRepositoryTest.JpaConfiguration.class)
class HistoricalTickerReuseRepositoryTest {

    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = {Stock.class, StockPrice.class, FinancialStatementEntity.class})
    @EnableJpaRepositories(basePackageClasses = {
            StockRepository.class, StockPriceRepository.class, FinancialStatementRepository.class})
    static class JpaConfiguration {
    }

    @Autowired
    private StockRepository stocks;

    @Autowired
    private StockPriceRepository prices;

    @Autowired
    private FinancialStatementRepository statements;

    @Test
    void separatesReusedTickerByListingWindowAndStockId() {
        Stock oldStock = save("OLD", "2000-01-01", "2010-12-31");
        Stock newStock = save("NEW", "2020-01-01", null);
        stocks.flush();

        assertThat(stocks.findAllBySymbolAndExchange("ABC", "NASDAQ")).hasSize(2);
        assertThat(stocks.findById(oldStock.getId())).get()
                .extracting(Stock::getSourceSecurityId).isEqualTo("OLD");
        assertThat(stocks.findById(newStock.getId())).get()
                .extracting(Stock::getSourceSecurityId).isEqualTo("NEW");
        assertStockAsOf("2000-01-01", oldStock);
        assertStockAsOf("2010-12-31", oldStock);
        assertStockAsOf("2020-01-01", newStock);
        assertStockAsOf("2026-01-01", newStock);
        assertThat(stocks.findListedBySymbolAndExchangeAsOf(
                "ABC", "NASDAQ", LocalDate.parse("2015-01-01"))).isEmpty();
        assertThat(stocks.findListedBySymbolAndExchangeAsOf(
                "ABC", "NYSE", LocalDate.parse("2026-01-01"))).isEmpty();
    }

    @Test
    void rejectsOverlappingListingsInsteadOfChoosingOne() {
        save("OLD", "2000-01-01", "2020-01-01");
        save("NEW", "2020-01-01", null);

        assertThatThrownBy(() -> stocks.findListedBySymbolAndExchangeAsOf(
                "ABC", "NASDAQ", LocalDate.parse("2020-01-01")))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void tickerOnlyLookupRequiresMoreIdentityWhenTickerWasReused() {
        save("OLD", "2000-01-01", "2010-12-31");
        save("NEW", "2020-01-01", null);

        assertThatThrownBy(() -> stocks.findBySymbolAndExchange("ABC", "NASDAQ"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessageContaining("stockId or asOfDate");
    }

    @Test
    void unknownListingStartCannotHideAnAmbiguousSecurity() {
        stocks.save(new Stock("ABC", "Unknown", "NASDAQ", "US", "USD"));
        save("NEW", "2020-01-01", null);

        assertThatThrownBy(() -> stocks.findListedBySymbolAndExchangeAsOf(
                "ABC", "NASDAQ", LocalDate.parse("2025-01-01")))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("Unknown listing start");
    }

    @Test
    void priceAndFinancialProvidersReadTheSecurityListedOnTheObservationDate() {
        Stock oldStock = save("OLD", "2000-01-01", "2010-12-31");
        Stock newStock = save("NEW", "2020-01-01", null);
        LocalDate oldDate = LocalDate.parse("2010-12-30");
        LocalDate newDate = LocalDate.parse("2025-06-30");
        prices.save(price(oldStock, oldDate, "10"));
        prices.save(price(newStock, newDate, "100"));
        statements.save(statement(oldStock, oldDate, "20"));
        statements.save(statement(newStock, newDate, "200"));
        stocks.flush();

        MarketPriceProvider priceProvider = new MarketPriceProvider(stocks, prices);
        FinancialStatementQueryService query = new FinancialStatementQueryService(stocks, statements);
        FundamentalDataSyncService sync = mock(FundamentalDataSyncService.class);
        FundamentalDataProvider fundamentals = new FundamentalDataProvider(query, sync);

        assertThat(priceProvider.getAsOf(" abc ", " nasdaq ", oldDate)).get()
                .extracting(value -> value.getStock().getId()).isEqualTo(oldStock.getId());
        assertThat(priceProvider.getAsOf("ABC", "NASDAQ", newDate)).get()
                .extracting(value -> value.getStock().getId()).isEqualTo(newStock.getId());
        assertThat(fundamentals.getAsOf("ABC", "NASDAQ", oldDate)).singleElement()
                .satisfies(value -> assertThat(value.revenue()).isEqualByComparingTo("20"));
        assertThat(fundamentals.getAsOf("ABC", "NASDAQ", newDate)).singleElement()
                .satisfies(value -> assertThat(value.revenue()).isEqualByComparingTo("200"));
        // A permanent identity also permits explicit access after delisting.
        assertThat(query.findAsOf(oldStock.getId(), newDate)).singleElement()
                .satisfies(value -> assertThat(value.revenue()).isEqualByComparingTo("20"));
        assertThat(query.findAll(oldStock.getId())).hasSize(1);
        verifyNoInteractions(sync);
    }

    @Test
    void priceSyncKeepsReusedTickerDataSeparateAndRejectsCrossListingRanges() {
        Stock oldStock = save("OLD", "2000-01-01", "2010-12-31");
        Stock newStock = save("NEW", "2020-01-01", null);
        MarketDataClient client = mock(MarketDataClient.class);
        MarketDataSyncService sync = new MarketDataSyncService(client, stocks, prices);
        LocalDate oldDate = LocalDate.parse("2010-12-30");
        LocalDate newDate = LocalDate.parse("2025-06-30");
        when(client.getDailyPrices("ABC", oldDate, oldDate.plusDays(1)))
                .thenReturn(List.of(dailyPrice(oldDate, "10")));
        when(client.getDailyPrices("ABC", newDate, newDate.plusDays(1)))
                .thenReturn(List.of(dailyPrice(newDate, "100")));

        assertThat(sync.sync("ABC", "NASDAQ", oldDate, oldDate)).isEqualTo(1);
        assertThat(sync.syncRange("ABC", "NASDAQ", newDate, newDate)).isEqualTo(1);
        assertThat(prices.findByStockIdAndTradeDate(oldStock.getId(), oldDate)).isPresent();
        assertThat(prices.findByStockIdAndTradeDate(newStock.getId(), newDate)).isPresent();
        assertThat(prices.findByStockIdAndTradeDate(newStock.getId(), oldDate)).isEmpty();
        assertThat(prices.findByStockIdAndTradeDate(oldStock.getId(), newDate)).isEmpty();

        MarketDataClient unusedClient = mock(MarketDataClient.class);
        MarketDataSyncService unsafeSync = new MarketDataSyncService(unusedClient, stocks, prices);
        assertThatThrownBy(() -> unsafeSync.syncRange("ABC", "NASDAQ", oldDate, newDate))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
        assertThatThrownBy(() -> unsafeSync.sync("ABC", "NASDAQ", oldDate,
                LocalDate.parse("2011-01-01")))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("not covered");
        verifyNoInteractions(unusedClient);
    }

    @Test
    void entityIndexesMatchV5AndV6WithoutTickerUniqueness() {
        Table table = Stock.class.getAnnotation(Table.class);
        assertThat(table.uniqueConstraints()).isEmpty();
        assertThat(Arrays.stream(table.indexes()).map(index -> index.name()).toList())
                .containsExactlyInAnyOrder("idx_stocks_symbol_exchange",
                        "idx_stocks_listing_window", "idx_stocks_source_security");
        assertThat(Arrays.stream(table.indexes()).allMatch(index -> !index.unique())).isTrue();
    }

    @Test
    void importingReusedTickerPreservesBothPermanentIdentitiesAndIsRepeatable() {
        List<HistoricalStockMasterRecord> records = List.of(
                record("OLD", "2000-01-01", "2010-12-31"),
                record("NEW", "2020-01-01", null));
        HistoricalStockMasterImportService importer =
                new HistoricalStockMasterImportService(() -> records, stocks);

        HistoricalStockMasterImportService.ImportResult first = importer.importAll();
        assertThat(first.insertedCount()).isEqualTo(2);
        assertThat(first.updatedCount()).isZero();
        assertThat(first.conflictCount()).isZero();
        stocks.flush();

        Stock oldStock = stocks.findAllByDataSourceAndSourceSecurityId("SHARADAR", "OLD").get(0);
        Stock newStock = stocks.findAllByDataSourceAndSourceSecurityId("SHARADAR", "NEW").get(0);
        assertThat(oldStock.getId()).isNotEqualTo(newStock.getId());
        assertStockAsOf("2005-01-01", oldStock);
        assertStockAsOf("2025-01-01", newStock);

        HistoricalStockMasterImportService.ImportResult second = importer.importAll();
        assertThat(second.insertedCount()).isZero();
        assertThat(second.updatedCount()).isEqualTo(2);
        assertThat(stocks.findAllBySymbolAndExchange("ABC", "NASDAQ")).hasSize(2);
        assertThat(oldStock.getSourceSecurityId()).isEqualTo("OLD");
        assertThat(newStock.getSourceSecurityId()).isEqualTo("NEW");
    }

    @Test
    void doesNotAttachPermanentIdToAnUnidentifiedTickerMatch() {
        Stock unidentified = stocks.save(new Stock("ABC", "Manual", "NASDAQ", "US", "USD"));
        HistoricalStockMasterImportService importer = new HistoricalStockMasterImportService(
                () -> List.of(record("NEW", "2020-01-01", null)), stocks);

        HistoricalStockMasterImportService.ImportResult result = importer.importAll();

        assertThat(result.conflictCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isZero();
        assertThat(result.insertedCount()).isZero();
        assertThat(unidentified.getSourceSecurityId()).isNull();
        assertThat(unidentified.getName()).isEqualTo("Manual");
    }

    private HistoricalStockMasterRecord record(String sourceId, String start, String end) {
        return new HistoricalStockMasterRecord("SHARADAR", sourceId, "ABC", sourceId,
                "NASDAQ", "US", "USD", "Common Stock", LocalDate.parse(start),
                end == null ? null : LocalDate.parse(end), null, null, null, end == null);
    }

    private StockPrice price(Stock stock, LocalDate date, String close) {
        BigDecimal value = new BigDecimal(close);
        return new StockPrice(stock, date, value, value, value, value, value, 100L);
    }

    private DailyPrice dailyPrice(LocalDate date, String close) {
        BigDecimal value = new BigDecimal(close);
        return new DailyPrice(date, value, value, value, value, value, 100L);
    }

    private FinancialStatementEntity statement(Stock stock, LocalDate filedDate, String revenue) {
        return new FinancialStatementEntity(stock, filedDate.minusMonths(3), filedDate,
                filedDate.getYear(), "Q1", new BigDecimal(revenue),
                null, null, null, null, null, null, null, null, null);
    }

    private void assertStockAsOf(String date, Stock expected) {
        assertThat(stocks.findListedBySymbolAndExchangeAsOf(
                "ABC", "NASDAQ", LocalDate.parse(date))).get()
                .extracting(Stock::getId).isEqualTo(expected.getId());
    }

    private Stock save(String sourceId, String start, String end) {
        Stock stock = new Stock("ABC", sourceId, "NASDAQ", "US", "USD");
        stock.applyHistoricalMasterData(sourceId, "US", "USD", end == null,
                "SHARADAR", sourceId, "Common Stock", LocalDate.parse(start),
                end == null ? null : LocalDate.parse(end), null);
        return stocks.save(stock);
    }
}
