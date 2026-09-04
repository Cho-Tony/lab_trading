package com.tony.tradinglab.discovery.domain;

public record CandidateRoutingResult(

        StockCandidate candidate,

        CandidateQualityAssessment assessment,

        CandidateRoute route

) {
}