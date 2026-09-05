package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.*;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class CatalystReviewEvaluator {

    private static final int STRONG_EVIDENCE_MIN_QUALITY =
            4;

    private static final int SUPPORTING_VERIFIED_MIN_QUALITY =
            2;

    private static final int SUPPORTING_PARTIAL_MIN_QUALITY =
            3;

    public CatalystReviewAssessment evaluate(
            CatalystReviewRequest request,
            CatalystReviewResult result
    ) {

        Set<CatalystType> reviewedTypes =
                new LinkedHashSet<>();

        Set<CatalystType> verifiedCatalysts =
                new LinkedHashSet<>();

        Set<CatalystType> contradictedCatalysts =
                new LinkedHashSet<>();


        int strongEvidenceCount = 0;
        int supportingEvidenceCount = 0;


        for (CatalystEvidence evidence
                : result.evidences()) {

            reviewedTypes.add(
                    evidence.catalystType()
            );


            if (isStrongEvidence(evidence)) {

                strongEvidenceCount++;

                verifiedCatalysts.add(
                        evidence.catalystType()
                );

                continue;
            }


            if (isSupportingEvidence(evidence)) {

                supportingEvidenceCount++;

                verifiedCatalysts.add(
                        evidence.catalystType()
                );
            }


            if (evidence.status()
                    == CatalystEvidenceStatus.CONTRADICTED) {

                contradictedCatalysts.add(
                        evidence.catalystType()
                );
            }
        }


        CatalystReviewDecision decision =
                determineDecision(

                        request,

                        strongEvidenceCount,
                        supportingEvidenceCount,

                        contradictedCatalysts,

                        reviewedTypes
                );


        return new CatalystReviewAssessment(

                decision,

                List.copyOf(
                        verifiedCatalysts
                ),

                List.copyOf(
                        contradictedCatalysts
                ),

                strongEvidenceCount,
                supportingEvidenceCount,

                request.catalystTypes().size(),
                reviewedTypes.size()
        );
    }


    private CatalystReviewDecision determineDecision(
            CatalystReviewRequest request,

            int strongEvidenceCount,
            int supportingEvidenceCount,

            Set<CatalystType> contradictedCatalysts,
            Set<CatalystType> reviewedTypes
    ) {

        boolean hasPositiveEvidence =
                strongEvidenceCount > 0
                        || supportingEvidenceCount > 0;


        /*
         * 상충 근거가 있고
         * 긍정 근거가 전혀 없다면 종료.
         */
        if (!contradictedCatalysts.isEmpty()
                && !hasPositiveEvidence) {

            return CatalystReviewDecision.STOP;
        }


        /*
         * 강한 공식 근거가 하나 이상 있고,
         * 반대 근거가 없다면 Deep Analysis로 승격.
         */
        if (strongEvidenceCount >= 1
                && contradictedCatalysts.isEmpty()) {

            return CatalystReviewDecision
                    .PROMOTE_TO_DEEP_ANALYSIS;
        }


        /*
         * 강한 공식근거는 없더라도
         * 서로 보조하는 근거가 2개 이상이라면 승격.
         */
        if (supportingEvidenceCount >= 2
                && contradictedCatalysts.isEmpty()) {

            return CatalystReviewDecision
                    .PROMOTE_TO_DEEP_ANALYSIS;
        }


        boolean allTypesReviewed =
                reviewedTypes.containsAll(
                        request.catalystTypes()
                );


        /*
         * 요청한 항목을 전부 조사했는데도
         * 긍정 근거가 하나도 없다면 종료.
         */
        if (allTypesReviewed
                && !hasPositiveEvidence) {

            return CatalystReviewDecision.STOP;
        }


        /*
         * 아직 조사되지 않은 Catalyst가 있거나
         * 근거가 애매하다면 추가 조사.
         */
        return CatalystReviewDecision.MORE_REVIEW;
    }


    private boolean isStrongEvidence(
            CatalystEvidence evidence
    ) {

        return evidence.status()
                == CatalystEvidenceStatus.VERIFIED

                && evidence.evidenceQuality() != null

                && evidence.evidenceQuality()
                .score()
                >= STRONG_EVIDENCE_MIN_QUALITY;
    }


    private boolean isSupportingEvidence(
            CatalystEvidence evidence
    ) {

        if (evidence.evidenceQuality() == null) {
            return false;
        }

        int quality =
                evidence.evidenceQuality()
                        .score();


        if (evidence.status()
                == CatalystEvidenceStatus.VERIFIED) {

            return quality
                    >= SUPPORTING_VERIFIED_MIN_QUALITY;
        }


        if (evidence.status()
                == CatalystEvidenceStatus.PARTIALLY_VERIFIED) {

            return quality
                    >= SUPPORTING_PARTIAL_MIN_QUALITY;
        }


        return false;
    }
}