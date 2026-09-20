package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.marketdata.service.MarketPriceUniverseSyncService;
import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.service.StockClassificationUniverseSyncService;
import com.tony.tradinglab.universe.UniverseTarget;
import com.tony.tradinglab.universe.ValuationUniverse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValuationUniverseSyncService {

    private final FundamentalUniverseSyncService
            fundamentalUniverseSyncService;

    private final StockClassificationUniverseSyncService
            classificationUniverseSyncService;

    private final MarketPriceUniverseSyncService
            marketPriceUniverseSyncService;


    public SyncResult sync(
            LocalDate classificationEffectiveFrom,
            ClassificationSource classificationSource,
            LocalDate priceStartDate,
            LocalDate priceEndDate
    ) {

        validateDates(
                classificationEffectiveFrom,
                priceStartDate,
                priceEndDate
        );


        if (classificationSource == null) {

            throw new IllegalArgumentException(
                    "classificationSource는 null일 수 없습니다."
            );
        }


        List<UniverseTarget> targets =
                ValuationUniverse.targets();


        List<FundamentalUniverseSyncService.SyncResult>
                fundamentalResults =
                fundamentalUniverseSyncService.sync(
                        targets
                );


        List<StockClassificationUniverseSyncService.SyncResult>
                classificationResults =
                classificationUniverseSyncService.sync(
                        targets,
                        classificationEffectiveFrom,
                        classificationSource
                );


        List<MarketPriceUniverseSyncService.SyncResult>
                priceResults =
                marketPriceUniverseSyncService.sync(
                        targets,
                        priceStartDate,
                        priceEndDate
                );


        return new SyncResult(
                targets.size(),
                fundamentalResults,
                classificationResults,
                priceResults
        );
    }


    private void validateDates(
            LocalDate classificationEffectiveFrom,
            LocalDate priceStartDate,
            LocalDate priceEndDate
    ) {

        if (classificationEffectiveFrom == null) {

            throw new IllegalArgumentException(
                    "classificationEffectiveFrom은 null일 수 없습니다."
            );
        }


        if (priceStartDate == null
                || priceEndDate == null
                || priceStartDate.isAfter(priceEndDate)) {

            throw new IllegalArgumentException(
                    "Invalid price sync date range"
            );
        }
    }


    public record SyncResult(

            int targetCount,

            List<FundamentalUniverseSyncService.SyncResult>
            fundamentals,

            List<StockClassificationUniverseSyncService.SyncResult>
            classifications,

            List<MarketPriceUniverseSyncService.SyncResult>
            prices

    ) {
    }
}