package com.tony.tradinglab.fundamental.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialStatementData(

        String symbol,

        // 이 실적이 어느 기간의 것인지
        LocalDate periodEndDate,

        // 시장에 실제 공개된 날짜
        LocalDate filedDate,

        // SEC fiscal period
        Integer fiscalYear,
        String fiscalQuarter,

        // Income Statement
        BigDecimal revenue,
        BigDecimal operatingIncome,
        BigDecimal netIncome,

        // Balance Sheet
        BigDecimal totalAssets,
        BigDecimal totalEquity,
        BigDecimal totalDebt,
        BigDecimal cash,

        // Cash Flow
        BigDecimal operatingCashFlow,
        BigDecimal capitalExpenditure,

        // Shares
        BigDecimal sharesOutstanding

) {
}