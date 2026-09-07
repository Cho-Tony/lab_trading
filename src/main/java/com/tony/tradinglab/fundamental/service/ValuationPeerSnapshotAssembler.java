package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshot;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValuationPeerSnapshotAssembler {

    private final ValuationPeerSnapshotFactory factory;


    public ValuationPeerSnapshotAssembler(
            ValuationPeerSnapshotFactory factory
    ) {

        this.factory = factory;
    }


    public List<ValuationPeerSnapshot> assemble(
            List<ValuationPeerSnapshotInput> inputs
    ) {

        if (inputs == null
                || inputs.isEmpty()) {

            return List.of();
        }


        return inputs.stream()

                .filter(
                        input ->
                                input != null
                )

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

                /*
                 * Optional.empty()
                 * 즉 PIT classification을 찾지 못한 종목 제거
                 */
                .flatMap(
                        optional ->
                                optional.stream()
                )

                .toList();
    }
}