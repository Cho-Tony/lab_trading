package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.GrowthAcceleration;
import com.tony.tradinglab.fundamental.domain.GrowthTrendAnalysis;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.RevenueGrowth;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PointInTimeGrowthAnalyzer {

    private final RevenueGrowthCalculator revenueGrowthCalculator;
    private final GrowthAccelerationCalculator growthAccelerationCalculator;
    private final GrowthTrendAnalyzer growthTrendAnalyzer;


    public PointInTimeGrowthAnalyzer(
            RevenueGrowthCalculator revenueGrowthCalculator,
            GrowthAccelerationCalculator growthAccelerationCalculator,
            GrowthTrendAnalyzer growthTrendAnalyzer
    ) {

        this.revenueGrowthCalculator =
                revenueGrowthCalculator;

        this.growthAccelerationCalculator =
                growthAccelerationCalculator;

        this.growthTrendAnalyzer =
                growthTrendAnalyzer;
    }


    public Optional<GrowthTrendAnalysis> analyze(
            List<QuarterlyFact> revenueFacts,
            LocalDate asOfDate
    ) {

        if (revenueFacts == null
                || revenueFacts.isEmpty()
                || asOfDate == null) {

            return Optional.empty();
        }


        List<QuarterlyFact> availableRevenueFacts =
                selectAvailableRevenueFacts(
                        revenueFacts,
                        asOfDate
                );


        if (availableRevenueFacts.isEmpty()) {

            return Optional.empty();
        }


        List<RevenueGrowth> growths =
                revenueGrowthCalculator.calculate(
                        availableRevenueFacts
                );


        if (growths == null
                || growths.isEmpty()) {

            return Optional.empty();
        }


        List<GrowthAcceleration> accelerations =
                growthAccelerationCalculator.calculate(
                        growths
                );


        if (accelerations == null
                || accelerations.isEmpty()) {

            return Optional.empty();
        }


        GrowthTrendAnalysis analysis =
                growthTrendAnalyzer.analyze(
                        accelerations
                );


        return Optional.ofNullable(
                analysis
        );
    }


    private List<QuarterlyFact> selectAvailableRevenueFacts(
            List<QuarterlyFact> revenueFacts,
            LocalDate asOfDate
    ) {

        Map<FiscalQuarterKey, QuarterlyFact> latestByQuarter =
                revenueFacts.stream()

                        .filter(
                                Objects::nonNull
                        )

                        /*
                         * 미래 공시 제거
                         */
                        .filter(
                                fact ->
                                        fact.filedDate() != null
                                                && !fact.filedDate()
                                                .isAfter(asOfDate)
                        )

                        .filter(
                                fact ->
                                        fact.fiscalYear() != null
                                                && fact.fiscalQuarter() != null
                        )

                        /*
                         * 같은 FY/Q가 여러 번 있으면
                         * asOfDate 당시 이용 가능한 최신 filing 사용
                         */
                        .collect(
                                Collectors.toMap(

                                        fact ->
                                                new FiscalQuarterKey(
                                                        fact.fiscalYear(),
                                                        fact.fiscalQuarter()
                                                ),

                                        fact ->
                                                fact,

                                        this::laterFiling
                                )
                        );


        return latestByQuarter.values()
                .stream()

                .sorted(
                        Comparator
                                .comparing(
                                        QuarterlyFact::fiscalYear
                                )
                                .thenComparing(
                                        QuarterlyFact::fiscalQuarter
                                )
                )

                .toList();
    }


    private QuarterlyFact laterFiling(
            QuarterlyFact first,
            QuarterlyFact second
    ) {

        return second.filedDate()
                .isAfter(first.filedDate())
                ? second
                : first;
    }


    private record FiscalQuarterKey(
            Integer fiscalYear,
            String fiscalQuarter
    ) {
    }
}