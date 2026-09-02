package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.FinancialHealthMetrics;
import com.tony.tradinglab.fundamental.domain.LiquidityMetrics;
import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LiquidityMetricsCalculator {

    private static final BigDecimal QUARTERS_PER_YEAR =
            new BigDecimal("4");

    public List<LiquidityMetrics> calculate(
            List<FinancialHealthMetrics> healthMetrics,
            List<TtmCashFlow> ttmCashFlows
    ) {

        Map<String, TtmCashFlow> cashFlowMap =
                ttmCashFlows.stream()
                        .collect(
                                Collectors.toMap(
                                        this::createKey,
                                        cashFlow -> cashFlow
                                )
                        );

        return healthMetrics.stream()
                .map(health -> {

                    TtmCashFlow cashFlow =
                            cashFlowMap.get(
                                    createKey(health)
                            );

                    if (cashFlow == null) {
                        return null;
                    }

                    BigDecimal ttmOcf =
                            cashFlow.operatingCashFlow();

                    BigDecimal ttmFcf =
                            cashFlow.freeCashFlow();

                    boolean operatingCashFlowPositive =
                            ttmOcf != null
                                    && ttmOcf.compareTo(
                                    BigDecimal.ZERO
                            ) > 0;

                    boolean freeCashFlowPositive =
                            ttmFcf != null
                                    && ttmFcf.compareTo(
                                    BigDecimal.ZERO
                            ) > 0;

                    boolean capexDrivenNegativeFcf =
                            operatingCashFlowPositive
                                    && ttmFcf != null
                                    && ttmFcf.compareTo(
                                    BigDecimal.ZERO
                            ) < 0;

                    BigDecimal annualCashBurn =
                            calculateAnnualCashBurn(
                                    ttmFcf
                            );

                    BigDecimal runwayYears =
                            calculateRunwayYears(
                                    health.cash(),
                                    annualCashBurn
                            );

                    BigDecimal runwayQuarters =
                            runwayYears != null
                                    ? runwayYears
                                    .multiply(
                                            QUARTERS_PER_YEAR
                                    )
                                    .setScale(
                                            2,
                                            RoundingMode.HALF_UP
                                    )
                                    : null;

                    return new LiquidityMetrics(

                            health.fiscalYear(),
                            health.fiscalQuarter(),

                            health.cash(),

                            ttmOcf,
                            ttmFcf,

                            annualCashBurn,

                            runwayYears,
                            runwayQuarters,

                            operatingCashFlowPositive,
                            freeCashFlowPositive,

                            capexDrivenNegativeFcf,

                            latestFiledDate(
                                    health.filedDate(),
                                    cashFlow.filedDate()
                            )
                    );
                })
                .filter(metrics -> metrics != null)
                .sorted(
                        Comparator
                                .comparing(
                                        LiquidityMetrics::fiscalYear
                                )
                                .thenComparing(
                                        metrics ->
                                                quarterOrder(
                                                        metrics.fiscalQuarter()
                                                )
                                )
                )
                .toList();
    }

    private BigDecimal calculateAnnualCashBurn(
            BigDecimal ttmFreeCashFlow
    ) {

        if (ttmFreeCashFlow == null
                || ttmFreeCashFlow.compareTo(
                BigDecimal.ZERO
        ) >= 0) {

            return null;
        }

        return ttmFreeCashFlow.abs();
    }

    private BigDecimal calculateRunwayYears(
            BigDecimal cash,
            BigDecimal annualCashBurn
    ) {

        if (cash == null
                || annualCashBurn == null
                || annualCashBurn.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            return null;
        }

        return cash.divide(
                annualCashBurn,
                2,
                RoundingMode.HALF_UP
        );
    }

    private String createKey(
            FinancialHealthMetrics health
    ) {

        return createKey(
                health.fiscalYear(),
                health.fiscalQuarter()
        );
    }

    private String createKey(
            TtmCashFlow cashFlow
    ) {

        return createKey(
                cashFlow.fiscalYear(),
                cashFlow.fiscalQuarter()
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

    private LocalDate latestFiledDate(
            LocalDate first,
            LocalDate second
    ) {

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return first.isAfter(second)
                ? first
                : second;
    }

    private int quarterOrder(
            String quarter
    ) {

        return switch (quarter) {

            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;

            default ->
                    throw new IllegalArgumentException(
                            "알 수 없는 quarter: "
                                    + quarter
                    );
        };
    }
}