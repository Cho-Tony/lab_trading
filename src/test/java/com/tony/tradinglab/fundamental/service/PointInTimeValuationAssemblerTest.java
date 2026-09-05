package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import com.tony.tradinglab.fundamental.domain.TtmFinancials;
import com.tony.tradinglab.fundamental.domain.ValuationMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PointInTimeValuationAssemblerTest {

    private final SharesOutstandingResolver
            sharesOutstandingResolver =
            new SharesOutstandingResolver();

    private final ValuationMetricsCalculator
            valuationMetricsCalculator =
            new ValuationMetricsCalculator();

    private final PointInTimeValuationAssembler assembler =
            new PointInTimeValuationAssembler(
                    sharesOutstandingResolver,
                    valuationMetricsCalculator
            );


    @Test
    void assembleUsingOnlyInformationAvailableAtPriceDate() {

        LocalDate priceDate =
                LocalDate.of(
                        2025,
                        11,
                        5
                );


        List<TtmFinancials> financials =
                List.of(

                        financials(
                                2025,
                                "Q2",
                                "90000000000",
                                "9000000000",
                                LocalDate.of(
                                        2025,
                                        7,
                                        30
                                )
                        ),

                        /*
                         * 11/5 당시 사용 가능
                         */
                        financials(
                                2025,
                                "Q3",
                                "100000000000",
                                "10000000000",
                                LocalDate.of(
                                        2025,
                                        10,
                                        30
                                )
                        ),

                        /*
                         * 미래 공시
                         * 절대 사용하면 안 됨
                         */
                        financials(
                                2025,
                                "Q4",
                                "120000000000",
                                "15000000000",
                                LocalDate.of(
                                        2026,
                                        1,
                                        30
                                )
                        )
                );


        List<TtmCashFlow> cashFlows =
                List.of(

                        new TtmCashFlow(
                                2025,
                                "Q3",

                                new BigDecimal(
                                        "15000000000"
                                ),

                                new BigDecimal(
                                        "5000000000"
                                ),

                                new BigDecimal(
                                        "10000000000"
                                ),

                                LocalDate.of(
                                        2025,
                                        10,
                                        30
                                )
                        )
                );


        List<SecFactPoint> sharesFacts =
                List.of(

                        sharesFact(
                                "1000000000",
                                LocalDate.of(
                                        2025,
                                        6,
                                        30
                                ),
                                LocalDate.of(
                                        2025,
                                        7,
                                        30
                                )
                        ),

                        sharesFact(
                                "950000000",
                                LocalDate.of(
                                        2025,
                                        9,
                                        30
                                ),
                                LocalDate.of(
                                        2025,
                                        10,
                                        30
                                )
                        ),

                        /*
                         * 미래에 공개
                         */
                        sharesFact(
                                "900000000",
                                LocalDate.of(
                                        2025,
                                        12,
                                        31
                                ),
                                LocalDate.of(
                                        2026,
                                        1,
                                        30
                                )
                        )
                );


        ValuationMetrics result =
                assembler.assemble(

                                priceDate,

                                new BigDecimal("100"),

                                financials,
                                cashFlows,
                                sharesFacts
                        )
                        .orElseThrow();


        System.out.println(
                "FY/Q = "
                        + result.fiscalYear()
                        + " "
                        + result.fiscalQuarter()
        );

        System.out.println(
                "Shares = "
                        + result.sharesOutstanding()
        );

        System.out.println(
                "Market Cap = "
                        + result.marketCap()
        );

        System.out.println(
                "P/E = "
                        + result.peRatio()
        );

        System.out.println(
                "P/S = "
                        + result.psRatio()
        );

        System.out.println(
                "P/FCF = "
                        + result.priceToFcfRatio()
        );


        /*
         * 미래 Q4가 아니라
         * 당시 공개된 Q3가 선택되어야 한다.
         */
        assertThat(
                result.fiscalQuarter()
        ).isEqualTo("Q3");


        /*
         * 950M shares
         */
        assertThat(
                result.sharesOutstanding()
        ).isEqualByComparingTo(
                "950000000"
        );


        /*
         * $100 × 950M
         * = $95B
         */
        assertThat(
                result.marketCap()
        ).isEqualByComparingTo(
                "95000000000"
        );


        /*
         * $95B / $10B
         * = 9.5
         */
        assertThat(
                result.peRatio()
        ).isEqualByComparingTo(
                "9.50"
        );


        /*
         * $95B / $100B
         * = 0.95
         */
        assertThat(
                result.psRatio()
        ).isEqualByComparingTo(
                "0.95"
        );


        /*
         * $95B / $10B
         * = 9.5
         */
        assertThat(
                result.priceToFcfRatio()
        ).isEqualByComparingTo(
                "9.50"
        );
    }


    private TtmFinancials financials(
            int fiscalYear,
            String fiscalQuarter,
            String revenue,
            String netIncome,
            LocalDate filedDate
    ) {

        return new TtmFinancials(

                fiscalYear,
                fiscalQuarter,

                new BigDecimal(revenue),

                new BigDecimal("10000000000"),

                new BigDecimal(netIncome),

                filedDate
        );
    }


    private SecFactPoint sharesFact(
            String shares,
            LocalDate measuredDate,
            LocalDate filedDate
    ) {

        return new SecFactPoint(

                "EntityCommonStockSharesOutstanding",

                new BigDecimal(shares),

                null,

                measuredDate,

                filedDate,

                2025,

                "FY",

                "10-Q",

                "TEST-ACC"
        );
    }
}