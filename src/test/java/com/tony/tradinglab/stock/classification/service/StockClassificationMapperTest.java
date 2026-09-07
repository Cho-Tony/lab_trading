package com.tony.tradinglab.stock.classification.service;

import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.domain.StockClassification;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class StockClassificationMapperTest {

    private final StockClassificationMapper mapper =
            new StockClassificationMapper();


    @Test
    void convertEntityToDomain() {

        StockClassificationEntity entity =
                new StockClassificationEntity(

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
                );


        StockClassification result =
                mapper.toDomain(
                        entity
                );


        assertThat(
                result.stockId()
        ).isEqualTo(1L);


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


        assertThat(
                result.effectiveFrom()
        ).isEqualTo(
                LocalDate.of(
                        2025,
                        1,
                        1
                )
        );


        assertThat(
                result.effectiveTo()
        ).isNull();


        assertThat(
                result.source()
        ).isEqualTo(
                ClassificationSource.PROVIDER
        );
    }
}