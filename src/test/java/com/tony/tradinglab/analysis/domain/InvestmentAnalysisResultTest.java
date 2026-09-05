package com.tony.tradinglab.analysis.domain;

import com.tony.tradinglab.discovery.domain.CatalystType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvestmentAnalysisResultTest {

    @Test
    void createInvestmentAnalysisResult() {

        InvestmentAnalysisResult result =
                new InvestmentAnalysisResult(

                        1L,
                        "TEST",

                        InvestmentDecision.BUY,

                        88,

                        CatalystStrength.STRONG,

                        GrowthSustainability.HIGH,

                        CompetitiveAdvantageLevel.MODERATE,

                        ValuationRiskLevel.MODERATE,

                        InvestmentRiskLevel.MODERATE,

                        """
                        Revenue growth is accelerating,
                        supported by new customer wins,
                        capacity expansion and strong industry demand.
                        """,

                        List.of(
                                "Revenue accelerating",
                                "Strong backlog growth",
                                "Capacity expansion",
                                "Improving operating margin"
                        ),

                        List.of(
                                "Customer concentration",
                                "High industry competition",
                                "Execution risk"
                        ),

                        List.of(
                                CatalystType.LARGE_CONTRACT,
                                CatalystType.NEW_CUSTOMER,
                                CatalystType.CAPACITY_EXPANSION
                        ),

                        List.of(
                                "Revenue growth falls sharply",
                                "Major customer loss",
                                "Margin expansion fails",
                                "Capacity expansion does not generate revenue"
                        ),

                        LocalDateTime.of(
                                2026,
                                9,
                                5,
                                16,
                                0
                        )
                );


        assertThat(
                result.decision()
        ).isEqualTo(
                InvestmentDecision.BUY
        );


        assertThat(
                result.confidenceScore()
        ).isEqualTo(88);


        assertThat(
                result.thesisInvalidationConditions()
        ).isNotEmpty();
    }
}