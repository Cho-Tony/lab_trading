package com.tony.tradinglab.analysis.service;

import com.tony.tradinglab.analysis.domain.InvestmentAnalysisRequest;
import com.tony.tradinglab.analysis.domain.InvestmentAnalysisTrigger;
import com.tony.tradinglab.analysis.domain.QuantAnalysisSnapshot;
import com.tony.tradinglab.discovery.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvestmentAnalysisRequestFactoryTest {

    private final InvestmentAnalysisRequestFactory factory =
            new InvestmentAnalysisRequestFactory();

    @Test
    void createFromCatalystPromotion() {

        StockCandidate candidate =
                new StockCandidate(
                        1L,
                        "TEST",
                        DiscoverySource.BOTH,
                        LocalDateTime.of(
                                2026,
                                9,
                                5,
                                10,
                                0
                        )
                );

        CandidateQualityAssessment qualityAssessment =
                new CandidateQualityAssessment(
                        CandidateQualityDecision.REVIEW,
                        List.of(
                                CandidateQualityReason
                                        .GROWTH_INVESTMENT_CANDIDATE
                        ),
                        2,
                        1
                );

        CandidateRoutingResult routingResult =
                new CandidateRoutingResult(
                        candidate,
                        qualityAssessment,
                        CandidateRoute.CATALYST_REVIEW
                );

        CatalystEvidence evidence =
                new CatalystEvidence(
                        CatalystType.LARGE_CONTRACT,
                        CatalystEvidenceStatus.VERIFIED,
                        EvidenceQuality.SEC_OR_GOVERNMENT,
                        "Large Contract",
                        "Material contract confirmed.",
                        "SEC",
                        "https://example.com",
                        LocalDate.of(
                                2026,
                                9,
                                1
                        )
                );

        CatalystReviewResult catalystResult =
                new CatalystReviewResult(
                        1L,
                        "TEST",
                        List.of(evidence),
                        LocalDateTime.of(
                                2026,
                                9,
                                5,
                                11,
                                0
                        )
                );

        CatalystReviewAssessment catalystAssessment =
                new CatalystReviewAssessment(
                        CatalystReviewDecision
                                .PROMOTE_TO_DEEP_ANALYSIS,
                        List.of(
                                CatalystType.LARGE_CONTRACT
                        ),
                        List.of(),
                        1,
                        0,
                        1,
                        1
                );

        QuantAnalysisSnapshot quantAnalysis =
                new QuantAnalysisSnapshot(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );


        InvestmentAnalysisRequest request =
                factory.createFromCatalystPromotion(
                        routingResult,
                        quantAnalysis,
                        catalystResult,
                        catalystAssessment
                );


        assertThat(
                request.trigger()
        ).isEqualTo(
                InvestmentAnalysisTrigger
                        .CATALYST_PROMOTED
        );

        assertThat(
                request.discoverySource()
        ).isEqualTo(
                DiscoverySource.BOTH
        );

        assertThat(
                request.catalystEvidences()
        ).hasSize(1);
    }
}