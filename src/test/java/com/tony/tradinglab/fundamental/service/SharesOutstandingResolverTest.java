package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.domain.SharesOutstandingSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SharesOutstandingResolverTest {

    private final SharesOutstandingResolver resolver =
            new SharesOutstandingResolver();


    @Test
    void resolveLatestAvailableSharesAsOfDate() {

        List<SecFactPoint> facts =
                List.of(

                        fact(
                                "1000000000",
                                "2025-06-30",
                                "2025-07-30"
                        ),

                        fact(
                                "950000000",
                                "2025-09-30",
                                "2025-10-30"
                        ),

                        fact(
                                "900000000",
                                "2025-12-31",
                                "2026-01-30"
                        )
                );


        Optional<SharesOutstandingSnapshot> result =
                resolver.resolve(
                        facts,
                        LocalDate.of(
                                2025,
                                11,
                                5
                        )
                );


        assertThat(result)
                .isPresent();


        SharesOutstandingSnapshot shares =
                result.orElseThrow();


        System.out.println(
                "Shares = "
                        + shares.sharesOutstanding()
        );

        System.out.println(
                "Measured = "
                        + shares.measuredDate()
        );

        System.out.println(
                "Filed = "
                        + shares.filedDate()
        );


        assertThat(
                shares.sharesOutstanding()
        ).isEqualByComparingTo(
                "950000000"
        );


        assertThat(
                shares.measuredDate()
        ).isEqualTo(
                LocalDate.of(
                        2025,
                        9,
                        30
                )
        );
    }


    @Test
    void mustNotUseFactFiledAfterBacktestDate() {

        List<SecFactPoint> facts =
                List.of(

                        fact(
                                "1000000000",
                                "2025-06-30",
                                "2025-07-30"
                        ),

                        fact(
                                "950000000",
                                "2025-09-30",
                                "2025-10-30"
                        )
                );


        SharesOutstandingSnapshot result =
                resolver.resolve(

                                facts,

                                LocalDate.of(
                                        2025,
                                        10,
                                        10
                                )
                        )
                        .orElseThrow();


        /*
         * 9월 30일 측정값이 존재하더라도
         * filing이 10월 30일이므로
         * 10월 10일에는 사용 불가능.
         */

        assertThat(
                result.sharesOutstanding()
        ).isEqualByComparingTo(
                "1000000000"
        );
    }


    private SecFactPoint fact(
            String value,
            String endDate,
            String filedDate
    ) {

        return new SecFactPoint(

                "EntityCommonStockSharesOutstanding",

                new BigDecimal(value),

                null,

                LocalDate.parse(endDate),

                LocalDate.parse(filedDate),

                2025,

                "FY",

                "10-Q",

                "TEST-ACC"
        );
    }
}