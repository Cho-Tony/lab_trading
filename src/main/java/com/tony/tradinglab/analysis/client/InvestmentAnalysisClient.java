package com.tony.tradinglab.analysis.client;

import com.tony.tradinglab.analysis.domain.InvestmentAnalysisRequest;
import com.tony.tradinglab.analysis.domain.InvestmentAnalysisResult;

public interface InvestmentAnalysisClient {

    InvestmentAnalysisResult analyze(
            InvestmentAnalysisRequest request
    );
}