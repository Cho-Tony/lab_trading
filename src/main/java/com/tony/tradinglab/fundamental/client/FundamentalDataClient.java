package com.tony.tradinglab.fundamental.client;

import com.tony.tradinglab.fundamental.dto.FinancialStatementData;

import java.util.List;

public interface FundamentalDataClient {

    List<FinancialStatementData> getFinancialStatements(
            String symbol
    );
}