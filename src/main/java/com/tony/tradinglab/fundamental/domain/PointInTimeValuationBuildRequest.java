package com.tony.tradinglab.fundamental.domain;

import java.math.BigDecimal;
import java.util.List;

public record PointInTimeValuationBuildRequest(

        PointInTimeFundamentalAnalysis fundamentalAnalysis,

        BigDecimal sharePrice,

        List<TtmCashFlow> cashFlows,

        List<SecFactPoint> sharesFacts

) {
}