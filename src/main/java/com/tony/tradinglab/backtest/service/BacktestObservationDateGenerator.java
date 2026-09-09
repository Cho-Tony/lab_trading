package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.BacktestObservationFrequency;
import com.tony.tradinglab.price.domain.StockPrice;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class BacktestObservationDateGenerator {

    public List<LocalDate> generate(
            LocalDate startDate,
            LocalDate endDate,
            BacktestObservationFrequency frequency,
            List<StockPrice> benchmarkPrices
    ) {

        if (startDate == null
                || endDate == null
                || frequency == null
                || benchmarkPrices == null
                || startDate.isAfter(endDate)) {

            return List.of();
        }


        List<LocalDate> tradingDates =
                benchmarkPrices.stream()

                        .filter(
                                price ->
                                        price != null
                                                && price.getTradeDate() != null
                        )

                        .map(
                                StockPrice::getTradeDate
                        )

                        .filter(
                                date ->
                                        !date.isBefore(startDate)
                                                && !date.isAfter(endDate)
                        )

                        .distinct()

                        .sorted()

                        .toList();


        if (tradingDates.isEmpty()) {

            return List.of();
        }


        List<LocalDate> result =
                new ArrayList<>();


        LocalDate scheduledDate =
                firstScheduledDate(
                        startDate,
                        frequency
                );


        while (!scheduledDate.isAfter(endDate)) {

            findLastTradingDateOnOrBefore(
                    scheduledDate,
                    startDate,
                    tradingDates
            )
                    .ifPresent(
                            result::add
                    );


            scheduledDate =
                    nextScheduledDate(
                            scheduledDate,
                            frequency
                    );
        }


        return result.stream()
                .distinct()
                .sorted()
                .toList();
    }


    private LocalDate firstScheduledDate(
            LocalDate startDate,
            BacktestObservationFrequency frequency
    ) {

        if (frequency
                == BacktestObservationFrequency.MONTHLY) {

            return YearMonth
                    .from(startDate)
                    .atEndOfMonth();
        }


        int month =
                startDate.getMonthValue();


        int quarterEndMonth =
                ((month - 1) / 3 + 1) * 3;


        return YearMonth.of(
                        startDate.getYear(),
                        quarterEndMonth
                )
                .atEndOfMonth();
    }


    private LocalDate nextScheduledDate(
            LocalDate currentScheduledDate,
            BacktestObservationFrequency frequency
    ) {

        int monthsToAdd =
                frequency
                        == BacktestObservationFrequency.MONTHLY
                        ? 1
                        : 3;


        return YearMonth
                .from(
                        currentScheduledDate
                )
                .plusMonths(
                        monthsToAdd
                )
                .atEndOfMonth();
    }


    private Optional<LocalDate> findLastTradingDateOnOrBefore(
            LocalDate scheduledDate,
            LocalDate startDate,
            List<LocalDate> tradingDates
    ) {

        return tradingDates.stream()

                /*
                 * scheduledDate 이후 가격은 절대 사용하지 않는다.
                 */
                .filter(
                        date ->
                                !date.isAfter(
                                        scheduledDate
                                )
                )

                /*
                 * 백테스트 시작일 이전 날짜도 사용하지 않는다.
                 */
                .filter(
                        date ->
                                !date.isBefore(
                                        startDate
                                )
                )

                .max(
                        Comparator.naturalOrder()
                );
    }
}