package com.tony.tradinglab.stock.classification.persistence;

import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class StockClassificationRepositoryTest {

    @Autowired
    private StockClassificationRepository repository;

    @PersistenceContext
    private EntityManager entityManager;


    @Test
    void saveAndFindClassificationHistory() {

        Long stockId =
                insertTestStock();


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


        repository.save(
                oldClassification
        );

        repository.save(
                currentClassification
        );


        /*
         * 실제 DB INSERT까지 강제로 수행.
         *
         * 테이블이 없거나
         * FK / 컬럼 / Entity 매핑에 문제가 있으면
         * 여기서 바로 실패한다.
         */
        repository.flush();


        List<StockClassificationEntity> result =
                repository
                        .findByStockIdOrderByEffectiveFromAsc(
                                stockId
                        );


        assertThat(result)
                .hasSize(2);


        assertThat(
                result.get(0)
                        .getIndustry()
        ).isEqualTo(
                "Semiconductors"
        );


        assertThat(
                result.get(1)
                        .getIndustry()
        ).isEqualTo(
                "Semiconductor Equipment"
        );


        assertThat(
                result.get(1)
                        .getEffectiveTo()
        ).isNull();
    }


    private Long insertTestStock() {

        /*
         * Stock 생성자 구조에 테스트가 의존하지 않도록
         * native SQL로 stocks row 하나를 만든다.
         *
         * @DataJpaTest는 기본적으로 transaction rollback을 하므로
         * 테스트 데이터는 끝나면 롤백된다.
         */

        entityManager
                .createNativeQuery(
                        """
                        INSERT INTO stocks (
                            symbol,
                            name,
                            exchange,
                            market,
                            currency,
                            active
                        )
                        VALUES (
                            'TEST_CLASSIFICATION',
                            'Test Classification Stock',
                            'NASDAQ',
                            'US',
                            'USD',
                            true
                        )
                        """
                )
                .executeUpdate();


        Number id =
                (Number) entityManager
                        .createNativeQuery(
                                "SELECT LAST_INSERT_ID()"
                        )
                        .getSingleResult();


        return id.longValue();
    }
}