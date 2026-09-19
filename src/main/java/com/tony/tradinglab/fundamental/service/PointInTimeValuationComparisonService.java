package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.RegressionValuationMetric;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointInTimeValuationComparisonService {

    private final ValuationPeerUniverseService peerUniverseService;

    private final ValuationComparisonService comparisonService;


    public Optional<ValuationComparisonResult> compare(
            Long targetStockId,
            LocalDate observationDate,
            List<ValuationPeerSnapshotInput> inputs
    ) {

        if (targetStockId == null
                || observationDate == null
                || inputs == null
                || inputs.isEmpty()) {

            return Optional.empty();
        }


        return peerUniverseService
                .build(
                        targetStockId,
                        observationDate,
                        inputs
                )
                .map(
                        universe ->
                                comparisonService.compare(
                                        universe,
                                        RegressionValuationMetric.PS
                                )
                );
    }
}