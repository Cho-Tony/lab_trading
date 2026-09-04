package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateRoutingServiceTest {

    private final CandidateRoutingService routingService =
            new CandidateRoutingService();


    @Test
    void routePassCandidateToDeepAnalysis() {

        StockCandidate candidate =
                createCandidate();

        CandidateQualityAssessment assessment =
                new CandidateQualityAssessment(
                        CandidateQualityDecision.PASS,
                        List.of(
                                CandidateQualityReason.REVENUE_ACCELERATING,
                                CandidateQualityReason.FCF_IMPROVING
                        ),
                        2,
                        0
                );


        CandidateRoutingResult result =
                routingService.route(
                        candidate,
                        assessment
                );


        assertThat(
                result.route()
        ).isEqualTo(
                CandidateRoute.DEEP_ANALYSIS
        );
    }


    @Test
    void routeReviewCandidateToCatalystReview() {

        StockCandidate candidate =
                createCandidate();

        CandidateQualityAssessment assessment =
                new CandidateQualityAssessment(
                        CandidateQualityDecision.REVIEW,
                        List.of(
                                CandidateQualityReason
                                        .GROWTH_INVESTMENT_CANDIDATE,

                                CandidateQualityReason
                                        .CAPEX_EXPANDING
                        ),
                        1,
                        1
                );


        CandidateRoutingResult result =
                routingService.route(
                        candidate,
                        assessment
                );


        assertThat(
                result.route()
        ).isEqualTo(
                CandidateRoute.CATALYST_REVIEW
        );
    }


    @Test
    void routeRejectedCandidateToStop() {

        StockCandidate candidate =
                createCandidate();

        CandidateQualityAssessment assessment =
                new CandidateQualityAssessment(
                        CandidateQualityDecision.REJECT,
                        List.of(
                                CandidateQualityReason.REVENUE_DECELERATING,
                                CandidateQualityReason.PROFITABILITY_DETERIORATING,
                                CandidateQualityReason.PERSISTENT_NEGATIVE_FCF,
                                CandidateQualityReason.SHORT_CASH_RUNWAY
                        ),
                        0,
                        5
                );


        CandidateRoutingResult result =
                routingService.route(
                        candidate,
                        assessment
                );


        assertThat(
                result.route()
        ).isEqualTo(
                CandidateRoute.STOP
        );
    }


    private StockCandidate createCandidate() {

        return new StockCandidate(
                1L,
                "TEST",
                DiscoverySource.QUANT,
                LocalDateTime.of(
                        2026,
                        9,
                        4,
                        20,
                        0
                )
        );
    }
}