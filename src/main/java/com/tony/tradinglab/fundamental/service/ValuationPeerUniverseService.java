package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PeerUniverse;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ValuationPeerUniverseService {

    private final ValuationPeerSnapshotAssembler snapshotAssembler;
    private final PeerUniverseBuilder peerUniverseBuilder;


    public ValuationPeerUniverseService(
            ValuationPeerSnapshotAssembler snapshotAssembler,
            PeerUniverseBuilder peerUniverseBuilder
    ) {

        this.snapshotAssembler =
                snapshotAssembler;

        this.peerUniverseBuilder =
                peerUniverseBuilder;
    }


    public Optional<PeerUniverse> build(
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


        /*
         * Peer Universe는 반드시
         * 동일한 observationDate 기준으로 구성한다.
         *
         * 2025-06-30 valuation과
         * 2025-07-31 valuation을 섞으면 안 된다.
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

            return Optional.empty();
        }


        List<ValuationPeerSnapshot> snapshots =
                snapshotAssembler.assemble(
                        sameDateInputs
                );


        /*
         * Target도 classification이 존재해야
         * Peer Universe를 구성할 수 있다.
         */
        Optional<ValuationPeerSnapshot> target =
                snapshots.stream()

                        .filter(
                                snapshot ->
                                        targetStockId.equals(
                                                snapshot.stockId()
                                        )
                        )

                        .findFirst();


        if (target.isEmpty()) {

            return Optional.empty();
        }


        PeerUniverse universe =
                peerUniverseBuilder.build(

                        target.get(),

                        snapshots
                );


        return Optional.of(
                universe
        );
    }
}