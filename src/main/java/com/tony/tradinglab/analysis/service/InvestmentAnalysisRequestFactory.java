package com.tony.tradinglab.analysis.service;

import com.tony.tradinglab.analysis.domain.InvestmentAnalysisRequest;
import com.tony.tradinglab.analysis.domain.InvestmentAnalysisTrigger;
import com.tony.tradinglab.analysis.domain.QuantAnalysisSnapshot;
import com.tony.tradinglab.discovery.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvestmentAnalysisRequestFactory {

    public InvestmentAnalysisRequest createFromQuantPass(
            CandidateRoutingResult routingResult,
            QuantAnalysisSnapshot quantAnalysis
    ) {

        if (routingResult.route()
                != CandidateRoute.DEEP_ANALYSIS) {

            throw new IllegalArgumentException(
                    "DEEP_ANALYSIS 후보만 Quant Pass 요청을 생성할 수 있습니다."
            );
        }

        StockCandidate candidate =
                routingResult.candidate();

        return new InvestmentAnalysisRequest(

                candidate.stockId(),
                candidate.symbol(),

                candidate.discoverySource(),

                InvestmentAnalysisTrigger.QUANT_PASS,

                routingResult.assessment(),

                quantAnalysis,

                List.of()
        );
    }


    public InvestmentAnalysisRequest createFromCatalystPromotion(
            CandidateRoutingResult routingResult,
            QuantAnalysisSnapshot quantAnalysis,
            CatalystReviewResult catalystResult,
            CatalystReviewAssessment catalystAssessment
    ) {

        if (routingResult.route()
                != CandidateRoute.CATALYST_REVIEW) {

            throw new IllegalArgumentException(
                    "CATALYST_REVIEW 후보가 아닙니다."
            );
        }

        if (catalystAssessment.decision()
                != CatalystReviewDecision
                .PROMOTE_TO_DEEP_ANALYSIS) {

            throw new IllegalArgumentException(
                    "Catalyst가 Deep Analysis로 승격되지 않았습니다."
            );
        }

        StockCandidate candidate =
                routingResult.candidate();

        return new InvestmentAnalysisRequest(

                candidate.stockId(),
                candidate.symbol(),

                candidate.discoverySource(),

                InvestmentAnalysisTrigger
                        .CATALYST_PROMOTED,

                routingResult.assessment(),

                quantAnalysis,

                List.copyOf(
                        catalystResult.evidences()
                )
        );
    }
}