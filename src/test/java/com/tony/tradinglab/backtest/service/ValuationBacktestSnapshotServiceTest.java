package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.*;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationSnapshotAnalysisReport;
import com.tony.tradinglab.price.domain.StockPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValuationBacktestSnapshotServiceTest {

    private ValuationBacktestObservationFactory observationFactory;

    private ValuationBacktestSnapshotService service;


    @BeforeEach
    void setUp() {

        observationFactory =
                mock(
                        ValuationBacktestObservationFactory.class
                );


        service =
                new ValuationBacktestSnapshotService(
                        observationFactory
                );
    }


    @Test
    void createBacktestObservationsForSnapshot() {

        LocalDate observationDate =
                LocalDate.of(
                        2025,
                        6,
                        30
                );


        ValuationComparisonResult result1 =
                mock(
                        ValuationComparisonResult.class
                );

        ValuationComparisonResult result2 =
                mock(
                        ValuationComparisonResult.class
                );

        ValuationComparisonResult result3 =
                mock(
                        ValuationComparisonResult.class
                );


        when(
                result1.stockId()
        ).thenReturn(
                1L
        );

        when(
                result1.symbol()
        ).thenReturn(
                "AAA"
        );


        when(
                result2.stockId()
        ).thenReturn(
                2L
        );

        when(
                result2.symbol()
        ).thenReturn(
                "BBB"
        );


        when(
                result3.stockId()
        ).thenReturn(
                3L
        );

        when(
                result3.symbol()
        ).thenReturn(
                "CCC"
        );


        ValuationSnapshotAnalysisReport valuationReport =
                new ValuationSnapshotAnalysisReport(

                        observationDate,

                        3,
                        3,

                        List.of(
                                result1,
                                result2,
                                result3
                        )
                );


        List<StockPrice> prices1 =
                List.of(
                        mock(StockPrice.class)
                );

        List<StockPrice> prices2 =
                List.of(
                        mock(StockPrice.class)
                );


        /*
         * stockId = 3 가격은 일부러 없음.
         */
        Map<Long, List<StockPrice>> stockPrices =
                Map.of(

                        1L,
                        prices1,

                        2L,
                        prices2
                );


        List<StockPrice> benchmarkPrices =
                List.of(
                        mock(StockPrice.class)
                );


        ValuationBacktestObservation observation1 =
                mock(
                        ValuationBacktestObservation.class
                );

        ValuationBacktestObservation observation2 =
                mock(
                        ValuationBacktestObservation.class
                );


        /*
         * record는 equals()가 값 기반이라
         * Mockito eq()로 input 전체를 비교할 수도 있지만,
         * 테스트가 너무 생성자 구조에 묶이지 않도록
         * any()를 사용한다.
         */
        when(
                observationFactory.create(
                        any(
                                ValuationBacktestObservationInput.class
                        )
                )
        )
                .thenReturn(
                        Optional.of(
                                observation1
                        ),

                        Optional.of(
                                observation2
                        )
                );


        ValuationBacktestSnapshotReport report =
                service.create(

                        valuationReport,

                        stockPrices,

                        "QQQ",

                        benchmarkPrices
                );


        assertThat(
                report.observationDate()
        ).isEqualTo(
                observationDate
        );


        assertThat(
                report.valuationResultCount()
        ).isEqualTo(
                3
        );


        /*
         * CCC는 가격 데이터가 없어서 제외.
         */
        assertThat(
                report.observationCount()
        ).isEqualTo(
                2
        );


        assertThat(
                report.observations()
        )
                .containsExactly(
                        observation1,
                        observation2
                );


        verify(
                observationFactory,
                times(2)
        )
                .create(
                        any(
                                ValuationBacktestObservationInput.class
                        )
                );
    }
}