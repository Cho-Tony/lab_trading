package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalystReviewEvaluatorTest {

    private final CatalystReviewEvaluator evaluator =
            new CatalystReviewEvaluator();


    @Test
    void promoteWhenStrongEvidenceExists() {

        CatalystReviewRequest request =
                createRequest();


        CatalystReviewResult result =
                new CatalystReviewResult(

                        1L,
                        "TEST",

                        List.of(

                                createEvidence(
                                        CatalystType.LARGE_CONTRACT,
                                        CatalystEvidenceStatus.VERIFIED,
                                        EvidenceQuality.SEC_OR_GOVERNMENT
                                ),

                                createEvidence(
                                        CatalystType.CAPACITY_EXPANSION,
                                        CatalystEvidenceStatus.VERIFIED,
                                        EvidenceQuality.COMPANY_OFFICIAL
                                )
                        ),

                        LocalDateTime.of(
                                2026,
                                9,
                                4,
                                20,
                                0
                        )
                );


        CatalystReviewAssessment assessment =
                evaluator.evaluate(
                        request,
                        result
                );


        System.out.println(
                "Decision = "
                        + assessment.decision()
        );

        System.out.println(
                "Verified = "
                        + assessment.verifiedCatalysts()
        );

        System.out.println(
                "Strong Evidence = "
                        + assessment.strongEvidenceCount()
        );


        assertThat(
                assessment.decision()
        ).isEqualTo(
                CatalystReviewDecision
                        .PROMOTE_TO_DEEP_ANALYSIS
        );

        assertThat(
                assessment.strongEvidenceCount()
        ).isEqualTo(2);
    }


    @Test
    void requestMoreReviewWhenEvidenceIsInsufficient() {

        CatalystReviewRequest request =
                createRequest();


        CatalystReviewResult result =
                new CatalystReviewResult(

                        1L,
                        "TEST",

                        List.of(

                                createEvidence(
                                        CatalystType.LARGE_CONTRACT,
                                        CatalystEvidenceStatus.UNVERIFIED,
                                        EvidenceQuality.REPUTABLE_MEDIA
                                )
                        ),

                        LocalDateTime.of(
                                2026,
                                9,
                                4,
                                20,
                                0
                        )
                );


        CatalystReviewAssessment assessment =
                evaluator.evaluate(
                        request,
                        result
                );


        assertThat(
                assessment.decision()
        ).isEqualTo(
                CatalystReviewDecision.MORE_REVIEW
        );
    }


    @Test
    void stopWhenAllCatalystsAreNotFound() {

        CatalystReviewRequest request =
                createRequest();


        CatalystReviewResult result =
                new CatalystReviewResult(

                        1L,
                        "TEST",

                        List.of(

                                createEvidence(
                                        CatalystType.LARGE_CONTRACT,
                                        CatalystEvidenceStatus.NOT_FOUND,
                                        EvidenceQuality.REPUTABLE_MEDIA
                                ),

                                createEvidence(
                                        CatalystType.CAPACITY_EXPANSION,
                                        CatalystEvidenceStatus.NOT_FOUND,
                                        EvidenceQuality.COMPANY_OFFICIAL
                                )
                        ),

                        LocalDateTime.of(
                                2026,
                                9,
                                4,
                                20,
                                0
                        )
                );


        CatalystReviewAssessment assessment =
                evaluator.evaluate(
                        request,
                        result
                );


        assertThat(
                assessment.decision()
        ).isEqualTo(
                CatalystReviewDecision.STOP
        );
    }


    private CatalystReviewRequest createRequest() {

        return new CatalystReviewRequest(

                1L,
                "TEST",

                DiscoverySource.QUANT,

                List.of(
                        CatalystType.LARGE_CONTRACT,
                        CatalystType.CAPACITY_EXPANSION
                ),

                List.of(
                        CandidateQualityReason
                                .GROWTH_INVESTMENT_CANDIDATE
                )
        );
    }


    private CatalystEvidence createEvidence(
            CatalystType catalystType,
            CatalystEvidenceStatus status,
            EvidenceQuality quality
    ) {

        return new CatalystEvidence(

                catalystType,
                status,
                quality,

                "Test Evidence",

                "Test summary",

                "Test Source",

                "https://example.com",

                LocalDate.of(
                        2026,
                        9,
                        1
                )
        );
    }
}