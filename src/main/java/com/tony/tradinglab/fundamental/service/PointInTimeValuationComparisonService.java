package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.RegressionValuationMetric;
import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PointInTimeValuationComparisonService {

    private final ValuationPeerUniverseService peerUniverseService;
    private final ValuationComparisonService comparisonService;


    public PointInTimeValuationComparisonService(
            ValuationPeerUniverseService peerUniverseService,
            ValuationComparisonService comparisonService
    ) {

        this.peerUniverseService =
                peerUniverseService;

        this.comparisonService =
                comparisonService;
    }


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


        Optional<PeerUniverse> universe =
                peerUniverseService.build(

                        targetStockId,
                        observationDate,
                        inputs
                );


        if (universe.isEmpty()) {

            return Optional.empty();
        }


        ValuationComparisonResult result =
                comparisonService.compare(
                        universe.get(),
                        RegressionValuationMetric.PS
                );


        return Optional.of(
                result
        );
    }
}