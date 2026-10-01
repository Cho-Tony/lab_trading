package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.universe.UniverseTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuantBacktestService {

    private final QuantDiscoveryEngine quantDiscoveryEngine;

    private final QuantUniverseProvider quantUniverseProvider;

    private final QuantBacktestSampleFactory
            quantBacktestSampleFactory;


    public List<QuantBacktestSample> run(
            LocalDate observationDate
    ) {

        if (observationDate == null) {
            return List.of();
        }


        /*
         * 해당 시점의 Universe.
         *
         * 장기적으로 Universe 자체도 PIT가 되므로
         * observationDate를 그대로 전달한다.
         */
        List<UniverseTarget> universe =
                quantUniverseProvider
                        .getUniverse(
                                observationDate
                        );


        if (universe.isEmpty()) {
            return List.of();
        }


        /*
         * Candidate -> exchange 연결용.
         */
        Map<String, UniverseTarget> targetBySymbol =
                universe.stream()

                        .collect(
                                Collectors.toMap(
                                        UniverseTarget::symbol,
                                        Function.identity()
                                )
                        );


        /*
         * observationDate 시점에서
         * 실제로 알 수 있었던 정보만 이용하여
         * Quant Score를 계산한다.
         */
        List<QuantDiscoveryCandidate> candidates =
                quantDiscoveryEngine
                        .discover(
                                observationDate
                        );


        /*
         * 이후 실제 가격을 결합하여
         * supervised/backtest sample 생성.
         */
        return candidates.stream()

                .map(
                        candidate -> {

                            UniverseTarget target =
                                    targetBySymbol.get(
                                            candidate.symbol()
                                    );


                            if (target == null) {
                                return null;
                            }


                            return quantBacktestSampleFactory
                                    .create(
                                            candidate,
                                            target.exchange()
                                    )
                                    .orElse(null);
                        }
                )

                .filter(
                        sample ->
                                sample != null
                )

                .toList();
    }

    public List<QuantBacktestSample> run(
            List<LocalDate> observationDates
    ) {

        if (observationDates == null
                || observationDates.isEmpty()) {

            return List.of();
        }


        return observationDates.stream()

                .filter(
                        date ->
                                date != null
                )

                .distinct()

                .sorted()

                .flatMap(
                        date ->
                                run(date)
                                        .stream()
                )

                .toList();
    }
}