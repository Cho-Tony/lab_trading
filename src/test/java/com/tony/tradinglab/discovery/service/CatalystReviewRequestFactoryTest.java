package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalystReviewRequestFactoryTest {

    private final CatalystReviewRequestFactory factory =
            new CatalystReviewRequestFactory();


    @Test
    void createGrowthInvestmentReviewRequest() {

        StockCandidate candidate =
                new StockCandidate(

                        1L,
                        "TEST",

                        DiscoverySource.BOTH,

                        LocalDateTime.of(
                                2026,
                                9,
                                4,
                                20,
                                0
                        )
                );


        CandidateQualityAssessment assessment =
                new CandidateQualityAssessment(

                        CandidateQualityDecision.REVIEW,

                        List.of(

                                CandidateQualityReason
                                        .REVENUE_ACCELERATING,

                                CandidateQualityReason
                                        .GROWTH_INVESTMENT_CANDIDATE,

                                CandidateQualityReason
                                        .CAPEX_EXPANDING,

                                CandidateQualityReason
                                        .CAPEX_DRIVEN_NEGATIVE_FCF
                        ),

                        2,
                        1
                );


        CandidateRoutingResult routingResult =
                new CandidateRoutingResult(

                        candidate,
                        assessment,

                        CandidateRoute.CATALYST_REVIEW
                );


        CatalystReviewRequest request =
                factory.create(
                        routingResult
                );


        System.out.println(
                "Symbol = "
                        + request.symbol()
        );

        System.out.println(
                "Source = "
                        + request.discoverySource()
        );

        System.out.println(
                "Catalyst Types = "
                        + request.catalystTypes()
        );

        System.out.println(
                "Reasons = "
                        + request.triggerReasons()
        );


        assertThat(
                request.catalystTypes()
        ).contains(

                CatalystType.CAPEX_EXPANSION,

                CatalystType.CAPACITY_EXPANSION,

                CatalystType.DEBT_PURPOSE,

                CatalystType.LARGE_CONTRACT,

                CatalystType.NEW_CUSTOMER,

                CatalystType.BACKLOG_GROWTH
        );
    }
}