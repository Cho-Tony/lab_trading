package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.DebtCapacityMetrics;
import com.tony.tradinglab.fundamental.domain.FinancialHealthMetrics;
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
public class DebtCapacityCalculator {

    public List<DebtCapacityMetrics> calculate(
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

                    String key =
                            createKey(health);

                    TtmCashFlow cashFlow =
                            cashFlowMap.get(key);

                    if (cashFlow == null) {
                        return null;
                    }

                    BigDecimal netDebtToFcf =
                            calculateDebtRatio(
                                    health.netDebt(),
                                    cashFlow.freeCashFlow()
                            );

                    BigDecimal debtToFcf =
                            calculateDebtRatio(
                                    health.totalDebt(),
                                    cashFlow.freeCashFlow()
                            );

                    boolean netCash =
                            health.netDebt() != null
                                    && health.netDebt()
                                    .compareTo(
                                            BigDecimal.ZERO
                                    ) < 0;

                    return new DebtCapacityMetrics(

                            health.fiscalYear(),
                            health.fiscalQuarter(),

                            health.totalDebt(),
                            health.cash(),
                            health.netDebt(),

                            cashFlow.operatingCashFlow(),
                            cashFlow.freeCashFlow(),

                            netDebtToFcf,
                            debtToFcf,

                            netCash,

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
                                        DebtCapacityMetrics::fiscalYear
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

    private BigDecimal calculateDebtRatio(
            BigDecimal debt,
            BigDecimal fcf
    ) {

        if (debt == null
                || fcf == null
                || fcf.compareTo(BigDecimal.ZERO) <= 0) {

            return null;
        }

        return debt.divide(
                fcf,
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