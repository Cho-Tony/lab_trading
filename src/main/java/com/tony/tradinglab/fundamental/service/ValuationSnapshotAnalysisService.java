package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ValuationSnapshotAnalysisService {

    private final ValuationPeerSnapshotAssembler snapshotAssembler;
    private final PeerUniverseBuilder peerUniverseBuilder;
    private final ValuationComparisonService comparisonService;


    public ValuationSnapshotAnalysisService(
            ValuationPeerSnapshotAssembler snapshotAssembler,
            PeerUniverseBuilder peerUniverseBuilder,
            ValuationComparisonService comparisonService
    ) {

        this.snapshotAssembler =
                snapshotAssembler;

        this.peerUniverseBuilder =
                peerUniverseBuilder;

        this.comparisonService =
                comparisonService;
    }


    public ValuationSnapshotAnalysisReport analyze(
            LocalDate observationDate,
            List<ValuationPeerSnapshotInput> inputs
    ) {

        if (observationDate == null
                || inputs == null
                || inputs.isEmpty()) {

            return new ValuationSnapshotAnalysisReport(

                    observationDate,

                    0,
                    0,

                    List.of()
            );
        }


        /*
         * 동일한 observation date의 데이터만 사용.
         */
        List<ValuationPeerSnapshotInput> sameDateInputs =
                inputs.stream()

                        .filter(
                                input ->
                                        input != null
                                                && observationDate.equals(
                                                input.observationDate()
                                        )
                        )

                        .toList();


        if (sameDateInputs.isEmpty()) {

            return new ValuationSnapshotAnalysisReport(

                    observationDate,

                    0,
                    0,

                    List.of()
            );
        }


        /*
         * Classification 조회 + Snapshot 생성은
         * 전체 종목에 대해 한 번만 수행한다.
         */
        List<ValuationPeerSnapshot> snapshots =
                snapshotAssembler.assemble(
                        sameDateInputs
                );


        List<ValuationComparisonResult> results =
                snapshots.stream()

                        .map(
                                target -> {

                                    PeerUniverse universe =
                                            peerUniverseBuilder.build(

                                                    target,
                                                    snapshots
                                            );


                                    return comparisonService.compare(

                                            universe,

                                            RegressionValuationMetric.PS
                                    );
                                }
                        )

                        .toList();


        return new ValuationSnapshotAnalysisReport(

                observationDate,

                sameDateInputs.size(),
                snapshots.size(),

                List.copyOf(
                        results
                )
        );
    }
}