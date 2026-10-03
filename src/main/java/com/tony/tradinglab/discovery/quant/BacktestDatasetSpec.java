package com.tony.tradinglab.discovery.quant;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class BacktestDatasetSpec {

    /*
     * Score를 실제로 평가할 기간.
     *
     * 2010 Q1 ~ 2025 Q2
     */
    public static final LocalDate OBSERVATION_START =
            LocalDate.of(
                    2010,
                    3,
                    31
            );

    public static final LocalDate OBSERVATION_END =
            LocalDate.of(
                    2025,
                    6,
                    30
            );


    /*
     * 첫 observation에서 TTM / YoY / Growth를
     * 계산할 수 있도록 충분한 이전 fundamental 확보.
     */
    public static final LocalDate FUNDAMENTAL_START =
            LocalDate.of(
                    2008,
                    1,
                    1
            );

    public static final LocalDate FUNDAMENTAL_END =
            OBSERVATION_END;


    /*
     * 가격은 observation 이전 여유 구간 +
     * 마지막 observation의 12M forward return까지 필요.
     */
    public static final LocalDate PRICE_START =
            LocalDate.of(
                    2009,
                    1,
                    1
            );

    public static final LocalDate PRICE_END =
            LocalDate.of(
                    2026,
                    6,
                    30
            );


    private BacktestDatasetSpec() {
    }


    public static List<LocalDate> observationDates() {

        List<LocalDate> dates =
                new ArrayList<>();


        LocalDate date =
                OBSERVATION_START;


        while (!date.isAfter(
                OBSERVATION_END
        )) {

            dates.add(
                    date
            );


            date =
                    nextQuarterEnd(
                            date
                    );
        }


        return List.copyOf(
                dates
        );
    }


    private static LocalDate nextQuarterEnd(
            LocalDate current
    ) {

        return switch (current.getMonthValue()) {

            case 3 ->
                    LocalDate.of(
                            current.getYear(),
                            6,
                            30
                    );

            case 6 ->
                    LocalDate.of(
                            current.getYear(),
                            9,
                            30
                    );

            case 9 ->
                    LocalDate.of(
                            current.getYear(),
                            12,
                            31
                    );

            case 12 ->
                    LocalDate.of(
                            current.getYear() + 1,
                            3,
                            31
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Not a quarter-end date: "
                                    + current
                    );
        };
    }
}