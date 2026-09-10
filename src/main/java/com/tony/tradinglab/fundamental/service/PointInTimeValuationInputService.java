package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PointInTimeValuationInputService {

    private final PointInTimeValuationAssembler valuationAssembler;

    private final ValuationPeerSnapshotInputFactory inputFactory;


    public PointInTimeValuationInputService(
            PointInTimeValuationAssembler valuationAssembler,
            ValuationPeerSnapshotInputFactory inputFactory
    ) {

        this.valuationAssembler =
                valuationAssembler;

        this.inputFactory =
                inputFactory;
    }


    public Optional<ValuationPeerSnapshotInput> create(
            PointInTimeValuationBuildRequest request
    ) {

        if (request == null
                || request.fundamentalAnalysis() == null
                || request.sharePrice() == null
                || request.sharePrice()
                .compareTo(BigDecimal.ZERO) <= 0
                || request.cashFlows() == null
                || request.sharesFacts() == null) {

            return Optional.empty();
        }


        PointInTimeFundamentalAnalysis fundamental =
                request.fundamentalAnalysis();


        if (fundamental.stockId() == null
                || fundamental.symbol() == null
                || fundamental.symbol().isBlank()
                || fundamental.asOfDate() == null
                || fundamental.ttmFinancials() == null) {

            return Optional.empty();
        }


        /*
         * PointInTimeFundamentalAnalysis에서 이미
         * asOfDate 기준 최신 TTM 하나를 골라놨다.
         *
         * 기존 PointInTimeValuationAssembler는
         * List<TtmFinancials>를 받으므로
         * 해당 TTM 하나를 List로 감싼다.
         */
        Optional<ValuationMetrics> valuation =
                valuationAssembler.assemble(

                        fundamental.asOfDate(),

                        request.sharePrice(),

                        List.of(
                                fundamental.ttmFinancials()
                        ),

                        request.cashFlows(),

                        request.sharesFacts()
                );


        if (valuation.isEmpty()) {

            return Optional.empty();
        }


        /*
         * 마지막 PIT validation을 거친 뒤
         * 실제 Peer Universe / Backtest용 Input 생성.
         */
        return inputFactory.create(

                fundamental.stockId(),

                fundamental.symbol(),

                fundamental.asOfDate(),

                valuation.get(),

                fundamental.growth(),

                fundamental.profitability()
        );
    }
}