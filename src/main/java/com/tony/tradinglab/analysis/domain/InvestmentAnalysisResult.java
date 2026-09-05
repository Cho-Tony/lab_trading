package com.tony.tradinglab.analysis.domain;

import com.tony.tradinglab.discovery.domain.CatalystType;

import java.time.LocalDateTime;
import java.util.List;

public record InvestmentAnalysisResult(

        Long stockId,
        String symbol,

        InvestmentDecision decision,

        int confidenceScore,

        CatalystStrength catalystStrength,

        GrowthSustainability growthSustainability,

        CompetitiveAdvantageLevel competitiveAdvantage,

        ValuationRiskLevel valuationRisk,

        InvestmentRiskLevel riskLevel,

        String investmentThesis,

        List<String> keyPositiveFactors,

        List<String> keyRisks,

        List<CatalystType> importantCatalysts,

        List<String> thesisInvalidationConditions,

        LocalDateTime analyzedAt

) {
}