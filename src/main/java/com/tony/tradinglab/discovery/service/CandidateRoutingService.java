package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.*;
import org.springframework.stereotype.Service;

@Service
public class CandidateRoutingService {

    public CandidateRoutingResult route(
            StockCandidate candidate,
            CandidateQualityAssessment assessment
    ) {

        CandidateRoute route =
                determineRoute(
                        assessment.decision()
                );

        return new CandidateRoutingResult(
                candidate,
                assessment,
                route
        );
    }

    private CandidateRoute determineRoute(
            CandidateQualityDecision decision
    ) {

        return switch (decision) {

            case REJECT ->
                    CandidateRoute.STOP;

            case REVIEW ->
                    CandidateRoute.CATALYST_REVIEW;

            case PASS ->
                    CandidateRoute.DEEP_ANALYSIS;
        };
    }
}