package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.*;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class CatalystReviewRequestFactory {

    public CatalystReviewRequest create(
            CandidateRoutingResult routingResult
    ) {

        if (routingResult.route()
                != CandidateRoute.CATALYST_REVIEW) {

            throw new IllegalArgumentException(
                    "CATALYST_REVIEW 후보만 요청을 생성할 수 있습니다."
            );
        }

        StockCandidate candidate =
                routingResult.candidate();

        CandidateQualityAssessment assessment =
                routingResult.assessment();

        Set<CatalystType> catalystTypes =
                new LinkedHashSet<>();


        for (CandidateQualityReason reason
                : assessment.reasons()) {

            addCatalystTypes(
                    reason,
                    catalystTypes
            );
        }


        /*
         * 특별한 조사 타입이 결정되지 않았다면
         * 일반 Catalyst 탐색
         */
        if (catalystTypes.isEmpty()) {

            catalystTypes.add(
                    CatalystType.OTHER
            );
        }


        return new CatalystReviewRequest(

                candidate.stockId(),
                candidate.symbol(),
                candidate.discoverySource(),

                List.copyOf(
                        catalystTypes
                ),

                assessment.reasons()
        );
    }


    private void addCatalystTypes(
            CandidateQualityReason reason,
            Set<CatalystType> catalystTypes
    ) {

        switch (reason) {

            case GROWTH_INVESTMENT_CANDIDATE -> {

                catalystTypes.add(
                        CatalystType.CAPACITY_EXPANSION
                );

                catalystTypes.add(
                        CatalystType.DEBT_PURPOSE
                );

                catalystTypes.add(
                        CatalystType.LARGE_CONTRACT
                );

                catalystTypes.add(
                        CatalystType.NEW_CUSTOMER
                );

                catalystTypes.add(
                        CatalystType.BACKLOG_GROWTH
                );
            }


            case CAPEX_EXPANDING -> {

                catalystTypes.add(
                        CatalystType.CAPEX_EXPANSION
                );

                catalystTypes.add(
                        CatalystType.CAPACITY_EXPANSION
                );
            }


            case CAPEX_DRIVEN_NEGATIVE_FCF -> {

                catalystTypes.add(
                        CatalystType.CAPEX_EXPANSION
                );

                catalystTypes.add(
                        CatalystType.DEBT_PURPOSE
                );
            }


            case FCF_TURNAROUND -> {

                catalystTypes.add(
                        CatalystType.TURNAROUND
                );

                catalystTypes.add(
                        CatalystType.NEW_PRODUCT
                );

                catalystTypes.add(
                        CatalystType.COMMERCIALIZATION
                );
            }


            case REVENUE_ACCELERATING -> {

                catalystTypes.add(
                        CatalystType.NEW_CUSTOMER
                );

                catalystTypes.add(
                        CatalystType.LARGE_CONTRACT
                );

                catalystTypes.add(
                        CatalystType.BACKLOG_GROWTH
                );

                catalystTypes.add(
                        CatalystType.INDUSTRY_TAILWIND
                );
            }


            default -> {
                // 해당 이유만으로는
                // 별도 Catalyst 조사 항목을 추가하지 않음.
            }
        }
    }
}