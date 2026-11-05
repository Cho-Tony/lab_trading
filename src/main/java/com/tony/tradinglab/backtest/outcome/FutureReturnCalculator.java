package com.tony.tradinglab.backtest.outcome;

import com.tony.tradinglab.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FutureReturnCalculator {

    private static final int PRICE_LOOKAHEAD_DAYS =
            7;

    private static final int RETURN_SCALE =
            16;


    private static final String FIND_FIRST_PRICE_SQL = """
            SELECT
                trade_date,
                adjusted_close
            FROM sharadar_stock_prices
            WHERE stock_id = ?
              AND trade_date >= ?
              AND trade_date <= ?
              AND adjusted_close IS NOT NULL
              AND adjusted_close > 0
            ORDER BY trade_date
            LIMIT 1
            """;


    private final JdbcTemplate jdbcTemplate;

    private final TerminalOutcomeResolver terminalOutcomeResolver;


    public Optional<FutureReturnResult> calculate(
            Stock stock,
            LocalDate observationDate,
            int horizonMonths
    ) {

        validate(
                stock,
                observationDate,
                horizonMonths
        );


        LocalDate targetDate =
                observationDate.plusMonths(
                        horizonMonths
                );


        Optional<PricePoint> entryPrice =
                findFirstPriceOnOrAfter(
                        stock.getId(),
                        observationDate
                );


        /*
         * Observation 시점에 실제 매수 가능한 가격이 없으면
         * 이 sample의 future return을 만들 수 없다.
         *
         * 이후 completeness validation에서
         * missing sample로 집계할 수 있도록 empty 반환.
         */
        if (entryPrice.isEmpty()) {

            return Optional.empty();
        }


        PricePoint entry =
                entryPrice.get();


        LocalDate listingEndDate =
                stock.getListingEndDate();


        boolean terminalBeforeTarget =
                listingEndDate != null
                        && listingEndDate.isBefore(
                        targetDate
                );


        /*
         * 목표 horizon 전에 상장 종료.
         *
         * 여기서는 아직 terminal wealth를 계산하지 않는다.
         *
         * acquisition / bankruptcy / spinoff 등의
         * 수익률 정책은 다음 단계에서 별도로 적용한다.
         */
        if (terminalBeforeTarget) {

            TerminalOutcomeType terminalOutcomeType =
                    terminalOutcomeResolver.resolve(
                            stock
                    );


            return Optional.of(
                    new FutureReturnResult(
                            stock.getId(),
                            observationDate,
                            targetDate,
                            entry.tradeDate(),
                            null,
                            entry.adjustedClose(),
                            null,
                            null,
                            true,
                            terminalOutcomeType
                    )
            );
        }


        /*
         * target date가 주말/휴일일 수 있으므로
         * target 당일 또는 그 이후 최대 7일까지
         * 첫 거래 가격을 사용한다.
         *
         * 과거 가격을 무기한 끌어오는 as-of 방식은
         * 사용하지 않는다.
         */
        Optional<PricePoint> exitPrice =
                findFirstPriceOnOrAfter(
                        stock.getId(),
                        targetDate
                );


        if (exitPrice.isEmpty()) {

            return Optional.empty();
        }


        PricePoint exit =
                exitPrice.get();


        BigDecimal returnRate =
                calculateReturnRate(
                        entry.adjustedClose(),
                        exit.adjustedClose()
                );


        return Optional.of(
                new FutureReturnResult(
                        stock.getId(),
                        observationDate,
                        targetDate,
                        entry.tradeDate(),
                        exit.tradeDate(),
                        entry.adjustedClose(),
                        exit.adjustedClose(),
                        returnRate,
                        false,
                        null
                )
        );
    }


    private Optional<PricePoint> findFirstPriceOnOrAfter(
            Long stockId,
            LocalDate date
    ) {

        LocalDate maxDate =
                date.plusDays(
                        PRICE_LOOKAHEAD_DAYS
                );


        List<PricePoint> prices =
                jdbcTemplate.query(
                        FIND_FIRST_PRICE_SQL,
                        (rs, rowNum) ->
                                new PricePoint(
                                        rs.getObject(
                                                "trade_date",
                                                LocalDate.class
                                        ),
                                        rs.getBigDecimal(
                                                "adjusted_close"
                                        )
                                ),
                        stockId,
                        date,
                        maxDate
                );


        if (prices.isEmpty()) {

            return Optional.empty();
        }


        return Optional.of(
                prices.get(0)
        );
    }


    private BigDecimal calculateReturnRate(
            BigDecimal entryPrice,
            BigDecimal exitPrice
    ) {

        if (entryPrice == null
                || entryPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "entryPrice must be positive."
            );
        }


        if (exitPrice == null
                || exitPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "exitPrice must be positive."
            );
        }


        return exitPrice
                .divide(
                        entryPrice,
                        RETURN_SCALE,
                        RoundingMode.HALF_UP
                )
                .subtract(
                        BigDecimal.ONE
                );
    }


    private void validate(
            Stock stock,
            LocalDate observationDate,
            int horizonMonths
    ) {

        if (stock == null) {

            throw new IllegalArgumentException(
                    "stock must not be null."
            );
        }


        if (stock.getId() == null) {

            throw new IllegalArgumentException(
                    "stock.id must not be null."
            );
        }


        if (observationDate == null) {

            throw new IllegalArgumentException(
                    "observationDate must not be null."
            );
        }


        if (horizonMonths <= 0) {

            throw new IllegalArgumentException(
                    "horizonMonths must be positive."
            );
        }
    }


    private record PricePoint(
            LocalDate tradeDate,
            BigDecimal adjustedClose
    ) {
    }
}