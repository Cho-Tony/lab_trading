package com.tony.tradinglab.stock.classification.service;

import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationEntity;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.ingest.HistoricalStockMasterRecord;
import com.tony.tradinglab.stock.ingest.HistoricalStockMasterSource;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoricalStockClassificationImportService {

    /*
     * 현재 stock_classifications 구조는
     * effective_from을 필수로 요구한다.
     *
     * 하지만 Sharadar sector / industry는 이번 백테스트에서
     * historical PIT taxonomy가 아니라
     * "static peer taxonomy"로 사용한다.
     *
     * 따라서 실제 분류 변경일이라는 의미가 아니라
     * static taxonomy용 sentinel date다.
     */
    private static final LocalDate STATIC_EFFECTIVE_FROM =
            LocalDate.of(1900, 1, 1);


    private final HistoricalStockMasterSource stockMasterSource;

    private final StockRepository stockRepository;

    private final StockClassificationRepository
            classificationRepository;


    @Transactional
    public ImportResult importAll() {

        List<HistoricalStockMasterRecord> records =
                stockMasterSource.loadAll();


        Map<String, Stock> stocksBySourceKey =
                buildStockIndex();


        Map<Long, StockClassificationEntity>
                staticClassificationByStockId =
                buildStaticClassificationIndex();


        int inserted = 0;
        int updated = 0;
        int unchanged = 0;
        int skipped = 0;
        int stockMissing = 0;


        for (
                HistoricalStockMasterRecord record
                : records
        ) {

            if (!hasText(record.sector())
                    || !hasText(record.industry())) {

                skipped++;

                continue;
            }


            String sourceKey =
                    createSourceKey(
                            record.dataSource(),
                            record.sourceSecurityId()
                    );


            Stock stock =
                    stocksBySourceKey.get(
                            sourceKey
                    );


            if (stock == null) {

                stockMissing++;

                continue;
            }


            StockClassificationEntity existing =
                    staticClassificationByStockId.get(
                            stock.getId()
                    );


            if (existing == null) {

                StockClassificationEntity created =
                        new StockClassificationEntity(
                                stock.getId(),
                                record.sector(),
                                record.industry(),
                                STATIC_EFFECTIVE_FROM,
                                null,
                                ClassificationSource.PROVIDER
                        );


                classificationRepository.save(
                        created
                );


                staticClassificationByStockId.put(
                        stock.getId(),
                        created
                );


                inserted++;

                continue;
            }


            if (sameClassification(
                    existing,
                    record
            )) {

                unchanged++;

                continue;
            }


            existing.updateClassification(
                    record.sector(),
                    record.industry(),
                    ClassificationSource.PROVIDER
            );


            updated++;
        }


        return new ImportResult(
                records.size(),
                inserted,
                updated,
                unchanged,
                skipped,
                stockMissing
        );
    }


    private Map<String, Stock> buildStockIndex() {

        Map<String, Stock> result =
                new HashMap<>();


        for (
                Stock stock
                : stockRepository.findAll()
        ) {

            if (!hasText(stock.getDataSource())
                    || !hasText(
                    stock.getSourceSecurityId()
            )) {

                continue;
            }


            String key =
                    createSourceKey(
                            stock.getDataSource(),
                            stock.getSourceSecurityId()
                    );


            Stock previous =
                    result.put(
                            key,
                            stock
                    );


            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate stock source identity: "
                                + key
                );
            }
        }


        return result;
    }


    private Map<Long, StockClassificationEntity>
    buildStaticClassificationIndex() {

        Map<Long, StockClassificationEntity> result =
                new HashMap<>();


        for (
                StockClassificationEntity entity
                : classificationRepository.findAll()
        ) {

            if (!STATIC_EFFECTIVE_FROM.equals(
                    entity.getEffectiveFrom()
            )) {

                continue;
            }


            StockClassificationEntity previous =
                    result.put(
                            entity.getStockId(),
                            entity
                    );


            if (previous != null) {

                throw new IllegalStateException(
                        "Duplicate static classification. stockId="
                                + entity.getStockId()
                );
            }
        }


        return result;
    }


    private boolean sameClassification(
            StockClassificationEntity existing,
            HistoricalStockMasterRecord record
    ) {

        return existing.getSector()
                .equals(record.sector())

                && existing.getIndustry()
                .equals(record.industry())

                && existing.getSource()
                == ClassificationSource.PROVIDER;
    }


    private String createSourceKey(
            String dataSource,
            String sourceSecurityId
    ) {

        return dataSource
                + "|"
                + sourceSecurityId;
    }


    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.isBlank();
    }


    public record ImportResult(
            int sourceRecordCount,
            int insertedCount,
            int updatedCount,
            int unchangedCount,
            int skippedCount,
            int stockMissingCount
    ) {
    }
}