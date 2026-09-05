package com.tony.tradinglab.analysis.domain;

import com.tony.tradinglab.discovery.domain.*;

import java.util.List;

public record InvestmentAnalysisRequest(

        Long stockId,
        String symbol,

        DiscoverySource discoverySource,

        InvestmentAnalysisTrigger trigger,

        CandidateQualityAssessment qualityAssessment,

        QuantAnalysisSnapshot quantAnalysis,

        List<CatalystEvidence> catalystEvidences

) {
}