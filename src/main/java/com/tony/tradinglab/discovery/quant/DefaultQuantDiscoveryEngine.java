package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.fundamental.service.PointInTimeValuationSnapshotService;
import com.tony.tradinglab.universe.UniverseTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultQuantDiscoveryEngine
        implements QuantDiscoveryEngine {

    private final QuantUniverseProvider universeProvider;

    private final PointInTimeValuationSnapshotService
            valuationSnapshotService;

    private final QuantScorer quantScorer;


    @Override
    public List<QuantDiscoveryCandidate> discover(
            LocalDate observationDate
    ) {

        if (observationDate == null) {
            return List.of();
        }


        List<UniverseTarget> universe =
                universeProvider.getUniverse(
                        observationDate
                );


        if (universe.isEmpty()) {
            return List.of();
        }


        List<QuantDiscoveryCandidate> candidates =
                new ArrayList<>();


        for (UniverseTarget target : universe) {

            try {

                Optional<ValuationPeerSnapshotInput> snapshotOptional =
                        valuationSnapshotService.analyze(
                                target.symbol(),
                                target.exchange(),
                                observationDate
                        );


                if (snapshotOptional.isEmpty()) {

                    log.debug(
                            "Quant discovery snapshot unavailable. symbol={} observationDate={}",
                            target.symbol(),
                            observationDate
                    );

                    continue;
                }


                ValuationPeerSnapshotInput snapshot =
                        snapshotOptional.get();


                QuantScoreBreakdown score =
                        quantScorer.score(
                                snapshot
                        );


                QuantDiscoveryCandidate candidate =
                        new QuantDiscoveryCandidate(
                                snapshot.stockId(),
                                snapshot.symbol(),
                                snapshot.observationDate(),
                                score
                        );


                candidates.add(
                        candidate
                );

            } catch (RuntimeException e) {

                /*
                 * 한 종목의 오류로 전체 Discovery가
                 * 중단되지 않도록 종목 단위로 격리한다.
                 */
                log.warn(
                        "Quant discovery failed. symbol={} observationDate={} message={}",
                        target.symbol(),
                        observationDate,
                        e.getMessage()
                );
            }
        }


        return List.copyOf(
                candidates
        );
    }
}