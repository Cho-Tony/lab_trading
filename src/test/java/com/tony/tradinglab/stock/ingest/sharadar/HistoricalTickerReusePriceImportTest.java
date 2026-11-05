package com.tony.tradinglab.stock.ingest.sharadar;

import com.tony.tradinglab.stock.domain.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HistoricalTickerReusePriceImportTest {

    private final SharadarStockPriceImportService importer = new SharadarStockPriceImportService(null, null);

    @Test
    void assignsEachPriceToTheStockListedOnItsTradeDate() {
        Map<String, List<Stock>> index = Map.of("ABC", List.of(
                stock(1L, "2000-01-01", "2010-12-31"),
                stock(2L, "2020-01-01", null)));

        assertThat(importer.findStockIdAsOf(index, "abc", LocalDate.parse("2010-12-31"))).isEqualTo(1L);
        assertThat(importer.findStockIdAsOf(index, "ABC", LocalDate.parse("2020-01-01"))).isEqualTo(2L);
        assertThat(importer.findStockIdAsOf(index, "ABC", LocalDate.parse("2025-01-01"))).isEqualTo(2L);
        assertThat(importer.findStockIdAsOf(index, "ABC", LocalDate.parse("2015-01-01"))).isNull();
        assertThat(importer.findStockIdAsOf(index, "OTHER", LocalDate.parse("2025-01-01"))).isNull();
    }

    @Test
    void overlappingListingWindowsCannotBeChosenFromTickerOnlyCsv() {
        Map<String, List<Stock>> index = Map.of("ABC", List.of(
                stock(1L, "2000-01-01", "2020-01-01"),
                stock(2L, "2020-01-01", null)));

        assertThatThrownBy(() -> importer.findStockIdAsOf(index, "ABC", LocalDate.parse("2020-01-01")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Ambiguous Sharadar ticker");
    }

    @Test
    void unknownListingStartRequiresReconciliation() {
        Map<String, List<Stock>> index = Map.of("ABC", List.of(stock(1L, null, null)));

        assertThatThrownBy(() -> importer.findStockIdAsOf(index, "ABC", LocalDate.parse("2025-01-01")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Unknown listing start");
    }

    private Stock stock(Long id, String start, String end) {
        Stock stock = new Stock("ABC", "Stock " + id, "NASDAQ", "US", "USD");
        ReflectionTestUtils.setField(stock, "id", id);
        stock.applyHistoricalMasterData(stock.getName(), "US", "USD", end == null,
                "SHARADAR", id.toString(), "Common Stock",
                start == null ? null : LocalDate.parse(start),
                end == null ? null : LocalDate.parse(end), null);
        return stock;
    }
}
