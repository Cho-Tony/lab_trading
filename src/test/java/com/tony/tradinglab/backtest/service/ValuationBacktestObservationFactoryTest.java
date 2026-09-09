package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.market.domain.MarketRegimeSnapshot;
import com.tony.tradinglab.market.service.MarketRegimeClassifier;
import com.tony.tradinglab.price.domain.StockPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValuationBacktestObservationFactoryTest {

    private ForwardReturnCalculator forwardReturnCalculator;
    private ExcessReturnCalculator excessReturnCalculator;
    private MarketRegimeClassifier marketRegimeClassifier;

    private ValuationBacktestObservationFactory factory;


    @BeforeEach
    void setUp() {

        forwardReturnCalculator =
                mock(
                        ForwardReturnCalculator.class
                );


        excessReturnCalculator =
                mock(
                        ExcessReturnCalculator.class
                );


        marketRegimeClassifier =
                mock(
                        MarketRegimeClassifier.class
                );


        factory =
                new ValuationBacktestObservationFactory(

                        forwardReturnCalculator,
                        excessReturnCalculator,
                        marketRegimeClassifier
                );
    }


    @Test
    void createBacktestObservation() {

        Long stockId =
                1L;

        String symbol =
                "CRDO";

        String benchmarkSymbol =
                "QQQ";


        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationComparisonResult valuationComparison =
                mock(
                        ValuationComparisonResult.class
                );


        StockPrice stockPrice =
                mock(
                        StockPrice.class
                );

        StockPrice benchmarkPrice =
                mock(
                        StockPrice.class
                );


        List<StockPrice> stockPrices =
                List.of(
                        stockPrice
                );

        List<StockPrice> benchmarkPrices =
                List.of(
                        benchmarkPrice
                );


        ForwardReturnMetrics stockReturns =
                mock(
                        ForwardReturnMetrics.class
                );


        ForwardReturnMetrics benchmarkReturns =
                mock(
                        ForwardReturnMetrics.class
                );


        ExcessReturnMetrics excessReturns =
                mock(
                        ExcessReturnMetrics.class
                );


        MarketRegimeSnapshot marketRegime =
                mock(
                        MarketRegimeSnapshot.class
                );


        when(
                forwardReturnCalculator.calculate(

                        observationDate,
                        stockPrices
                )
        )
                .thenReturn(
                        Optional.of(
                                stockReturns
                        )
                );


        when(
                forwardReturnCalculator.calculate(

                        observationDate,
                        benchmarkPrices
                )
        )
                .thenReturn(
                        Optional.of(
                                benchmarkReturns
                        )
                );


        when(
                excessReturnCalculator.calculate(

                        benchmarkSymbol,
                        stockReturns,
                        benchmarkReturns
                )
        )
                .thenReturn(
                        excessReturns
                );


        when(
                marketRegimeClassifier.classify(

                        benchmarkSymbol,
                        observationDate,
                        benchmarkPrices
                )
        )
                .thenReturn(
                        Optional.of(
                                marketRegime
                        )
                );


        ValuationBacktestObservationInput input =
                new ValuationBacktestObservationInput(

                        stockId,
                        symbol,

                        observationDate,

                        valuationComparison,

                        stockPrices,

                        benchmarkSymbol,
                        benchmarkPrices
                );


        ValuationBacktestObservation result =
                factory.create(
                                input
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
                result.observationDate()
        ).isEqualTo(
                observationDate
        );


        assertThat(
                result.valuationComparison()
        ).isSameAs(
                valuationComparison
        );


        assertThat(
                result.forwardReturns()
        ).isSameAs(
                stockReturns
        );


        assertThat(
                result.excessReturns()
        ).isSameAs(
                excessReturns
        );


        assertThat(
                result.marketRegime()
        ).isSameAs(
                marketRegime
        );
    }


    @Test
    void returnEmptyWhenStockForwardReturnCannotBeCalculated() {

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        List<StockPrice> stockPrices =
                List.of(
                        mock(StockPrice.class)
                );


        List<StockPrice> benchmarkPrices =
                List.of(
                        mock(StockPrice.class)
                );


        when(
                forwardReturnCalculator.calculate(

                        observationDate,
                        stockPrices
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        ValuationBacktestObservationInput input =
                new ValuationBacktestObservationInput(

                        1L,
                        "CRDO",

                        observationDate,

                        mock(
                                ValuationComparisonResult.class
                        ),

                        stockPrices,

                        "QQQ",
                        benchmarkPrices
                );


        assertThat(
                factory.create(
                        input
                )
        ).isEmpty();


        /*
         * 종목 미래수익률 자체를 계산할 수 없으면
         * benchmark / excess / regime 계산으로
         * 더 진행할 필요가 없다.
         */
        verifyNoInteractions(
                excessReturnCalculator,
                marketRegimeClassifier
        );
    }
}