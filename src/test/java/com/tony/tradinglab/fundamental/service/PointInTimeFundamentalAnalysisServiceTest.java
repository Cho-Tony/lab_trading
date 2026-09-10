package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PointInTimeFundamentalAnalysisServiceTest {

    private PointInTimeTtmFinancialsAnalyzer ttmAnalyzer;
    private PointInTimeGrowthAnalyzer growthAnalyzer;
    private PointInTimeProfitabilityAnalyzer profitabilityAnalyzer;

    private PointInTimeFundamentalAnalysisService service;


    @BeforeEach
    void setUp() {

        ttmAnalyzer =
                mock(
                        PointInTimeTtmFinancialsAnalyzer.class
                );


        growthAnalyzer =
                mock(
                        PointInTimeGrowthAnalyzer.class
                );


        profitabilityAnalyzer =
                mock(
                        PointInTimeProfitabilityAnalyzer.class
                );


        service =
                new PointInTimeFundamentalAnalysisService(

                        ttmAnalyzer,
                        growthAnalyzer,
                        profitabilityAnalyzer
                );
    }


    @Test
    void analyzePointInTimeFundamentals() {

        LocalDate asOfDate =
                LocalDate.of(
                        2025,
                        7,
                        15
                );


        PointInTimeFundamentalContext context =
                new PointInTimeFundamentalContext(

                        1L,
                        "CRDO",

                        asOfDate,

                        List.of(
                                mock(
                                        QuarterlyFinancials.class
                                )
                        )
                );


        List<QuarterlyFact> revenueFacts =
                List.of(
                        mock(
                                QuarterlyFact.class
                        )
                );


        TtmFinancials ttm =
                mock(
                        TtmFinancials.class
                );


        GrowthTrendAnalysis growth =
                mock(
                        GrowthTrendAnalysis.class
                );


        ProfitabilityTrendAnalysis profitability =
                mock(
                        ProfitabilityTrendAnalysis.class
                );


        when(
                ttmAnalyzer.analyze(
                        context
                )
        )
                .thenReturn(
                        Optional.of(
                                ttm
                        )
                );


        when(
                growthAnalyzer.analyze(

                        revenueFacts,
                        asOfDate
                )
        )
                .thenReturn(
                        Optional.of(
                                growth
                        )
                );


        when(
                profitabilityAnalyzer.analyze(
                        context
                )
        )
                .thenReturn(
                        Optional.of(
                                profitability
                        )
                );


        PointInTimeFundamentalAnalysis result =
                service.analyze(

                                context,
                                revenueFacts
                        )

                        .orElseThrow();


        assertThat(
                result.stockId()
        ).isEqualTo(
                1L
        );


        assertThat(
                result.symbol()
        ).isEqualTo(
                "CRDO"
        );


        assertThat(
                result.asOfDate()
        ).isEqualTo(
                asOfDate
        );


        assertThat(
                result.ttmFinancials()
        ).isSameAs(
                ttm
        );


        assertThat(
                result.growth()
        ).isSameAs(
                growth
        );


        assertThat(
                result.profitability()
        ).isSameAs(
                profitability
        );
    }


    @Test
    void returnEmptyWhenTtmIsUnavailable() {

        PointInTimeFundamentalContext context =
                new PointInTimeFundamentalContext(

                        1L,
                        "CRDO",

                        LocalDate.of(
                                2025,
                                7,
                                15
                        ),

                        List.of(
                                mock(
                                        QuarterlyFinancials.class
                                )
                        )
                );


        when(
                ttmAnalyzer.analyze(
                        context
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        assertThat(
                service.analyze(

                        context,

                        List.of(
                                mock(
                                        QuarterlyFact.class
                                )
                        )
                )
        ).isEmpty();


        /*
         * TTM도 못 만들면 이후 계산은
         * 할 필요가 없다.
         */
        verifyNoInteractions(
                growthAnalyzer,
                profitabilityAnalyzer
        );
    }


    @Test
    void allowMissingGrowthAndProfitability() {

        LocalDate asOfDate =
                LocalDate.of(
                        2025,
                        7,
                        15
                );


        PointInTimeFundamentalContext context =
                new PointInTimeFundamentalContext(

                        1L,
                        "CRDO",

                        asOfDate,

                        List.of(
                                mock(
                                        QuarterlyFinancials.class
                                )
                        )
                );


        List<QuarterlyFact> revenueFacts =
                List.of();


        TtmFinancials ttm =
                mock(
                        TtmFinancials.class
                );


        when(
                ttmAnalyzer.analyze(
                        context
                )
        )
                .thenReturn(
                        Optional.of(
                                ttm
                        )
                );


        when(
                growthAnalyzer.analyze(

                        revenueFacts,
                        asOfDate
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        when(
                profitabilityAnalyzer.analyze(
                        context
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        PointInTimeFundamentalAnalysis result =
                service.analyze(

                                context,
                                revenueFacts
                        )

                        .orElseThrow();


        assertThat(
                result.ttmFinancials()
        ).isSameAs(
                ttm
        );


        assertThat(
                result.growth()
        ).isNull();


        assertThat(
                result.profitability()
        ).isNull();
    }
}