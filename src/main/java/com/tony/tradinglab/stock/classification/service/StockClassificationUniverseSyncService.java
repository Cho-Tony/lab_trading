package com.tony.tradinglab.stock.classification.service;

import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationEntity;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.service.StockMasterService;
import com.tony.tradinglab.universe.UniverseTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockClassificationUniverseSyncService {

    private final StockMasterService stockMasterService;

    private final StockClassificationRepository repository;

    private final PlatformTransactionManager transactionManager;


    public List<SyncResult> sync(
            List<UniverseTarget> targets,
            LocalDate effectiveFrom,
            ClassificationSource source
    ) {

        if (targets == null
                || targets.isEmpty()) {

            return List.of();
        }


        if (effectiveFrom == null) {

            throw new IllegalArgumentException(
                    "effectiveFrom은 null일 수 없습니다."
            );
        }


        if (source == null) {

            throw new IllegalArgumentException(
                    "source는 null일 수 없습니다."
            );
        }


        List<SyncResult> results =
                new ArrayList<>(
                        targets.size()
                );


        for (UniverseTarget target : targets) {

            if (target == null) {

                throw new IllegalArgumentException(
                        "target은 null일 수 없습니다."
                );
            }


            results.add(
                    syncTargetSafely(
                            target,
                            effectiveFrom,
                            source
                    )
            );
        }


        return List.copyOf(
                results
        );
    }


    private SyncResult syncTargetSafely(
            UniverseTarget target,
            LocalDate effectiveFrom,
            ClassificationSource source
    ) {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                        transactionManager
                );


        transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );


        try {

            SyncResult result =
                    transactionTemplate.execute(
                            status ->
                                    syncTarget(
                                            target,
                                            effectiveFrom,
                                            source
                                    )
                    );


            if (result == null) {

                throw new IllegalStateException(
                        "Classification sync result가 null입니다."
                );
            }


            return result;

        } catch (RuntimeException e) {

            log.error(
                    "Classification sync failed. symbol={}, exchange={}",
                    target.symbol(),
                    target.exchange(),
                    e
            );


            return new SyncResult(
                    null,
                    target.symbol(),
                    target.exchange(),
                    SyncStatus.FAILED,
                    e.getMessage()
            );
        }
    }


    private SyncResult syncTarget(
            UniverseTarget target,
            LocalDate effectiveFrom,
            ClassificationSource source
    ) {

        validateTarget(
                target
        );


        Stock stock =
                stockMasterService.ensureStock(
                        target.symbol(),
                        target.name(),
                        target.exchange(),
                        target.market(),
                        target.currency()
                );


        List<StockClassificationEntity> existing =
                repository
                        .findByStockIdOrderByEffectiveFromAsc(
                                stock.getId()
                        );


        StockClassificationEntity exactStart =
                existing.stream()
                        .filter(
                                classification ->
                                        effectiveFrom.equals(
                                                classification.getEffectiveFrom()
                                        )
                        )
                        .findFirst()
                        .orElse(null);


        if (exactStart != null) {

            if (sameClassification(
                    exactStart,
                    target
            )) {

                return successResult(
                        stock,
                        SyncStatus.UNCHANGED
                );
            }


            throw new IllegalStateException(
                    "동일한 effectiveFrom에 다른 classification이 존재합니다. "
                            + "symbol="
                            + stock.getSymbol()
                            + ", effectiveFrom="
                            + effectiveFrom
            );
        }


        StockClassificationEntity active =
                findActiveAsOf(
                        existing,
                        effectiveFrom
                );


        if (active != null
                && sameClassification(
                active,
                target
        )) {

            return successResult(
                    stock,
                    SyncStatus.UNCHANGED
            );
        }


        StockClassificationEntity next =
                findNext(
                        existing,
                        effectiveFrom
                );


        if (active != null) {

            active.closeAt(
                    effectiveFrom
            );


            repository.save(
                    active
            );
        }


        LocalDate effectiveTo =
                next == null
                        ? null
                        : next.getEffectiveFrom();


        StockClassificationEntity created =
                new StockClassificationEntity(
                        stock.getId(),
                        target.sector(),
                        target.industry(),
                        effectiveFrom,
                        effectiveTo,
                        source
                );


        repository.save(
                created
        );


        return successResult(
                stock,
                active == null
                        ? SyncStatus.INSERTED
                        : SyncStatus.CHANGED
        );
    }


    private SyncResult successResult(
            Stock stock,
            SyncStatus status
    ) {

        return new SyncResult(
                stock.getId(),
                stock.getSymbol(),
                stock.getExchange(),
                status,
                null
        );
    }


    private StockClassificationEntity findActiveAsOf(
            List<StockClassificationEntity> classifications,
            LocalDate asOfDate
    ) {

        return classifications.stream()
                .filter(
                        classification ->
                                classification.getEffectiveFrom() != null
                                        && !classification
                                        .getEffectiveFrom()
                                        .isAfter(asOfDate)
                )
                .filter(
                        classification ->
                                classification.getEffectiveTo() == null
                                        || asOfDate.isBefore(
                                        classification.getEffectiveTo()
                                )
                )
                .max(
                        Comparator.comparing(
                                StockClassificationEntity::getEffectiveFrom
                        )
                )
                .orElse(null);
    }


    private StockClassificationEntity findNext(
            List<StockClassificationEntity> classifications,
            LocalDate effectiveFrom
    ) {

        return classifications.stream()
                .filter(
                        classification ->
                                classification.getEffectiveFrom() != null
                                        && classification
                                        .getEffectiveFrom()
                                        .isAfter(effectiveFrom)
                )
                .min(
                        Comparator.comparing(
                                StockClassificationEntity::getEffectiveFrom
                        )
                )
                .orElse(null);
    }


    private boolean sameClassification(
            StockClassificationEntity classification,
            UniverseTarget target
    ) {

        return Objects.equals(
                classification.getSector(),
                target.sector()
        )
                && Objects.equals(
                classification.getIndustry(),
                target.industry()
        );
    }


    private void validateTarget(
            UniverseTarget target
    ) {

        if (target.sector() == null
                || target.sector().isBlank()) {

            throw new IllegalArgumentException(
                    "sector는 비어 있을 수 없습니다. symbol="
                            + target.symbol()
            );
        }


        if (target.industry() == null
                || target.industry().isBlank()) {

            throw new IllegalArgumentException(
                    "industry는 비어 있을 수 없습니다. symbol="
                            + target.symbol()
            );
        }
    }


    public enum SyncStatus {

        INSERTED,
        CHANGED,
        UNCHANGED,
        FAILED
    }


    public record SyncResult(

            Long stockId,

            String symbol,
            String exchange,

            SyncStatus status,

            String errorMessage

    ) {
    }
}