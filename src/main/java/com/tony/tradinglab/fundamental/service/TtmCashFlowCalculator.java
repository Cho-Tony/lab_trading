package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.QuarterlyCashFlow;
import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class TtmCashFlowCalculator {

    public List<TtmCashFlow> calculate(
            List<QuarterlyCashFlow> cashFlows
    ) {

        List<QuarterlyCashFlow> sorted =
                cashFlows.stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                QuarterlyCashFlow::fiscalYear
                                        )
                                        .thenComparing(
                                                cashFlow ->
                                                        quarterOrder(
                                                                cashFlow.fiscalQuarter()
                                                        )
                                        )
                        )
                        .toList();

        List<TtmCashFlow> result =
                new ArrayList<>();

        for (int i = 3; i < sorted.size(); i++) {

            List<QuarterlyCashFlow> fourQuarters =
                    sorted.subList(
                            i - 3,
                            i + 1
                    );

            if (!isConsecutive(fourQuarters)) {
                continue;
            }

            QuarterlyCashFlow latest =
                    fourQuarters.get(3);

            BigDecimal ttmOperatingCashFlow =
                    sumOperatingCashFlow(
                            fourQuarters
                    );

            BigDecimal ttmCapex =
                    sumCapitalExpenditure(
                            fourQuarters
                    );

            BigDecimal ttmFreeCashFlow =
                    sumFreeCashFlow(
                            fourQuarters
                    );

            LocalDate latestFiledDate =
                    fourQuarters.stream()
                            .map(
                                    QuarterlyCashFlow::filedDate
                            )
                            .filter(
                                    date -> date != null
                            )
                            .max(
                                    LocalDate::compareTo
                            )
                            .orElse(null);

            result.add(
                    new TtmCashFlow(

                            latest.fiscalYear(),
                            latest.fiscalQuarter(),

                            ttmOperatingCashFlow,
                            ttmCapex,
                            ttmFreeCashFlow,

                            latestFiledDate
                    )
            );
        }

        return result;
    }

    private BigDecimal sumOperatingCashFlow(
            List<QuarterlyCashFlow> cashFlows
    ) {

        return cashFlows.stream()
                .map(
                        QuarterlyCashFlow::operatingCashFlow
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private BigDecimal sumCapitalExpenditure(
            List<QuarterlyCashFlow> cashFlows
    ) {

        return cashFlows.stream()
                .map(
                        QuarterlyCashFlow::capitalExpenditure
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private BigDecimal sumFreeCashFlow(
            List<QuarterlyCashFlow> cashFlows
    ) {

        return cashFlows.stream()
                .map(
                        QuarterlyCashFlow::freeCashFlow
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private boolean isConsecutive(
            List<QuarterlyCashFlow> cashFlows
    ) {

        for (int i = 1; i < cashFlows.size(); i++) {

            QuarterlyCashFlow previous =
                    cashFlows.get(i - 1);

            QuarterlyCashFlow current =
                    cashFlows.get(i);

            if (!isNextQuarter(
                    previous,
                    current
            )) {

                return false;
            }
        }

        return true;
    }

    private boolean isNextQuarter(
            QuarterlyCashFlow previous,
            QuarterlyCashFlow current
    ) {

        if ("Q4".equals(
                previous.fiscalQuarter()
        )) {

            return current.fiscalYear()
                    == previous.fiscalYear() + 1

                    && "Q1".equals(
                    current.fiscalQuarter()
            );
        }

        return current.fiscalYear()
                .equals(
                        previous.fiscalYear()
                )

                && quarterOrder(
                current.fiscalQuarter()
        )
                == quarterOrder(
                previous.fiscalQuarter()
        ) + 1;
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