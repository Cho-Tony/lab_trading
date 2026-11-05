package com.tony.tradinglab.stock.repository;

import com.tony.tradinglab.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockRepository
        extends JpaRepository<Stock, Long> {

    /** Ticker-only lookup is valid only when it identifies exactly one security. */
    default Optional<Stock> findBySymbolAndExchange(String symbol, String exchange) {
        List<Stock> matches = findAllBySymbolAndExchange(symbol, exchange);
        if (matches.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(
                    "Ambiguous ticker/exchange: " + symbol + " / " + exchange
                            + "; use stockId or asOfDate with listing dates", 1, matches.size());
        }
        return matches.stream().findFirst();
    }

    boolean existsBySymbolAndExchange(
            String symbol,
            String exchange
    );

    /**
     * Listing dates are inclusive, consistently with findCommonStocksListedAsOf.
     * Missing starts and overlapping windows cannot establish a unique security.
     */
    @Query("""
            SELECT s
            FROM Stock s
            WHERE s.symbol = :symbol
              AND s.exchange = :exchange
              AND (s.listingStartDate IS NULL OR s.listingStartDate <= :asOfDate)
              AND (s.listingEndDate IS NULL OR s.listingEndDate >= :asOfDate)
            """)
    List<Stock> findListingCandidatesBySymbolAndExchangeAsOf(
            @Param("symbol") String symbol,
            @Param("exchange") String exchange,
            @Param("asOfDate") LocalDate asOfDate
    );

    default Optional<Stock> findListedBySymbolAndExchangeAsOf(
            String symbol, String exchange, LocalDate asOfDate
    ) {
        if (asOfDate == null) {
            throw new IllegalArgumentException("asOfDate is required");
        }
        List<Stock> matches = findListingCandidatesBySymbolAndExchangeAsOf(symbol, exchange, asOfDate);
        if (matches.stream().anyMatch(stock -> stock.getListingStartDate() == null)) {
            throw new IllegalStateException("Unknown listing start: " + symbol + " / " + exchange
                    + " / asOfDate=" + asOfDate + "; use stockId or reconcile listing dates");
        }
        if (matches.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(
                    "Overlapping listings: " + symbol + " / " + exchange + " / asOfDate=" + asOfDate,
                    1, matches.size());
        }
        return matches.stream().findFirst();
    }

    @Query("""
            SELECT s
            FROM Stock s
            WHERE s.symbol = :symbol
              AND s.exchange = :exchange
              AND (s.listingStartDate IS NULL OR s.listingStartDate <= :endDate)
              AND (s.listingEndDate IS NULL OR s.listingEndDate >= :startDate)
            """)
    List<Stock> findListingCandidatesBySymbolAndExchangeInRange(
            @Param("symbol") String symbol,
            @Param("exchange") String exchange,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /** A sync request must belong to one security for its entire date range. */
    default Optional<Stock> findListedBySymbolAndExchangeForRange(
            String symbol, String exchange, LocalDate startDate, LocalDate endDate
    ) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid listing date range");
        }
        List<Stock> matches = findListingCandidatesBySymbolAndExchangeInRange(
                symbol, exchange, startDate, endDate);
        if (matches.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(
                    "Multiple securities in date range: " + symbol + " / " + exchange
                            + "; split the request by stockId and listing window", 1, matches.size());
        }
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        Stock stock = matches.get(0);
        if (stock.getListingStartDate() == null || stock.getListingStartDate().isAfter(startDate)
                || (stock.getListingEndDate() != null && stock.getListingEndDate().isBefore(endDate))) {
            throw new IllegalStateException("Date range is not covered by a known listing window: "
                    + symbol + " / " + exchange + " / stockId=" + stock.getId());
        }
        return Optional.of(stock);
    }


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
