package com.tony.tradinglab.stock.classification.service;

import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.domain.StockClassification;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationEntity;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockClassificationServiceTest {

    private StockClassificationRepository repository;

    private StockClassificationService service;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        StockClassificationRepository.class
                );


        StockClassificationMapper mapper =
                new StockClassificationMapper();


        StockClassificationResolver resolver =
                new StockClassificationResolver();


        service =
                new StockClassificationService(
                        repository,
                        mapper,
                        resolver
                );
    }


    @Test
    void findClassificationAvailableAtAsOfDate() {

        Long stockId =
                1L;


        StockClassificationEntity oldClassification =
                new StockClassificationEntity(

                        stockId,

                        "Technology",
                        "Semiconductors",

                        LocalDate.of(
                                2024,
                                1,
                                1
                        ),

                        LocalDate.of(
                                2025,
                                7,
                                1
                        ),

                        ClassificationSource.PROVIDER
                );


        StockClassificationEntity currentClassification =
                new StockClassificationEntity(

                        stockId,

                        "Technology",
                        "Semiconductor Equipment",

                        LocalDate.of(
                                2025,
                                7,
                                1
                        ),

                        null,

                        ClassificationSource.PROVIDER
                );


        when(
                repository
                        .findByStockIdOrderByEffectiveFromAsc(
                                stockId
                        )
        )
                .thenReturn(
                        List.of(
                                oldClassification,
                                currentClassification
                        )
                );


        StockClassification result =
                service.findAsOf(

                                stockId,

                                LocalDate.of(
                                        2025,
                                        6,
                                        30
                                )
                        )

                        .orElseThrow();


        assertThat(
                result.sector()
        ).isEqualTo(
                "Technology"
        );


        assertThat(
                result.industry()
        ).isEqualTo(
                "Semiconductors"
        );
    }


    @Test
    void useNewClassificationFromEffectiveDate() {

        Long stockId =
                1L;


        StockClassificationEntity oldClassification =
                new StockClassificationEntity(

                        stockId,

                        "Technology",
                        "Semiconductors",

                        LocalDate.of(
                                2024,
                                1,
                                1
                        ),

                        LocalDate.of(
                                2025,
                                7,
                                1
                        ),

                        ClassificationSource.PROVIDER
                );


        StockClassificationEntity currentClassification =
                new StockClassificationEntity(

                        stockId,

                        "Technology",
                        "Semiconductor Equipment",

                        LocalDate.of(
                                2025,
                                7,
                                1
                        ),

                        null,

                        ClassificationSource.PROVIDER
                );


        when(
                repository
                        .findByStockIdOrderByEffectiveFromAsc(
                                stockId
                        )
        )
                .thenReturn(
                        List.of(
                                oldClassification,
                                currentClassification
                        )
                );


        StockClassification result =
                service.findAsOf(

                                stockId,

                                LocalDate.of(
                                        2025,
                                        7,
                                        1
                                )
                        )

                        .orElseThrow();


        assertThat(
                result.industry()
        ).isEqualTo(
                "Semiconductor Equipment"
        );
    }


    @Test
    void returnEmptyWhenClassificationDoesNotExist() {

        Long stockId =
                1L;


        when(
                repository
                        .findByStockIdOrderByEffectiveFromAsc(
                                stockId
                        )
        )
                .thenReturn(
                        List.of()
                );


        assertThat(
                service.findAsOf(

                        stockId,

                        LocalDate.of(
                                2025,
                                6,
                                30
                        )
                )
        ).isEmpty();
    }
}