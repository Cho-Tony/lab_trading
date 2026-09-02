package com.tony.tradinglab.discovery.domain;

import java.util.List;

public record CandidateQualityAssessment(

        CandidateQualityDecision decision,

        List<CandidateQualityReason> reasons,

        int positiveSignalCount,
        int riskSignalCount

) {
}