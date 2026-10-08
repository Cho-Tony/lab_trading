package com.tony.tradinglab.stock.ingest.sharadar;

import com.tony.tradinglab.stock.ingest.HistoricalStockMasterRecord;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class SharadarTickerMapper {

    private static final String DATA_SOURCE =
            "SHARADAR";

    private static final String MARKET =
            "US";

    private static final String SECURITY_TYPE =
            "COMMON STOCK";


    public Optional<HistoricalStockMasterRecord> map(
            SharadarTickerRecord source
    ) {

        if (source == null) {
            return Optional.empty();
        }

        if (!isFundamentalsRecord(
                source.table()
        )) {
            return Optional.empty();
        }

        String sourceSecurityId =
                normalize(
                        source.permaticker()
                );

        String symbol =
                normalizeUpper(
                        source.ticker()
                );

        String exchange =
                normalizeUpper(
                        source.exchange()
                );


        if (sourceSecurityId == null
                || symbol == null
                || exchange == null) {

            return Optional.empty();
        }


        String name =
                normalize(
                        source.name()
                );

        if (name == null) {
            name = symbol;
        }


        String currency =
                normalizeUpper(
                        source.currency()
                );

        /*
         * Sharadar stock prices are US-listed stock data.
         * Our stocks.currency is NOT NULL,
         * so use USD as a defensive fallback.
         */
        if (currency == null) {
            currency = "USD";
        }


        return Optional.of(
                new HistoricalStockMasterRecord(
                        DATA_SOURCE,
                        sourceSecurityId,

                        symbol,
                        name,

                        exchange,
                        MARKET,
                        currency,

                        SECURITY_TYPE,

                        source.firstPriceDate(),

                        source.delisted()
                                ? source.lastPriceDate()
                                : null,

                        null,

                        normalize(
                                source.sector()
                        ),

                        normalize(
                                source.industry()
                        ),

                        !source.delisted()
                )
        );
    }


    private boolean isFundamentalsRecord(
            String table
    ) {

        if (table == null
                || table.isBlank()) {

            return false;
        }

        String normalized =
                table.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        /*
         * Sharadar legacy alias = SF1
         * Current table name = fundamentals
         *
         * 실제 bulk sample을 받은 뒤
         * 사용되는 값을 확인해서 필요하면 하나로 좁힌다.
         */
        return "SF1".equals(normalized)
                || "FUNDAMENTALS".equals(normalized);
    }


    private String normalize(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        return normalized;
    }


    private String normalizeUpper(
            String value
    ) {

        String normalized =
                normalize(
                        value
                );

        if (normalized == null) {
            return null;
        }

        return normalized.toUpperCase(
                Locale.ROOT
        );
    }
}