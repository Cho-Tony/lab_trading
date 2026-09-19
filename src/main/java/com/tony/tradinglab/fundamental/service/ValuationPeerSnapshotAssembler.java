package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ValuationPeerSnapshotAssembler {

    private final ValuationPeerSnapshotFactory factory;


    public List<ValuationPeerSnapshot> assemble(
            List<ValuationPeerSnapshotInput> inputs
    ) {

        if (inputs == null
                || inputs.isEmpty()) {

            return List.of();
        }


        return inputs.stream()
                .filter(input -> input != null)
                .map(
                        input ->
                                factory.create(
                                        input.stockId(),
                                        input.symbol(),
                                        input.observationDate(),
                                        input.valuation(),
                                        input.growth(),
                                        input.profitability()
                                )
                )
                .flatMap(Optional::stream)
                .toList();
    }
}