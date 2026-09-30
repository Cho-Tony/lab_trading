package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.fundamental.domain.ValuationComparisonResult;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.fundamental.service.PointInTimeValuationComparisonService;
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

    private final PointInTimeValuationComparisonService
            valuationComparisonService;

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


        /*
         * 1.
         * 먼저 Universe 전체의 PIT Snapshot을 만든다.
         *
         * Valuation 상대평가를 하려면
         * 한 종목만 있어서는 안 되고
         * 같은 observationDate의 peer snapshot들이
         * 먼저 준비되어 있어야 한다.
         */
        List<ValuationPeerSnapshotInput> snapshots =
                new ArrayList<>();


        for (UniverseTarget target : universe) {

            try {

                valuationSnapshotService
                        .analyze(
                                target.symbol(),
                                target.exchange(),
                                observationDate
                        )

                        .ifPresentOrElse(
                                snapshots::add,

                                () ->
                                        log.debug(
                                                "Quant discovery snapshot unavailable. "
                                                        + "symbol={} observationDate={}",
                                                target.symbol(),
                                                observationDate
                                        )
                        );

            } catch (RuntimeException e) {

                /*
                 * 한 종목의 데이터 문제가
                 * 전체 Discovery를 중단시키면 안 된다.
                 */
                log.warn(
                        "Quant discovery snapshot failed. "
                                + "symbol={} observationDate={} message={}",
                        target.symbol(),
                        observationDate,
                        e.getMessage()
                );
            }
        }


        if (snapshots.isEmpty()) {
            return List.of();
        }


        /*
         * 2.
         * 각 종목을 전체 snapshot universe와 비교한 뒤
         * Quant Score를 계산한다.
         */
        List<QuantDiscoveryCandidate> candidates =
                new ArrayList<>();


        for (ValuationPeerSnapshotInput snapshot : snapshots) {

            ValuationComparisonResult valuationComparison =
                    null;


            try {

                Optional<ValuationComparisonResult>
                        comparisonOptional =
                        valuationComparisonService
                                .compare(
                                        snapshot.stockId(),
                                        observationDate,
                                        snapshots
                                );


                valuationComparison =
                        comparisonOptional
                                .orElse(null);

            } catch (RuntimeException e) {

                /*
                 * Valuation 상대평가가 실패하더라도
                 * Growth / Quality 정보까지 버릴 필요는 없다.
                 *
                 * valuationComparison = null 상태로
                 * QuantScorer에 넘기면
                 * 현재 구현에서는 Valuation Score가 0이 된다.
                 */
                log.warn(
                        "Quant valuation comparison failed. "
                                + "symbol={} observationDate={} message={}",
                        snapshot.symbol(),
                        observationDate,
                        e.getMessage()
                );
            }


            try {

                QuantScoreBreakdown score =
                        quantScorer.score(
                                snapshot,
                                valuationComparison
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

                log.warn(
                        "Quant scoring failed. "
                                + "symbol={} observationDate={} message={}",
                        snapshot.symbol(),
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