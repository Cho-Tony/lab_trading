package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.GrowthAcceleration;
import com.tony.tradinglab.fundamental.domain.RevenueGrowth;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GrowthAccelerationCalculator {

    public List<GrowthAcceleration> calculate(
            List<RevenueGrowth> growths
    ) {

        Map<String, RevenueGrowth> growthMap =
                growths.stream()
                        .filter(growth ->
                                growth.yoyGrowthPct() != null
                        )
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        growth -> growth,
                                        this::selectPreferredGrowth
                                )
                        );

        return growthMap.values()
                .stream()
                .sorted(
                        Comparator
                                .comparing(RevenueGrowth::fiscalYear)
                                .thenComparing(
                                        growth ->
                                                quarterOrder(
                                                        growth.fiscalQuarter()
                                                )
                                )
                )
                .map(current -> {

                    QuarterKey previousQuarter =
                            getPreviousQuarter(
                                    current.fiscalYear(),
                                    current.fiscalQuarter()
                            );

                    RevenueGrowth previous =
                            growthMap.get(
                                    createKey(
                                            previousQuarter.fiscalYear(),
                                            previousQuarter.fiscalQuarter()
                                    )
                            );

                    if (previous == null) {
                        return null;
                    }

                    BigDecimal acceleration =
                            current.yoyGrowthPct()
                                    .subtract(
                                            previous.yoyGrowthPct()
                                    )
                                    .setScale(
                                            2,
                                            RoundingMode.HALF_UP
                                    );

                    return new GrowthAcceleration(
                            current.fiscalYear(),
                            current.fiscalQuarter(),

                            current.yoyGrowthPct(),
                            previous.yoyGrowthPct(),

                            acceleration,

                            current.filedDate()
                    );
                })
                .filter(result -> result != null)
                .toList();
    }

    private QuarterKey getPreviousQuarter(
            Integer fiscalYear,
            String fiscalQuarter
    ) {

        return switch (fiscalQuarter) {

            case "Q1" ->
                    new QuarterKey(
                            fiscalYear - 1,
                            "Q4"
                    );

            case "Q2" ->
                    new QuarterKey(
                            fiscalYear,
                            "Q1"
                    );

            case "Q3" ->
                    new QuarterKey(
                            fiscalYear,
                            "Q2"
                    );

            case "Q4" ->
                    new QuarterKey(
                            fiscalYear,
                            "Q3"
                    );

            default ->
                    throw new IllegalArgumentException(
                            "알 수 없는 fiscal quarter: "
                                    + fiscalQuarter
                    );
        };
    }

    private String createKey(
            RevenueGrowth growth
    ) {

        return createKey(
                growth.fiscalYear(),
                growth.fiscalQuarter()
        );
    }

    private String createKey(
            Integer fiscalYear,
            String fiscalQuarter
    ) {

        return fiscalYear
                + "-"
                + fiscalQuarter;
    }

    private RevenueGrowth selectPreferredGrowth(
            RevenueGrowth first,
            RevenueGrowth second
    ) {

        if (first.filedDate()
                .isBefore(second.filedDate())) {

            return first;
        }

        return second;
    }

    private int quarterOrder(
            String quarter
    ) {

        return switch (quarter) {

            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;

            default -> 99;
        };
    }

    private record QuarterKey(
            Integer fiscalYear,
            String fiscalQuarter
    ) {
    }
}