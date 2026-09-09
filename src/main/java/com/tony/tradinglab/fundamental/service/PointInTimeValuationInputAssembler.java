package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.PointInTimeValuationInputRequest;
import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PointInTimeValuationInputAssembler {

    private final ValuationPeerSnapshotInputFactory inputFactory;


    public PointInTimeValuationInputAssembler(
            ValuationPeerSnapshotInputFactory inputFactory
    ) {

        this.inputFactory =
                inputFactory;
    }


    public Optional<ValuationPeerSnapshotInput> assemble(
            PointInTimeValuationInputRequest request
    ) {

        if (request == null) {

            return Optional.empty();
        }


        return inputFactory.create(

                request.stockId(),

                request.symbol(),

                request.observationDate(),

                request.valuation(),

                request.growth(),

                request.profitability()
        );
    }
}