package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PointInTimeFundamentalContext;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PointInTimeFundamentalContextFactoryTest {

    private PointInTimeQuarterlyFinancialsSelector selector;

    private PointInTimeFundamentalContextFactory factory;


    @BeforeEach
    void setUp() {

        selector =
                mock(
                        PointInTimeQuarterlyFinancialsSelector.class
                );


        factory =
                new PointInTimeFundamentalContextFactory(
                        selector
                );
    }


    @Test
    void createContextUsingOnlyAvailableFinancials() {

        Long stockId =
                1L;

        String symbol =
                "CRDO";

        LocalDate asOfDate =
                LocalDate.of(
                        2025,
                        7,
                        15
                );


        QuarterlyFinancials q4 =
                mock(
                        QuarterlyFinancials.class
                );

        QuarterlyFinancials q1 =
                mock(
                        QuarterlyFinancials.class
                );


        List<QuarterlyFinancials> allFinancials =
                List.of(
                        q4,
                        q1
                );


        List<QuarterlyFinancials> availableFinancials =
                List.of(
                        q4,
                        q1
                );


        when(
                selector.select(
                        allFinancials,
                        asOfDate
                )
        )
                .thenReturn(
                        availableFinancials
                );


        PointInTimeFundamentalContext result =
                factory.create(

                                stockId,
                                symbol,

                                asOfDate,

                                allFinancials
                        )

                        .orElseThrow();


        assertThat(
                result.stockId()
        ).isEqualTo(
                stockId
        );


        assertThat(
                result.symbol()
        ).isEqualTo(
                symbol
        );


        assertThat(
                result.asOfDate()
        ).isEqualTo(
                asOfDate
        );


        assertThat(
                result.quarterlyFinancials()
        )
                .containsExactly(
                        q4,
                        q1
                );


        verify(
                selector
        )
                .select(
                        allFinancials,
                        asOfDate
                );
    }


    @Test
    void returnEmptyWhenNoFinancialsWereAvailableAtThatTime() {

        LocalDate asOfDate =
                LocalDate.of(
                        2020,
                        1,
                        1
                );


        List<QuarterlyFinancials> financials =
                List.of(
                        mock(
                                QuarterlyFinancials.class
                        )
                );


        when(
                selector.select(
                        financials,
                        asOfDate
                )
        )
                .thenReturn(
                        List.of()
                );


        assertThat(
                factory.create(

                        1L,
                        "CRDO",

                        asOfDate,

                        financials
                )
        ).isEmpty();
    }
}