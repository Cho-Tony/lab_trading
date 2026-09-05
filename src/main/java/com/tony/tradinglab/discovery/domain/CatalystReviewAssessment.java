package com.tony.tradinglab.discovery.domain;

import java.util.List;

public record CatalystReviewAssessment(

        CatalystReviewDecision decision,

        List<CatalystType> verifiedCatalysts,
        List<CatalystType> contradictedCatalysts,

        int strongEvidenceCount,
        int supportingEvidenceCount,

        int requestedTypeCount,
        int reviewedTypeCount

) {
}