package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.ProfitabilityDirection;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrend;
import com.tony.tradinglab.fundamental.domain.ProfitabilityTrendAnalysis;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfitabilityTrendAnalyzerTest {

    private final ProfitabilityTrendAnalyzer analyzer =
            new ProfitabilityTrendAnalyzer();

    @Test
    void analyzeImprovingProfitability() {

            List<ProfitabilityTrend> trends =
                    List.of(

                            createTrend(
                                    2025,
                                    "Q2",
                                    "12.00",
                                    "10.00",
                                    "2.00",
                                    "8.00",
                                    "6.00",
                                    "2.00"
                            ),

                            createTrend(
                                    2025,
                                    "Q3",
                                    "12.40",
                                    "12.00",
                                    "0.40",
                                    "8.30",
                                    "8.00",
                                    "0.30"
                            ),

                            createTrend(
                                    2025,
                                    "Q4",
                                    "14.00",
                                    "12.40",
                                    "1.60",
                                    "10.00",
                                    "8.30",
                                    "1.70"
                            )
                    );

            ProfitabilityTrendAnalysis analysis =
                    analyzer.analyze(trends);

            System.out.println(
                    "Operating Margin Direction = "
                            + analysis.operatingMarginDirection()
            );

            System.out.println(
                    "Net Margin Direction = "
                            + analysis.netMarginDirection()
            );

            System.out.println(
                    "Average Operating Margin Change = "
                            + analysis.averageOperatingMarginChangePctPoint()
                            + "%p"
            );

            System.out.println(
                    "Average Net Margin Change = "
                            + analysis.averageNetMarginChangePctPoint()
                            + "%p"
            );


            assertThat(
                    analysis.operatingMarginDirection()
            ).isEqualTo(
                    ProfitabilityDirection.IMPROVING
            );

            assertThat(
                    analysis.netMarginDirection()
            ).isEqualTo(
                    ProfitabilityDirection.IMPROVING
            );
        }

    private ProfitabilityTrend createTrend(

            int fiscalYear,
            String fiscalQuarter,

            String operatingMargin,
            String previousOperatingMargin,
            String operatingChange,

            String netMargin,
            String previousNetMargin,
            String netChange
    ) {

        return new ProfitabilityTrend(

                fiscalYear,
                fiscalQuarter,

                new BigDecimal(operatingMargin),
                new BigDecimal(previousOperatingMargin),
                new BigDecimal(operatingChange),

                new BigDecimal(netMargin),
                new BigDecimal(previousNetMargin),
                new BigDecimal(netChange),

                LocalDate.of(
                        2025,
                        1,
                        1
                )
        );
    }
    }