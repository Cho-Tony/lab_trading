package com.tony.tradinglab.stock.classification.service;

import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.domain.StockClassification;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockClassificationResolverTest {

    private final StockClassificationResolver resolver =
            new StockClassificationResolver();


    @Test
    void resolveClassificationAsOfDate() {

        List<StockClassification> classifications =
                List.of(

                        new StockClassification(

                                1L,

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
                        ),


                        new StockClassification(

                                1L,

                                "Technology",
                                "Semiconductor Equipment",

                                LocalDate.of(
                                        2025,
                                        7,
                                        1
                                ),

                                null,

                                ClassificationSource.PROVIDER
                        )
                );


        StockClassification oldClassification =
                resolver.resolve(

                                1L,

                                LocalDate.of(
                                        2025,
                                        6,
                                        30
                                ),

                                classifications
                        )
                        .orElseThrow();


        assertThat(
                oldClassification.industry()
        ).isEqualTo(
                "Semiconductors"
        );


        StockClassification newClassification =
                resolver.resolve(

                                1L,

                                LocalDate.of(
                                        2025,
                                        7,
                                        1
                                ),

                                classifications
                        )
                        .orElseThrow();


        assertThat(
                newClassification.industry()
        ).isEqualTo(
                "Semiconductor Equipment"
        );
    }


    @Test
    void returnEmptyWhenNoClassificationWasAvailableYet() {

        List<StockClassification> classifications =
                List.of(

                        new StockClassification(

                                1L,

                                "Technology",
                                "Semiconductors",

                                LocalDate.of(
                                        2025,
                                        1,
                                        1
                                ),

                                null,

                                ClassificationSource.PROVIDER
                        )
                );


        assertThat(
                resolver.resolve(

                        1L,

                        LocalDate.of(
                                2024,
                                12,
                                31
                        ),

                        classifications
                )
        ).isEmpty();
    }
}